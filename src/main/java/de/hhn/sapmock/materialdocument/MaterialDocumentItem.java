package de.hhn.sapmock.materialdocument;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

/**
 * Item of a material document (A_MaterialDocumentItem): one posted material with its quantity.
 */
@Entity
public class MaterialDocumentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private MaterialDocument materialDocument;

    /** Item number within the document: "1", "2", … */
    @Column(nullable = false, length = 4)
    private String materialDocumentItem;

    @Column(nullable = false, length = 40)
    private String material;

    @Column(nullable = false, length = 4)
    private String plant;

    @Column(nullable = false, length = 4)
    private String storageLocation;

    /** Movement type: "201" = consumption for cost center, "101" = goods receipt for a purchase order */
    @Column(nullable = false, length = 3)
    private String goodsMovementType;

    @Column(nullable = false, precision = 13, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, length = 3)
    private String entryUnit;

    /** Cost center of the consuming ward */
    @Column(length = 10)
    private String costCenter;

    /** Only set for goods receipts: the order and item that were delivered */
    @Column(length = 10)
    private String purchaseOrder;

    @Column(length = 5)
    private String purchaseOrderItem;

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

    public MaterialDocumentItem(String materialDocumentItem, String material, String plant, String storageLocation,
                                String goodsMovementType, BigDecimal quantity, String entryUnit, String costCenter,
                                String purchaseOrder, String purchaseOrderItem) {
        this(materialDocumentItem, material, plant, storageLocation, goodsMovementType, quantity, entryUnit, costCenter);
        this.purchaseOrder = purchaseOrder;
        this.purchaseOrderItem = purchaseOrderItem;
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

    public String getPurchaseOrder() {
        return purchaseOrder;
    }

    public String getPurchaseOrderItem() {
        return purchaseOrderItem;
    }
}
