package de.hhn.sapmock.purchaseorder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Header of a purchase order (A_PurchaseOrder).
 * Holds who is ordered from; what is ordered is in the items.
 */
@Entity
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Order number, e.g. "4500000001"; SAP uses the 45 number range for standard orders */
    @Column(nullable = false, length = 10)
    private String purchaseOrder;

    @Column(nullable = false, length = 4)
    private String companyCode;

    /** "NB" = standard purchase order */
    @Column(nullable = false, length = 4)
    private String purchaseOrderType;

    @Column(nullable = false, length = 10)
    private String supplier;

    @Column(nullable = false, length = 4)
    private String purchasingOrganization;

    @Column(length = 3)
    private String purchasingGroup;

    @Column(nullable = false)
    private LocalDate purchaseOrderDate;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("purchaseOrderItem")
    private List<PurchaseOrderItem> items = new ArrayList<>();

    protected PurchaseOrder() {
    }

    public PurchaseOrder(String purchaseOrder, String companyCode, String purchaseOrderType, String supplier,
                         String purchasingOrganization, String purchasingGroup, LocalDate purchaseOrderDate) {
        this.purchaseOrder = purchaseOrder;
        this.companyCode = companyCode;
        this.purchaseOrderType = purchaseOrderType;
        this.supplier = supplier;
        this.purchasingOrganization = purchasingOrganization;
        this.purchasingGroup = purchasingGroup;
        this.purchaseOrderDate = purchaseOrderDate;
    }

    public void addItem(PurchaseOrderItem item) {
        item.setPurchaseOrder(this);
        items.add(item);
    }

    public Long getId() {
        return id;
    }

    public String getPurchaseOrder() {
        return purchaseOrder;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public String getPurchaseOrderType() {
        return purchaseOrderType;
    }

    public String getSupplier() {
        return supplier;
    }

    public String getPurchasingOrganization() {
        return purchasingOrganization;
    }

    public String getPurchasingGroup() {
        return purchasingGroup;
    }

    public LocalDate getPurchaseOrderDate() {
        return purchaseOrderDate;
    }

    public List<PurchaseOrderItem> getItems() {
        return items;
    }
}
