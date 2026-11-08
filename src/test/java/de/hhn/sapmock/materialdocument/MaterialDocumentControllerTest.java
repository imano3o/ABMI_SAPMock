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
    private static final String ORDER_URL = "/sap/opu/odata/sap/API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder";

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
        post(document(null, item("MED-IBU400", "311", "1")))
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

    @Test
    void wareneingangErhoehtBestandUndSchliesstBestellung() throws Exception {
        String order = createOrder("MED-ENOX40", "50");

        mockMvc.perform(MockMvcRequestBuilders.post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(goodsReceipt(order, "MED-ENOX40", "50")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.d.GoodsMovementCode").value("01"))
                .andExpect(jsonPath("$.d.to_MaterialDocumentItem.results[0].GoodsMovementType").value("101"))
                .andExpect(jsonPath("$.d.to_MaterialDocumentItem.results[0].PurchaseOrder").value(order));

        assertThat(stock("MED-ENOX40")).isEqualByComparingTo("56");
        mockMvc.perform(MockMvcRequestBuilders.get(ORDER_URL))
                .andExpect(jsonPath("$.d.results[0].to_PurchaseOrderItem.results[0].IsCompletelyDelivered").value(true));
    }

    @Test
    void teillieferungIstErlaubtAberNichtMehrAlsBestellt() throws Exception {
        String order = createOrder("MED-ENOX40", "50");

        post(goodsReceipt(order, "MED-ENOX40", "30")).andExpect(status().isCreated());
        post(goodsReceipt(order, "MED-ENOX40", "30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("M7/022"));

        assertThat(stock("MED-ENOX40")).isEqualByComparingTo("36");
    }

    @Test
    void wareneingangOhneBestellungWirdAbgelehnt() throws Exception {
        post(document(null, item("MED-ENOX40", "101", "5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("M7/146"));
    }

    @Test
    void wareneingangFuerAnderesMaterialWirdAbgelehnt() throws Exception {
        String order = createOrder("MED-ENOX40", "50");

        post(goodsReceipt(order, "MED-IBU400", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("PO_MISMATCH"));
    }

    private String createOrder(String material, String quantity) throws Exception {
        String body = """
                {"Supplier": "17300001", "to_PurchaseOrderItem": {"results": [
                  {"Material": "%s", "Plant": "1710", "StorageLocation": "ST03", "OrderQuantity": "%s"}]}}
                """.formatted(material, quantity);
        String response = mockMvc.perform(MockMvcRequestBuilders.post(ORDER_URL)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(response, "$.d.PurchaseOrder");
    }

    private static String goodsReceipt(String purchaseOrder, String material, String quantity) {
        return """
                {"GoodsMovementCode": "01", "to_MaterialDocumentItem": {"results": [
                  {"Material": "%s", "Plant": "1710", "StorageLocation": "ST03", "GoodsMovementType": "101",
                   "QuantityInEntryUnit": "%s", "PurchaseOrder": "%s", "PurchaseOrderItem": "10"}]}}
                """.formatted(material, quantity, purchaseOrder);
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
