package de.hhn.sapmock.materialdocument.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.hhn.sapmock.materialdocument.MaterialDocument;
import de.hhn.sapmock.odata.ODataList;

import java.time.ZoneOffset;

/**
 * Document header in SAP format (A_MaterialDocumentHeader), including the items under to_MaterialDocumentItem.
 * Used for request and response; the fields marked "response only" are set by the mock.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MaterialDocumentHeaderDto(
        @JsonProperty("MaterialDocumentYear") String materialDocumentYear,      // response only
        @JsonProperty("MaterialDocument") String materialDocument,              // response only
        @JsonProperty("PostingDate") String postingDate,                        // OData format "/Date(...)/"
        @JsonProperty("MaterialDocumentHeaderText") String materialDocumentHeaderText,
        @JsonProperty("GoodsMovementCode") String goodsMovementCode,            // "03" = goods issue, "01" = goods receipt
        @JsonProperty("to_MaterialDocumentItem") ODataList.Results<MaterialDocumentItemDto> toMaterialDocumentItem
) {

    public static MaterialDocumentHeaderDto from(MaterialDocument document) {
        long millis = document.getPostingDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        return new MaterialDocumentHeaderDto(
                document.getMaterialDocumentYear(),
                document.getMaterialDocument(),
                "/Date(" + millis + ")/",
                document.getHeaderText(),
                document.getGoodsMovementCode(),
                new ODataList.Results<>(document.getItems().stream().map(MaterialDocumentItemDto::from).toList())
        );
    }
}
