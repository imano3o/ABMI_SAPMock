package de.hhn.sapmock.materialstock.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hhn.sapmock.materialstock.MaterialStock;

/**
 * Entity A_MatlStkInAcctMod from API_MATERIAL_STOCK_SRV.
 * Field names match the SAP signature 1:1; quantities are sent as strings, as in OData v2.
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
