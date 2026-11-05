package de.hhn.sapmock.purchaseorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Sql({"/reset.sql", "/data.sql"})
class PurchaseOrderControllerTest {

    private static final String URL = "/sap/opu/odata/sap/API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void bestellungWirdAngelegtUndNummeriert() throws Exception {
        create(order("17300001", "MED-ENOX40", "50"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.d.PurchaseOrder").value("4500000001"))
                .andExpect(jsonPath("$.d.PurchaseOrderType").value("NB"))
                .andExpect(jsonPath("$.d.to_PurchaseOrderItem.results[0].PurchaseOrderItem").value("10"))
                .andExpect(jsonPath("$.d.to_PurchaseOrderItem.results[0].OrderQuantity").value("50"))
                .andExpect(jsonPath("$.d.to_PurchaseOrderItem.results[0].PurchaseOrderQuantityUnit").value("PC"))
                .andExpect(jsonPath("$.d.to_PurchaseOrderItem.results[0].IsCompletelyDelivered").value(false));

        create(order("17300001", "MED-ONDAN4", "20"))
                .andExpect(jsonPath("$.d.PurchaseOrder").value("4500000002"));
    }

    @Test
    void getLiefertAlleBestellungenMitPositionen() throws Exception {
        create(order("17300001", "MED-ENOX40", "50"));
        create(order("17300001", "MED-ONDAN4", "20"));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.d.results", hasSize(2)))
                .andExpect(jsonPath("$.d.results[1].to_PurchaseOrderItem.results[0].Material").value("MED-ONDAN4"));
    }

    @Test
    void ohneLieferantWirdAbgelehnt() throws Exception {
        create(order(null, "MED-ENOX40", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ME/083"));
    }

    @Test
    void unbekanntesMaterialWirdAbgelehnt() throws Exception {
        create(order("17300001", "MED-GIBTESNICHT", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("ME/062"));
    }

    @Test
    void mengeNullWirdAbgelehnt() throws Exception {
        create(order("17300001", "MED-ENOX40", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_QUANTITY"));
    }

    private ResultActions create(String body) throws Exception {
        return mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private static String order(String supplier, String material, String quantity) {
        String supplierField = supplier == null ? "" : "\"Supplier\": \"" + supplier + "\", ";
        return """
                {%s"PurchasingOrganization": "1710", "to_PurchaseOrderItem": {"results": [
                  {"Material": "%s", "Plant": "1710", "StorageLocation": "ST03", "OrderQuantity": "%s"}]}}
                """.formatted(supplierField, material, quantity);
    }
}
