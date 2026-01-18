package com.inventory.repository;

import com.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    boolean existsByProductSkuAndPurchaseDate(String productSku, LocalDate purchaseDate);
}
