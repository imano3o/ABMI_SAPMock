package de.hhn.sapmock.materialdocument.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.hhn.sapmock.materialdocument.MaterialDocumentItem;

/**
 * Item in SAP format (A_MaterialDocumentItem). Used for request and response;
 * the fields marked "response only" are set by the mock and are null in the request.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MaterialDocumentItemDto(
        @JsonProperty("MaterialDocumentYear") String materialDocumentYear,      // response only
        @JsonProperty("MaterialDocument") String materialDocument,              // response only
        @JsonProperty("MaterialDocumentItem") String materialDocumentItem,      // response only
        @JsonProperty("Material") String material,
        @JsonProperty("Plant") String plant,
        @JsonProperty("StorageLocation") String storageLocation,
        @JsonProperty("GoodsMovementType") String goodsMovementType,
        @JsonProperty("QuantityInEntryUnit") String quantityInEntryUnit,        // String, as in SAP
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
