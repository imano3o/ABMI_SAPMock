package de.hhn.sapmock.purchaseorder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.hhn.sapmock.purchaseorder.PurchaseOrderItem;

/**
 * Item in SAP format (A_PurchaseOrderItem). Used for request and response;
 * the fields marked "response only" are set by the mock and are null in the request.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PurchaseOrderItemDto(
        @JsonProperty("PurchaseOrder") String purchaseOrder,                    // response only
        @JsonProperty("PurchaseOrderItem") String purchaseOrderItem,            // response only
        @JsonProperty("Material") String material,
        @JsonProperty("Plant") String plant,
        @JsonProperty("StorageLocation") String storageLocation,
        @JsonProperty("OrderQuantity") String orderQuantity,                    // String, as in SAP
        @JsonProperty("PurchaseOrderQuantityUnit") String purchaseOrderQuantityUnit,
        @JsonProperty("IsCompletelyDelivered") Boolean isCompletelyDelivered    // response only
) {

    public static PurchaseOrderItemDto from(PurchaseOrderItem item) {
        return new PurchaseOrderItemDto(
                item.getPurchaseOrder().getPurchaseOrder(),
                item.getPurchaseOrderItem(),
                item.getMaterial(),
                item.getPlant(),
                item.getStorageLocation(),
                item.getOrderQuantity().stripTrailingZeros().toPlainString(),
                item.getPurchaseOrderQuantityUnit(),
                item.isCompletelyDelivered()
        );
    }
}
