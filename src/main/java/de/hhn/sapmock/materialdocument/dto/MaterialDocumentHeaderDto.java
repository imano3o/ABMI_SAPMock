package de.hhn.sapmock.materialdocument.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hhn.sapmock.odata.ODataList;

public record MaterialDocumentHeaderDto(
        @JsonProperty("MaterialDocumentYear") String materialDocumentYear,      // nur Response
        @JsonProperty("MaterialDocument") String materialDocument,              // nur Response
        @JsonProperty("PostingDate") String postingDate,
        @JsonProperty("MaterialDocumentHeaderText") String materialDocumentHeaderText,
        @JsonProperty("GoodsMovementCode") String goodsMovementCode,
        @JsonProperty("to_MaterialDocumentItem") ODataList.Results<MaterialDocumentItemDto> toMaterialDocumentItem
) {}