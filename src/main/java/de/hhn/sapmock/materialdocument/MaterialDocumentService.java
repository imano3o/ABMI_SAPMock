package de.hhn.sapmock.materialdocument;

import de.hhn.sapmock.materialdocument.dto.MaterialDocumentHeaderDto;
import de.hhn.sapmock.materialdocument.dto.MaterialDocumentItemDto;
import de.hhn.sapmock.materialstock.MaterialStock;
import de.hhn.sapmock.materialstock.MaterialStockRepository;
import de.hhn.sapmock.odata.ODataException;
import de.hhn.sapmock.purchaseorder.PurchaseOrderItem;
import de.hhn.sapmock.purchaseorder.PurchaseOrderItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Posts material documents and updates the stock, just like SAP does.
 * Supported movement types: 201 (consumption for cost center) and 101 (goods receipt for a purchase order).
 * Business rules of the hospital system (minimum stock, status) are deliberately not here but in the ABMI backend.
 */
@Service
public class MaterialDocumentService {

    private static final String CONSUMPTION = "201";
    private static final String GOODS_RECEIPT = "101";
    private static final String GOODS_ISSUE_CODE = "03";
    private static final long FIRST_DOCUMENT_NUMBER = 4900000001L;
    private static final int MAX_HEADER_TEXT_LENGTH = 25;
    private static final Pattern ODATA_DATE = Pattern.compile("/Date\\((-?\\d+)[^)]*\\)/");

    private final MaterialDocumentRepository documentRepository;
    private final MaterialStockRepository stockRepository;
    private final PurchaseOrderItemRepository orderItemRepository;

    public MaterialDocumentService(MaterialDocumentRepository documentRepository, MaterialStockRepository stockRepository,
                                   PurchaseOrderItemRepository orderItemRepository) {
        this.documentRepository = documentRepository;
        this.stockRepository = stockRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Creates a material document. If one item fails, @Transactional ensures that
     * nothing is posted, not even the previous items.
     */
    @Transactional
    public MaterialDocument create(MaterialDocumentHeaderDto request) {
        List<MaterialDocumentItemDto> requestItems = request.toMaterialDocumentItem() == null
                ? List.of()
                : request.toMaterialDocumentItem().results();
        if (requestItems == null || requestItems.isEmpty()) {
            throw new ODataException("NO_ITEMS", "Material document must contain at least one item");
        }

        LocalDate postingDate = parsePostingDate(request.postingDate());
        MaterialDocument document = new MaterialDocument(
                String.valueOf(FIRST_DOCUMENT_NUMBER + documentRepository.count()),
                String.valueOf(postingDate.getYear()),
                postingDate,
                request.goodsMovementCode() == null ? GOODS_ISSUE_CODE : request.goodsMovementCode(),
                truncate(request.materialDocumentHeaderText())
        );

        int itemNumber = 1;
        for (MaterialDocumentItemDto requestItem : requestItems) {
            document.addItem(postItem(requestItem, String.valueOf(itemNumber++)));
        }
        return documentRepository.save(document);
    }

    private MaterialDocumentItem postItem(MaterialDocumentItemDto item, String itemNumber) {
        String movementType = item.goodsMovementType();
        if (!CONSUMPTION.equals(movementType) && !GOODS_RECEIPT.equals(movementType)) {
            throw new ODataException("UNSUPPORTED_MOVEMENT_TYPE", "Goods movement type " + movementType
                    + " is not supported, only " + CONSUMPTION + " and " + GOODS_RECEIPT);
        }

        BigDecimal quantity = parseQuantity(item.quantityInEntryUnit());

        MaterialStock stock = stockRepository
                .findByMaterialAndPlantAndStorageLocation(item.material(), item.plant(), item.storageLocation())
                .orElseThrow(() -> new ODataException("STOCK_NOT_FOUND", "No stock for material "
                        + item.material() + " in plant " + item.plant() + ", storage location " + item.storageLocation()));

        if (CONSUMPTION.equals(movementType)) {
            consume(stock, quantity, item);
        } else {
            receive(stock, quantity, item);
        }

        String unit = item.entryUnit() == null || item.entryUnit().isBlank() ? stock.getBaseUnit() : item.entryUnit();
        return new MaterialDocumentItem(itemNumber, item.material(), item.plant(), item.storageLocation(),
                movementType, quantity, unit, item.costCenter(), item.purchaseOrder(), item.purchaseOrderItem());
    }

    /** 201: stock goes down, but never below zero (SAP error M7/021). */
    private static void consume(MaterialStock stock, BigDecimal quantity, MaterialDocumentItemDto item) {
        if (quantity.compareTo(stock.getQuantity()) > 0) {
            BigDecimal deficit = quantity.subtract(stock.getQuantity());
            throw new ODataException("M7/021", "Deficit of unrestricted-use stock "
                    + deficit.stripTrailingZeros().toPlainString() + " " + stock.getBaseUnit()
                    + " : " + item.material() + " " + item.plant() + " " + item.storageLocation());
        }
        stock.setQuantity(stock.getQuantity().subtract(quantity));
    }

    /**
     * 101: stock goes up. Like SAP, a goods receipt must reference a purchase order item,
     * must match its material and location, and may not deliver more than is still open.
     * Partial deliveries are allowed; the item is complete once the full quantity has arrived.
     */
    private void receive(MaterialStock stock, BigDecimal quantity, MaterialDocumentItemDto item) {
        if (item.purchaseOrder() == null || item.purchaseOrder().isBlank()) {
            throw new ODataException("M7/146", "Goods receipt needs a purchase order");
        }
        String orderItemNumber = item.purchaseOrderItem() == null || item.purchaseOrderItem().isBlank()
                ? "10" : item.purchaseOrderItem();
        PurchaseOrderItem orderItem = orderItemRepository
                .findByPurchaseOrder_PurchaseOrderAndPurchaseOrderItem(item.purchaseOrder(), orderItemNumber)
                .orElseThrow(() -> new ODataException("M7/013", "Purchase order item "
                        + item.purchaseOrder() + " " + orderItemNumber + " does not exist"));

        if (!orderItem.getMaterial().equals(item.material())
                || !orderItem.getPlant().equals(item.plant())
                || !orderItem.getStorageLocation().equals(item.storageLocation())) {
            throw new ODataException("PO_MISMATCH", "Material or location does not match purchase order item "
                    + item.purchaseOrder() + " " + orderItemNumber);
        }
        if (orderItem.isCompletelyDelivered()) {
            throw new ODataException("M7/022", "Purchase order item " + item.purchaseOrder() + " "
                    + orderItemNumber + " is already completely delivered");
        }
        if (quantity.compareTo(orderItem.getOpenQuantity()) > 0) {
            throw new ODataException("M7/022", "Quantity exceeds open order quantity "
                    + orderItem.getOpenQuantity().stripTrailingZeros().toPlainString() + " "
                    + orderItem.getPurchaseOrderQuantityUnit());
        }

        orderItem.addDeliveredQuantity(quantity);
        stock.setQuantity(stock.getQuantity().add(quantity));
    }

    private static BigDecimal parseQuantity(String value) {
        try {
            BigDecimal quantity = new BigDecimal(value);
            if (quantity.signum() <= 0) {
                throw new ODataException("INVALID_QUANTITY", "Quantity must be greater than 0");
            }
            return quantity;
        } catch (NumberFormatException | NullPointerException e) {
            throw new ODataException("INVALID_QUANTITY", "Invalid quantity: " + value);
        }
    }

    /** Parses "/Date(1792108800000)/"; if no date is given, today's date is posted. */
    private static LocalDate parsePostingDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }
        Matcher matcher = ODATA_DATE.matcher(value);
        if (!matcher.matches()) {
            throw new ODataException("INVALID_DATE", "Invalid PostingDate: " + value);
        }
        return Instant.ofEpochMilli(Long.parseLong(matcher.group(1))).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static String truncate(String text) {
        if (text == null || text.length() <= MAX_HEADER_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_HEADER_TEXT_LENGTH);
    }
}
