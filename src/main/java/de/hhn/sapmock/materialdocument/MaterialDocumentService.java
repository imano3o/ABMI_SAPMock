package de.hhn.sapmock.materialdocument;

import de.hhn.sapmock.materialdocument.dto.MaterialDocumentHeaderDto;
import de.hhn.sapmock.materialdocument.dto.MaterialDocumentItemDto;
import de.hhn.sapmock.materialstock.MaterialStock;
import de.hhn.sapmock.materialstock.MaterialStockRepository;
import de.hhn.sapmock.odata.ODataException;
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
 * Bucht Materialbelege und verändert dabei den Lagerbestand – so wie SAP es tut.
 * Unterstützt wird nur Bewegungsart 201 (Verbrauch an Kostenstelle).
 * Fachliche Regeln des KIS (Mindestbestand, Status) liegen bewusst nicht hier, sondern im ABMI-Backend.
 */
@Service
public class MaterialDocumentService {

    private static final String CONSUMPTION = "201";
    private static final long FIRST_DOCUMENT_NUMBER = 4900000001L;
    private static final int MAX_HEADER_TEXT_LENGTH = 25;
    private static final Pattern ODATA_DATE = Pattern.compile("/Date\\((-?\\d+)[^)]*\\)/");

    private final MaterialDocumentRepository documentRepository;
    private final MaterialStockRepository stockRepository;

    public MaterialDocumentService(MaterialDocumentRepository documentRepository, MaterialStockRepository stockRepository) {
        this.documentRepository = documentRepository;
        this.stockRepository = stockRepository;
    }

    /**
     * Legt einen Materialbeleg an. Schlägt eine Position fehl, wird dank @Transactional
     * nichts gebucht – auch keine der vorherigen Positionen.
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
                truncate(request.materialDocumentHeaderText())
        );

        int itemNumber = 1;
        for (MaterialDocumentItemDto requestItem : requestItems) {
            document.addItem(postItem(requestItem, String.valueOf(itemNumber++)));
        }
        return documentRepository.save(document);
    }

    private MaterialDocumentItem postItem(MaterialDocumentItemDto item, String itemNumber) {
        if (!CONSUMPTION.equals(item.goodsMovementType())) {
            throw new ODataException("UNSUPPORTED_MOVEMENT_TYPE",
                    "Goods movement type " + item.goodsMovementType() + " is not supported, only " + CONSUMPTION);
        }

        BigDecimal quantity = parseQuantity(item.quantityInEntryUnit());

        MaterialStock stock = stockRepository
                .findByMaterialAndPlantAndStorageLocation(item.material(), item.plant(), item.storageLocation())
                .orElseThrow(() -> new ODataException("STOCK_NOT_FOUND", "No stock for material "
                        + item.material() + " in plant " + item.plant() + ", storage location " + item.storageLocation()));

        if (quantity.compareTo(stock.getQuantity()) > 0) {
            BigDecimal deficit = quantity.subtract(stock.getQuantity());
            throw new ODataException("M7/021", "Deficit of unrestricted-use stock "
                    + deficit.stripTrailingZeros().toPlainString() + " " + stock.getBaseUnit()
                    + " : " + item.material() + " " + item.plant() + " " + item.storageLocation());
        }
        stock.setQuantity(stock.getQuantity().subtract(quantity));

        String unit = item.entryUnit() == null || item.entryUnit().isBlank() ? stock.getBaseUnit() : item.entryUnit();
        return new MaterialDocumentItem(itemNumber, item.material(), item.plant(), item.storageLocation(),
                item.goodsMovementType(), quantity, unit, item.costCenter());
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

    /** Liest "/Date(1792108800000)/"; ohne Angabe wird das heutige Datum gebucht. */
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
