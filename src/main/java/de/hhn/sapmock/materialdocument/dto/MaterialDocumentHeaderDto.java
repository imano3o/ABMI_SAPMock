package de.hhn.sapmock.materialdocument.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.hhn.sapmock.materialdocument.MaterialDocument;
import de.hhn.sapmock.odata.ODataList;

import java.time.ZoneOffset;

/**
 * Belegkopf im SAP-Format (A_MaterialDocumentHeader) inklusive Positionen unter to_MaterialDocumentItem.
 * Wird für Request und Response verwendet; die mit "nur Response" markierten Felder setzt der Mock.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MaterialDocumentHeaderDto(
        @JsonProperty("MaterialDocumentYear") String materialDocumentYear,      // nur Response
        @JsonProperty("MaterialDocument") String materialDocument,              // nur Response
        @JsonProperty("PostingDate") String postingDate,                        // OData-Format "/Date(...)/"
        @JsonProperty("MaterialDocumentHeaderText") String materialDocumentHeaderText,
        @JsonProperty("GoodsMovementCode") String goodsMovementCode,            // "03" = Warenausgang
        @JsonProperty("to_MaterialDocumentItem") ODataList.Results<MaterialDocumentItemDto> toMaterialDocumentItem
) {

    /** Warenausgang; das einzige, was der Mock bucht (Bewegungsart 201) */
    private static final String GOODS_ISSUE = "03";

    public static MaterialDocumentHeaderDto from(MaterialDocument document) {
        long millis = document.getPostingDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        return new MaterialDocumentHeaderDto(
                document.getMaterialDocumentYear(),
                document.getMaterialDocument(),
                "/Date(" + millis + ")/",
                document.getHeaderText(),
                GOODS_ISSUE,
                new ODataList.Results<>(document.getItems().stream().map(MaterialDocumentItemDto::from).toList())
        );
    }
}
