package com.kuchtoker.inventory_service.service;

import com.kuchtoker.inventory_service.dto.InventoryCheckRequest;
import com.kuchtoker.inventory_service.model.Inventory;
import com.kuchtoker.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public void checkAndDeductStock(List<InventoryCheckRequest> requests) {
        log.info("Inside InventoryService::checkAndDeductStock");

        // Validate stock availability
        for (InventoryCheckRequest request : requests) {
            Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode())
                    .orElseThrow(() -> new RuntimeException("SKU not found: " + request.getSkuCode()));

            log.info("inventory.getQuantity(): {} < request.getQuantity(): {}", inventory.getQuantity(), request.getQuantity());

            if (inventory.getQuantity() < request.getQuantity()) {
                throw new RuntimeException("Insufficient stock for SKU: " + request.getSkuCode());
            }
        }

        // Deduct stock
        for (InventoryCheckRequest request : requests) {
            Inventory inventory = inventoryRepository.findBySkuCode(request.getSkuCode()).get();
            inventory.setQuantity(inventory.getQuantity() - request.getQuantity());
            inventoryRepository.save(inventory);
        }
    }

    public void checkAndAddStock(List<InventoryCheckRequest> requests) {
        log.info("Inside InventoryService::checkAndAddStock");

        // Build map for fast lookup
        Map<String, Inventory> existingInventoryMap = inventoryRepository
                .findAllBySkuCodeIn(requests.stream()
                        .map(InventoryCheckRequest::getSkuCode)
                        .toList())
                .stream()
                .collect(Collectors.toMap(Inventory::getSkuCode, inv -> inv));

        List<Inventory> inventoriesToSave = new ArrayList<>();

        for (InventoryCheckRequest request : requests) {
            Inventory inventory = existingInventoryMap.get(request.getSkuCode());

            if (inventory != null) {
                log.info("Updating quantity for SKU: {} (existing: {}, adding: {})",
                        request.getSkuCode(), inventory.getQuantity(), request.getQuantity());
                inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
            } else {
                log.info("Creating new inventory entry for SKU: {} with quantity: {}",
                        request.getSkuCode(), request.getQuantity());
                inventory = new Inventory();
                inventory.setSkuCode(request.getSkuCode());
                inventory.setQuantity(request.getQuantity());
            }

            inventoriesToSave.add(inventory);
        }

        inventoryRepository.saveAll(inventoriesToSave);
    }

}
