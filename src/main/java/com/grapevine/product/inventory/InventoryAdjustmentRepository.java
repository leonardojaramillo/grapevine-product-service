package com.grapevine.product.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryAdjustmentRepository
        extends JpaRepository<InventoryAdjustment, Long> {
}