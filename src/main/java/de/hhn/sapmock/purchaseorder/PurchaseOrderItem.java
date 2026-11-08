package de.hhn.sapmock.purchaseorder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

/**
 * Item of a purchase order (A_PurchaseOrderItem): one ordered material with its quantity.
 * The delivered quantity is raised by goods receipts (movement type 101) in the material document service.
 */
@Entity
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private PurchaseOrder purchaseOrder;

    /** Item number within the order; SAP counts in steps of 10: "10", "20", … */
    @Column(nullable = false, length = 5)
    private String purchaseOrderItem;

    @Column(nullable = false, length = 40)
    private String material;

    @Column(nullable = false, length = 4)
    private String plant;

    @Column(nullable = false, length = 4)
    private String storageLocation;

    @Column(nullable = false, precision = 13, scale = 3)
    private BigDecimal orderQuantity;

    @Column(nullable = false, length = 3)
    private String purchaseOrderQuantityUnit;

    /** Not an API field in SAP (there it comes from the PO history), but needed to know when the item is complete */
    @Column(nullable = false, precision = 13, scale = 3)
    private BigDecimal deliveredQuantity = BigDecimal.ZERO;

    protected PurchaseOrderItem() {
    }

    public PurchaseOrderItem(String purchaseOrderItem, String material, String plant, String storageLocation,
                             BigDecimal orderQuantity, String purchaseOrderQuantityUnit) {
        this.purchaseOrderItem = purchaseOrderItem;
        this.material = material;
        this.plant = plant;
        this.storageLocation = storageLocation;
        this.orderQuantity = orderQuantity;
        this.purchaseOrderQuantityUnit = purchaseOrderQuantityUnit;
    }

    void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    /** Quantity that may still be delivered; a goods receipt above this is rejected */
    public BigDecimal getOpenQuantity() {
        return orderQuantity.subtract(deliveredQuantity);
    }

    public boolean isCompletelyDelivered() {
        return getOpenQuantity().signum() <= 0;
    }

    public void addDeliveredQuantity(BigDecimal quantity) {
        this.deliveredQuantity = deliveredQuantity.add(quantity);
    }

    public Long getId() {
        return id;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public String getPurchaseOrderItem() {
        return purchaseOrderItem;
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

    public BigDecimal getOrderQuantity() {
        return orderQuantity;
    }

    public String getPurchaseOrderQuantityUnit() {
        return purchaseOrderQuantityUnit;
    }

    public BigDecimal getDeliveredQuantity() {
        return deliveredQuantity;
    }
}
