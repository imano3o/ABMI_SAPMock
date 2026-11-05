package de.hhn.sapmock.purchaseorder;

import de.hhn.sapmock.odata.ODataEntity;
import de.hhn.sapmock.odata.ODataList;
import de.hhn.sapmock.purchaseorder.dto.PurchaseOrderDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulates API_PURCHASEORDER_PROCESS_SRV.
 * As with Material Stock, OData query options like $filter or $expand are ignored:
 * GET always returns all orders including their items.
 * Example: POST /sap/opu/odata/sap/API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder
 */
@RestController
@RequestMapping("/sap/opu/odata/sap/API_PURCHASEORDER_PROCESS_SRV")
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    public PurchaseOrderController(PurchaseOrderService service) {
        this.service = service;
    }

    @GetMapping("/A_PurchaseOrder")
    public ODataList<PurchaseOrderDto> getAll() {
        return ODataList.of(service.findAll().stream().map(PurchaseOrderDto::from).toList());
    }

    @PostMapping("/A_PurchaseOrder")
    public ResponseEntity<ODataEntity<PurchaseOrderDto>> create(@RequestBody PurchaseOrderDto request) {
        PurchaseOrder created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ODataEntity.of(PurchaseOrderDto.from(created)));
    }
}
