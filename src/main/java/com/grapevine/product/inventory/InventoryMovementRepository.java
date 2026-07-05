package com.grapevine.product.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, Long> {
}