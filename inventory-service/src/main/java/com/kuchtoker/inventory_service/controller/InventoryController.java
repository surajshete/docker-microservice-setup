package com.kuchtoker.inventory_service.controller;

import com.kuchtoker.inventory_service.dto.InventoryCheckRequest;
import com.kuchtoker.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory/")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/check-and-deduct")
    public ResponseEntity<?> checkAndDeduct(@RequestBody List<InventoryCheckRequest> requests) {
        log.info("Received inventory checkAndDeduct request");

        try {
            inventoryService.checkAndDeductStock(requests);
            return ResponseEntity.ok(Map.of("success", true, "message", "Stock reserved"));
        } catch (RuntimeException e) {
            log.warn("Error during inventory check: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/check-and-add")
    public ResponseEntity<Map<String, Object>> checkAndAdd(@RequestBody List<InventoryCheckRequest> requests) {
        log.info("Received inventory checkAndAdd request with {} item(s)", requests.size());

        try {
            inventoryService.checkAndAddStock(requests);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Stock added/updated successfully"
            ));
        } catch (RuntimeException e) {
            log.warn("Error during inventory check and add: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }




}
