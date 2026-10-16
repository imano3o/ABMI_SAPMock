package de.hhn.sapmock.materialstock;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialStockRepository extends JpaRepository<MaterialStock, Long> {

    Optional<MaterialStock> findByMaterialAndPlantAndStorageLocation(String material, String plant, String storageLocation);
}
