package de.hhn.sapmock.materialstock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

/**
 * Frei verwendbarer Lagerbestand eines Materials an einem Lagerort.
 * Wird von Material Stock gelesen und von Material Document verändert.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"material", "plant", "storage_location"}))
public class MaterialStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String material;

    @Column(nullable = false, length = 4)
    private String plant;

    @Column(name = "storage_location", nullable = false, length = 4)
    private String storageLocation;

    @Column(nullable = false, length = 3)
    private String baseUnit;

    @Column(nullable = false, precision = 13, scale = 3)
    private BigDecimal quantity;

    protected MaterialStock() {
    }

    public MaterialStock(String material, String plant, String storageLocation, String baseUnit, BigDecimal quantity) {
        this.material = material;
        this.plant = plant;
        this.storageLocation = storageLocation;
        this.baseUnit = baseUnit;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
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

    public String getBaseUnit() {
        return baseUnit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
