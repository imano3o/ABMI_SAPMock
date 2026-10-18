package de.hhn.sapmock.materialdocument;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class MaterialDocumentItem {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private MaterialDocument materialDocument;

    private String materialDocumentItem;
    private String material;
    private String plant;
    private String storageLocation;
    private String goodsMovementType;
    private BigDecimal quantity;
    private String entryUnit;
    private String costCenter;
    private String purchaseOrder;
    private String purchaseOrderItem;
    private String debitCreditCode;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
