package com.grapevine.product.warehouse;

import com.grapevine.product.warehouse.dto.CreateWarehouseRequest;
import com.grapevine.product.warehouse.dto.UpdateWarehouseRequest;
import com.grapevine.product.warehouse.dto.WarehouseResponse;
import com.grapevine.product.warehouse_stock.WarehouseStockRepository;
import com.grapevine.product.warehouse_stock.WarehouseStockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository     warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    public WarehouseResponse create(CreateWarehouseRequest request) {
        Warehouse w = Warehouse.builder()
                .name(request.getName())
                .address(request.getAddress())
                .ubigeoCode(request.getUbigeoCode())
                .department(request.getDepartment())
                .province(request.getProvince())
                .district(request.getDistrict())
                .active(true)
                .build();
        Warehouse saved = warehouseRepository.save(w);

        return toResponse(saved);
    }

    public List<WarehouseResponse> findAll() {
        return warehouseRepository.findAll().stream().map(this::toResponse).toList();
    }

    public WarehouseResponse update(Long id, UpdateWarehouseRequest request) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));
        if (request.getName()       != null) w.setName(request.getName());
        if (request.getAddress()    != null) w.setAddress(request.getAddress());
        if (request.getUbigeoCode() != null) w.setUbigeoCode(request.getUbigeoCode());
        if (request.getDepartment() != null) w.setDepartment(request.getDepartment());
        if (request.getProvince()   != null) w.setProvince(request.getProvince());
        if (request.getDistrict()   != null) w.setDistrict(request.getDistrict());

        Warehouse saved = warehouseRepository.save(w);

        return toResponse(saved);
    }

    public WarehouseResponse toggleActive(Long id) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));
        w.setActive(!w.getActive());
        Warehouse saved = warehouseRepository.save(w);


        return toResponse(saved);
    }

    public List<WarehouseStockResponse> getStock(Long id) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));
        return warehouseStockRepository.findByWarehouse(w).stream()
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

    private WarehouseResponse toResponse(Warehouse w) {
        return WarehouseResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .address(w.getAddress())
                .active(w.getActive())
                .ubigeoCode(w.getUbigeoCode())
                .department(w.getDepartment())
                .province(w.getProvince())
                .district(w.getDistrict())
                .build();
    }
}