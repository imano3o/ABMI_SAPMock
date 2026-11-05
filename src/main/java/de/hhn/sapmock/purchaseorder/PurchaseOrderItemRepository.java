package de.hhn.sapmock.purchaseorder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    /** A goods receipt references the order by number and item number, as in SAP */
    Optional<PurchaseOrderItem> findByPurchaseOrder_PurchaseOrderAndPurchaseOrderItem(String purchaseOrder, String purchaseOrderItem);
}
