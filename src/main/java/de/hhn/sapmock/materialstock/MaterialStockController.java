package de.hhn.sapmock.materialstock;

import de.hhn.sapmock.materialstock.dto.MatlStkInAcctMod;
import de.hhn.sapmock.odata.ODataList;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulates API_MATERIAL_STOCK_SRV (read-only, like the original).
 * OData query options such as $filter or $top are ignored; all stock entries are always returned,
 * sorted by storage location (ward) and material.
 * Example: GET /sap/opu/odata/sap/API_MATERIAL_STOCK_SRV/A_MatlStkInAcctMod
 */
@RestController
@RequestMapping("/sap/opu/odata/sap/API_MATERIAL_STOCK_SRV")
public class MaterialStockController {

    private final MaterialStockRepository repository;

    public MaterialStockController(MaterialStockRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/A_MatlStkInAcctMod")
    public ODataList<MatlStkInAcctMod> getStock() {
        return ODataList.of(repository.findAll(Sort.by("storageLocation", "material")).stream()
                .map(MatlStkInAcctMod::from)
                .toList());
    }
}
