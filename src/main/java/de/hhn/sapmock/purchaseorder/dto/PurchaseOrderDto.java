package de.hhn.sapmock.purchaseorder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.hhn.sapmock.odata.ODataList;
import de.hhn.sapmock.purchaseorder.PurchaseOrder;

import java.time.ZoneOffset;

/**
 * Purchase order header in SAP format (A_PurchaseOrder), including the items under to_PurchaseOrderItem.
 * Used for request and response; the fields marked "response only" are set by the mock.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PurchaseOrderDto(
        @JsonProperty("PurchaseOrder") String purchaseOrder,                    // response only
        @JsonProperty("CompanyCode") String companyCode,
        @JsonProperty("PurchaseOrderType") String purchaseOrderType,            // "NB" = standard order
        @JsonProperty("Supplier") String supplier,
        @JsonProperty("PurchasingOrganization") String purchasingOrganization,
        @JsonProperty("PurchasingGroup") String purchasingGroup,
        @JsonProperty("PurchaseOrderDate") String purchaseOrderDate,            // response only, "/Date(...)/"
        @JsonProperty("to_PurchaseOrderItem") ODataList.Results<PurchaseOrderItemDto> toPurchaseOrderItem
) {

    public static PurchaseOrderDto from(PurchaseOrder order) {
        long millis = order.getPurchaseOrderDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        return new PurchaseOrderDto(
                order.getPurchaseOrder(),
                order.getCompanyCode(),
                order.getPurchaseOrderType(),
                order.getSupplier(),
                order.getPurchasingOrganization(),
                order.getPurchasingGroup(),
                "/Date(" + millis + ")/",
                new ODataList.Results<>(order.getItems().stream().map(PurchaseOrderItemDto::from).toList())
        );
    }
}
