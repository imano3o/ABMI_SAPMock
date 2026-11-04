package de.hhn.sapmock.materialdocument;

import de.hhn.sapmock.materialstock.MaterialStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Sql({"/reset.sql", "/data.sql"})
class MaterialDocumentControllerTest {

    private static final String URL = "/sap/opu/odata/sap/API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MaterialStockRepository stockRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void verbrauchBuchtBelegUndSenktBestand() throws Exception {
        post(document("Zi. 12, Fr. Müller", item("MED-ENOX40", "201", "2")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.d.MaterialDocument").value("4900000001"))
                .andExpect(jsonPath("$.d.MaterialDocumentHeaderText").value("Zi. 12, Fr. Müller"))
                .andExpect(jsonPath("$.d.GoodsMovementCode").value("03"))
                .andExpect(jsonPath("$.d.to_MaterialDocumentItem.results[0].MaterialDocumentItem").value("1"))
                .andExpect(jsonPath("$.d.to_MaterialDocumentItem.results[0].QuantityInEntryUnit").value("2"));

        assertThat(stock("MED-ENOX40")).isEqualByComparingTo("4");
    }

    @Test
    void belegnummernWerdenFortlaufendVergeben() throws Exception {
        post(document(null, item("MED-IBU400", "201", "1"))).andExpect(status().isCreated());
        post(document(null, item("MED-IBU400", "201", "1")))
                .andExpect(jsonPath("$.d.MaterialDocument").value("4900000002"));
    }

    @Test
    void zuLangerKopftextWirdAuf25ZeichenGekuerzt() throws Exception {
        post(document("Zimmer 12, Frau Müller-Lüdenscheidt", item("MED-IBU400", "201", "1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.d.MaterialDocumentHeaderText").value("Zimmer 12, Frau Müller-Lü"));
    }

    @Test
    void zuWenigBestandLiefertSapFehlerM7021() throws Exception {
        post(document(null, item("MED-ENOX40", "201", "100")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("M7/021"))
                .andExpect(jsonPath("$.error.message.lang").value("en"))
                .andExpect(jsonPath("$.error.message.value").value("Deficit of unrestricted-use stock 94 PC : MED-ENOX40 1710 ST03"));

        assertThat(stock("MED-ENOX40")).isEqualByComparingTo("6");
    }

    @Test
    void schlaegtEinePositionFehlWirdNichtsGebucht() throws Exception {
        // The first item would be valid, the second exceeds the stock: the whole document must fail
        post(document(null, item("MED-IBU400", "201", "10"), item("MED-ONDAN4", "201", "5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("M7/021"));

        assertThat(stock("MED-IBU400")).isEqualByComparingTo("240");
        assertThat(stock("MED-ONDAN4")).isEqualByComparingTo("3");
    }

    @Test
    void andereBewegungsartWirdAbgelehnt() throws Exception {
        post(document(null, item("MED-IBU400", "101", "1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MOVEMENT_TYPE"));
    }

    @Test
    void mengeNullWirdAbgelehnt() throws Exception {
        post(document(null, item("MED-IBU400", "201", "0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_QUANTITY"));
    }

    @Test
    void unbekanntesMaterialWirdAbgelehnt() throws Exception {
        post(document(null, item("MED-GIBTESNICHT", "201", "1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("STOCK_NOT_FOUND"));
    }

    @Test
    void belegOhnePositionenWirdAbgelehnt() throws Exception {
        post(document(null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("NO_ITEMS"));
    }

    @Test
    void buchungsdatumImODataFormatWirdUebernommen() throws Exception {
        // 1792108800000 ms = 15.10.2026, 00:00 UTC
        String body = """
                {"PostingDate": "/Date(1792108800000)/", "GoodsMovementCode": "03",
                 "to_MaterialDocumentItem": {"results": [%s]}}
                """.formatted(item("MED-IBU400", "201", "1"));
        post(body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.d.PostingDate").value("/Date(1792108800000)/"))
                .andExpect(jsonPath("$.d.MaterialDocumentYear").value("2026"));
    }

    private ResultActions post(String body) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private BigDecimal stock(String material) {
        return stockRepository.findByMaterialAndPlantAndStorageLocation(material, "1710", "ST03")
                .orElseThrow()
                .getQuantity();
    }

    private static String document(String headerText, String... items) {
        String text = headerText == null ? "" : "\"MaterialDocumentHeaderText\": \"" + headerText + "\", ";
        return "{" + text + "\"GoodsMovementCode\": \"03\", \"to_MaterialDocumentItem\": {\"results\": ["
                + String.join(",", items) + "]}}";
    }

    private static String item(String material, String movementType, String quantity) {
        return """
                {"Material": "%s", "Plant": "1710", "StorageLocation": "ST03", "GoodsMovementType": "%s",
                 "QuantityInEntryUnit": "%s", "EntryUnit": "PC", "CostCenter": "17101301"}
                """.formatted(material, movementType, quantity);
    }
}
