package com.kuchtoker.inventory_service.repository;

import com.kuchtoker.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findBySkuCodeIn(List<String> skuCode);

    Optional<Inventory> findBySkuCode(String skuCode);

    List<Inventory> findAllBySkuCodeIn(List<String> list);
}
