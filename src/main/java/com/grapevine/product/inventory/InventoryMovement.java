package com.grapevine.product.inventory;

import com.grapevine.product.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Product product;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    private Integer quantity;

    private String referenceType;

    private Long referenceId;

    private LocalDateTime createdAt;
}