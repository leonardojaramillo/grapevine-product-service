package com.grapevine.product.inventory;

import com.grapevine.product.inventory.dto.AdjustStockRequest;
import com.grapevine.product.inventory.dto.InventoryAdjustmentResponse;
import com.grapevine.product.product.Product;
import com.grapevine.product.product.ProductRepository;
import com.grapevine.product.warehouse.Warehouse;
import com.grapevine.product.warehouse.WarehouseRepository;
import com.grapevine.product.warehouse_stock.WarehouseStock;
import com.grapevine.product.warehouse_stock.WarehouseStockRepository;
import com.grapevine.product.warehouse_stock.WarehouseStockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryAdjustmentRepository adjustmentRepository;
    private final ProductRepository             productRepository;
    private final WarehouseRepository           warehouseRepository;
    private final WarehouseStockRepository      warehouseStockRepository;

    public List<InventoryAdjustmentResponse> findAll() {
        return adjustmentRepository.findAll().stream()
                .map(a -> InventoryAdjustmentResponse.builder()
                        .id(a.getId())
                        .productName(a.getProduct().getName())
                        .warehouseName(a.getWarehouse() != null ? a.getWarehouse().getName() : "—")
                        .previousStock(a.getPreviousStock())
                        .newStock(a.getNewStock())
                        .reason(a.getReason())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }

    public List<InventoryAdjustmentResponse> findByWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));
        return adjustmentRepository.findAll().stream()
                .filter(a -> a.getWarehouse() != null
                        && a.getWarehouse().getId().equals(warehouse.getId()))
                .map(a -> InventoryAdjustmentResponse.builder()
                        .id(a.getId())
                        .productName(a.getProduct().getName())
                        .warehouseName(warehouse.getName())
                        .previousStock(a.getPreviousStock())
                        .newStock(a.getNewStock())
                        .reason(a.getReason())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }

    public List<WarehouseStockResponse> getStockByWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));
        return warehouseStockRepository.findByWarehouse(warehouse).stream()
                .map(ws -> WarehouseStockResponse.builder()
                        .warehouseStockId(ws.getId())
                        .warehouseId(ws.getWarehouse().getId())
                        .warehouseName(ws.getWarehouse().getName())
                        .productId(ws.getProduct().getId())
                        .productName(ws.getProduct().getName())
                        .productCategory(ws.getProduct().getCategory() != null
                                ? ws.getProduct().getCategory().name() : null)
                        .stock(ws.getStock())
                        .build())
                .toList();
    }

    public List<WarehouseStockResponse> getAllStock() {
        return warehouseStockRepository.findAll().stream()
                .map(ws -> WarehouseStockResponse.builder()
                        .warehouseStockId(ws.getId())
                        .warehouseId(ws.getWarehouse().getId())
                        .warehouseName(ws.getWarehouse().getName())
                        .productId(ws.getProduct().getId())
                        .productName(ws.getProduct().getName())
                        .productCategory(ws.getProduct().getCategory() != null
                                ? ws.getProduct().getCategory().name() : null)
                        .stock(ws.getStock())
                        .build())
                .toList();
    }

    public InventoryAdjustmentResponse adjust(AdjustStockRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));

        WarehouseStock ws = warehouseStockRepository
                .findByWarehouseAndProduct(warehouse, product)
                .orElse(WarehouseStock.builder()
                        .warehouse(warehouse)
                        .product(product)
                        .stock(0)
                        .build());

        Integer previousStock = ws.getStock();
        ws.setStock(request.getNewStock());
        warehouseStockRepository.save(ws);

        // Recalcular stock global en products
        Integer totalStock = warehouseStockRepository.sumStockByProduct(product);
        product.setStock(totalStock);
        productRepository.save(product);

        InventoryAdjustment adjustment = InventoryAdjustment.builder()
                .product(product)
                .warehouse(warehouse)
                .previousStock(previousStock)
                .newStock(request.getNewStock())
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .build();

        InventoryAdjustment saved = adjustmentRepository.save(adjustment);

        return InventoryAdjustmentResponse.builder()
                .id(saved.getId())
                .productName(product.getName())
                .warehouseName(warehouse.getName())
                .previousStock(previousStock)
                .newStock(request.getNewStock())
                .reason(request.getReason())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}