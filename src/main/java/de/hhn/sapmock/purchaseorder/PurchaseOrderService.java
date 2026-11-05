package de.hhn.sapmock.purchaseorder;

import de.hhn.sapmock.materialstock.MaterialStock;
import de.hhn.sapmock.materialstock.MaterialStockRepository;
import de.hhn.sapmock.odata.ODataException;
import de.hhn.sapmock.purchaseorder.dto.PurchaseOrderDto;
import de.hhn.sapmock.purchaseorder.dto.PurchaseOrderItemDto;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Creates purchase orders like SAP does: checks the master data and assigns the number.
 * How much to order is decided in the ABMI backend, the mock only records the order.
 */
@Service
public class PurchaseOrderService {

    private static final long FIRST_ORDER_NUMBER = 4500000001L;
    private static final String STANDARD_ORDER = "NB";
    private static final int ITEM_STEP = 10;

    private final PurchaseOrderRepository orderRepository;
    private final MaterialStockRepository stockRepository;

    public PurchaseOrderService(PurchaseOrderRepository orderRepository, MaterialStockRepository stockRepository) {
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findAll() {
        List<PurchaseOrder> orders = orderRepository.findAll(Sort.by("purchaseOrder"));
        // Load the items inside the transaction, because open-in-view is off
        orders.forEach(order -> order.getItems().size());
        return orders;
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrderDto request) {
        if (isBlank(request.supplier())) {
            throw new ODataException("ME/083", "Enter a supplier");
        }
        List<PurchaseOrderItemDto> requestItems = request.toPurchaseOrderItem() == null
                ? List.of()
                : request.toPurchaseOrderItem().results();
        if (requestItems == null || requestItems.isEmpty()) {
            throw new ODataException("NO_ITEMS", "Purchase order must contain at least one item");
        }

        PurchaseOrder order = new PurchaseOrder(
                String.valueOf(FIRST_ORDER_NUMBER + orderRepository.count()),
                isBlank(request.companyCode()) ? "1710" : request.companyCode(),
                isBlank(request.purchaseOrderType()) ? STANDARD_ORDER : request.purchaseOrderType(),
                request.supplier(),
                isBlank(request.purchasingOrganization()) ? "1710" : request.purchasingOrganization(),
                request.purchasingGroup(),
                LocalDate.now()
        );

        int itemNumber = ITEM_STEP;
        for (PurchaseOrderItemDto requestItem : requestItems) {
            order.addItem(createItem(requestItem, String.valueOf(itemNumber)));
            itemNumber += ITEM_STEP;
        }
        return orderRepository.save(order);
    }

    private PurchaseOrderItem createItem(PurchaseOrderItemDto item, String itemNumber) {
        BigDecimal quantity = parseQuantity(item.orderQuantity());

        // In SAP the material must be maintained for the plant; the mock uses the stock table as master data
        MaterialStock stock = stockRepository
                .findByMaterialAndPlantAndStorageLocation(item.material(), item.plant(), item.storageLocation())
                .orElseThrow(() -> new ODataException("ME/062", "Material " + item.material()
                        + " not maintained in plant " + item.plant() + ", storage location " + item.storageLocation()));

        String unit = isBlank(item.purchaseOrderQuantityUnit()) ? stock.getBaseUnit() : item.purchaseOrderQuantityUnit();
        return new PurchaseOrderItem(itemNumber, item.material(), item.plant(), item.storageLocation(), quantity, unit);
    }

    private static BigDecimal parseQuantity(String value) {
        try {
            BigDecimal quantity = new BigDecimal(value);
            if (quantity.signum() <= 0) {
                throw new ODataException("INVALID_QUANTITY", "Order quantity must be greater than 0");
            }
            return quantity;
        } catch (NumberFormatException | NullPointerException e) {
            throw new ODataException("INVALID_QUANTITY", "Invalid order quantity: " + value);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
