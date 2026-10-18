package de.hhn.sapmock.materialdocument.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
        @JsonProperty("CostCenter") String costCenter,
        @JsonProperty("PurchaseOrder") String purchaseOrder,                    // nur bei 101
        @JsonProperty("PurchaseOrderItem") String purchaseOrderItem,            // nur bei 101
        @JsonProperty("DebitCreditCode") String debitCreditCode                 // nur Response
) {}
