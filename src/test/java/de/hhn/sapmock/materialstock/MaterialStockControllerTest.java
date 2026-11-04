package de.hhn.sapmock.materialstock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Sql({"/reset.sql", "/data.sql"})
class MaterialStockControllerTest {

    private static final String URL = "/sap/opu/odata/sap/API_MATERIAL_STOCK_SRV/A_MatlStkInAcctMod";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void liefertAlleBestaendeImODataFormat() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.d.results", hasSize(23)));
    }

    @Test
    void liefertBestaendeNachLagerortUndMaterialSortiertMitSapFeldnamen() throws Exception {
        // Sortiert nach Lagerort, dann Material: erster Eintrag ist MED-AMOXI1000 auf ST01; Mengen kommen wie bei SAP als String
        mockMvc.perform(get(URL))
                .andExpect(jsonPath("$.d.results[0].Material").value("MED-AMOXI1000"))
                .andExpect(jsonPath("$.d.results[0].Plant").value("1710"))
                .andExpect(jsonPath("$.d.results[0].StorageLocation").value("ST01"))
                .andExpect(jsonPath("$.d.results[0].MaterialBaseUnit").value("PC"))
                .andExpect(jsonPath("$.d.results[0].MatlWrhsStkQtyInMatlBaseUnit").value("32"));
    }
}
