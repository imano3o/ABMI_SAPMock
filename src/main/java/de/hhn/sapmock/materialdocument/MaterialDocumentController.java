package de.hhn.sapmock.materialdocument;

import de.hhn.sapmock.materialdocument.dto.MaterialDocumentHeaderDto;
import de.hhn.sapmock.odata.ODataEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simuliert API_MATERIAL_DOCUMENT_SRV.
 * Beispiel: POST /sap/opu/odata/sap/API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader
 */
@RestController
@RequestMapping("/sap/opu/odata/sap/API_MATERIAL_DOCUMENT_SRV")
public class MaterialDocumentController {

    private final MaterialDocumentService service;

    public MaterialDocumentController(MaterialDocumentService service) {
        this.service = service;
    }

    @PostMapping("/A_MaterialDocumentHeader")
    public ResponseEntity<ODataEntity<MaterialDocumentHeaderDto>> create(@RequestBody MaterialDocumentHeaderDto request) {
        MaterialDocument created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ODataEntity.of(MaterialDocumentHeaderDto.from(created)));
    }
}
