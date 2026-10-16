package de.hhn.sapmock.materialstock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hhn.sapmock.materialstock.MaterialStock;

/**
 * Entität A_MatlStkInAcctMod aus API_MATERIAL_STOCK_SRV.
 * Enthält nur die Felder, die unser Anwendungsfall braucht (entspricht einer SAP-Abfrage mit $select).
 * Feldnamen entsprechen 1:1 der SAP-Signatur; Mengen werden wie in OData v2 als String übertragen.
 */
public record MatlStkInAcctMod(
        @JsonProperty("Material") String material,
        @JsonProperty("Plant") String plant,
        @JsonProperty("StorageLocation") String storageLocation,
        @JsonProperty("MaterialBaseUnit") String materialBaseUnit,
        @JsonProperty("MatlWrhsStkQtyInMatlBaseUnit") String matlWrhsStkQtyInMatlBaseUnit
) {

    public static MatlStkInAcctMod from(MaterialStock stock) {
        return new MatlStkInAcctMod(
                stock.getMaterial(),
                stock.getPlant(),
                stock.getStorageLocation(),
                stock.getBaseUnit(),
                stock.getQuantity().stripTrailingZeros().toPlainString()
        );
    }
}
