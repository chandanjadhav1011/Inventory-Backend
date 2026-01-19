package com.inventory.repository;

import com.inventory.entity.Inventory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    boolean existsByProductSkuAndPurchaseDate(String productSku, LocalDate purchaseDate);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE inventory", nativeQuery = true)
    void truncateInventory();

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE inventory ALTER COLUMN id RESTART WITH 1", nativeQuery = true)
    void resetInventoryIdentity();
}
