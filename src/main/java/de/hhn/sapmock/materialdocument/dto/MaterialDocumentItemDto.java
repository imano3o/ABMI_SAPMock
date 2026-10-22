package de.hhn.sapmock.materialdocument.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.hhn.sapmock.materialdocument.MaterialDocumentItem;

/**
 * Position im SAP-Format (A_MaterialDocumentItem). Wird für Request und Response verwendet;
 * die mit "nur Response" markierten Felder setzt der Mock, im Request sind sie null.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MaterialDocumentItemDto(
        @JsonProperty("MaterialDocumentYear") String materialDocumentYear,      // nur Response
        @JsonProperty("MaterialDocument") String materialDocument,              // nur Response
        @JsonProperty("MaterialDocumentItem") String materialDocumentItem,      // nur Response
        @JsonProperty("Material") String material,
        @JsonProperty("Plant") String plant,
        @JsonProperty("StorageLocation") String storageLocation,
        @JsonProperty("GoodsMovementType") String goodsMovementType,
        @JsonProperty("QuantityInEntryUnit") String quantityInEntryUnit,        // String wie bei SAP
        @JsonProperty("EntryUnit") String entryUnit,
        @JsonProperty("CostCenter") String costCenter
) {

    public static MaterialDocumentItemDto from(MaterialDocumentItem item) {
        return new MaterialDocumentItemDto(
                item.getMaterialDocument().getMaterialDocumentYear(),
                item.getMaterialDocument().getMaterialDocument(),
                item.getMaterialDocumentItem(),
                item.getMaterial(),
                item.getPlant(),
                item.getStorageLocation(),
                item.getGoodsMovementType(),
                item.getQuantity().stripTrailingZeros().toPlainString(),
                item.getEntryUnit(),
                item.getCostCenter()
        );
    }
}
