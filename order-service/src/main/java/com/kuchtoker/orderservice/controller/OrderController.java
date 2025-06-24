package com.kuchtoker.orderservice.controller;

import com.kuchtoker.orderservice.dto.OrderRequest;
import com.kuchtoker.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @TimeLimiter(name = "inventory")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    public CompletableFuture<ResponseEntity<String>> placeOrder(@RequestBody OrderRequest orderRequest) {
        return CompletableFuture.supplyAsync(() -> {
            String message = orderService.placeOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        });
    }

    public CompletableFuture<ResponseEntity<String>> fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
        log.error("Fallback triggered due to: {}", runtimeException.getMessage(), runtimeException);
        String fallbackMessage = "Fallback: Unable to place order right now, " + runtimeException.getMessage() + ", Please try again.";
        return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fallbackMessage));
    }
}
