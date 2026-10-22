package de.hhn.sapmock.materialdocument;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

/**
 * Position eines Materialbelegs (A_MaterialDocumentItem): ein gebuchtes Material mit Menge.
 */
@Entity
public class MaterialDocumentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private MaterialDocument materialDocument;

    /** Positionsnummer innerhalb des Belegs: "1", "2", … */
    @Column(nullable = false, length = 4)
    private String materialDocumentItem;

    @Column(nullable = false, length = 40)
    private String material;

    @Column(nullable = false, length = 4)
    private String plant;

    @Column(nullable = false, length = 4)
    private String storageLocation;

    /** Bewegungsart, aktuell nur "201" = Verbrauch an Kostenstelle */
    @Column(nullable = false, length = 3)
    private String goodsMovementType;

    @Column(nullable = false, precision = 13, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, length = 3)
    private String entryUnit;

    /** Kostenstelle der verbrauchenden Station */
    @Column(length = 10)
    private String costCenter;

    protected MaterialDocumentItem() {
    }

    public MaterialDocumentItem(String materialDocumentItem, String material, String plant, String storageLocation,
                                String goodsMovementType, BigDecimal quantity, String entryUnit, String costCenter) {
        this.materialDocumentItem = materialDocumentItem;
        this.material = material;
        this.plant = plant;
        this.storageLocation = storageLocation;
        this.goodsMovementType = goodsMovementType;
        this.quantity = quantity;
        this.entryUnit = entryUnit;
        this.costCenter = costCenter;
    }

    void setMaterialDocument(MaterialDocument materialDocument) {
        this.materialDocument = materialDocument;
    }

    public Long getId() {
        return id;
    }

    public MaterialDocument getMaterialDocument() {
        return materialDocument;
    }

    public String getMaterialDocumentItem() {
        return materialDocumentItem;
    }

    public String getMaterial() {
        return material;
    }

    public String getPlant() {
        return plant;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public String getGoodsMovementType() {
        return goodsMovementType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getEntryUnit() {
        return entryUnit;
    }

    public String getCostCenter() {
        return costCenter;
    }
}
