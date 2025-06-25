package com.kuchtoker.orderservice.service;

import com.kuchtoker.orderservice.exception.CustomException;
import com.kuchtoker.orderservice.dto.*;
import com.kuchtoker.orderservice.event.OrderPlacedEvent;
import com.kuchtoker.orderservice.model.Order;
import com.kuchtoker.orderservice.model.OrderLineItem;
import com.kuchtoker.orderservice.repository.OrderRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<String> missingProducts = null;
        Map<String, BigDecimal> productPriceMap = null;
        BigDecimal totalPrice = null;

        List<OrderLineItem> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderLineItemList(orderLineItems);

        List<String> skuCodes = orderLineItems.stream()
                .map(OrderLineItem::getSkuCode)
                .toList();

        // Create InventoryCheckRequest list
        List<InventoryCheckRequest> inventoryCheckList = orderLineItems.stream()
                .map(item -> new InventoryCheckRequest(item.getSkuCode(), item.getQuantity()))
                .toList();

        boolean allProductsExist = false;

        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");
        boolean isProductExistsInMongoDB = false;

        try {
            log.info("Calling product exists Service for list of {}",skuCodes);
            List<ProductExistsResponse> productResponses = webClientBuilder.build()
                    .get()
                    .uri("http://product-service/api/product/exists", uriBuilder ->
                            uriBuilder.queryParam("name", skuCodes).build())
                    .retrieve()
                    .bodyToFlux(ProductExistsResponse.class)
                    .collectList()
                    .block();

            allProductsExist = productResponses != null &&
                    productResponses.stream().allMatch(ProductExistsResponse::isPresent);
            log.info("product exists Service result {}", allProductsExist);
            if(!allProductsExist) {
                // Identify missing products
                missingProducts = productResponses.stream()
                        .filter(resp -> !resp.isPresent())
                        .map(ProductExistsResponse::getName)
                        .toList();
            }
             productPriceMap = productResponses.stream()
                    .filter(ProductExistsResponse::isPresent)
                    .collect(Collectors.toMap(ProductExistsResponse::getName, ProductExistsResponse::getPrice));
        } catch (Exception e) {
            log.warn("Failed to call product-service: {}", e.getMessage());
        }

        if (allProductsExist) {
            try (Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start())) {

	            if( productPriceMap != null){
                    // Update price for each orderLineItem
                    Map<String, BigDecimal> finalProductPriceMap = productPriceMap;
                    orderLineItems.forEach(item -> {
                        if (finalProductPriceMap.containsKey(item.getSkuCode())) {
                            item.setPrice(finalProductPriceMap.get(item.getSkuCode()));
                        }
                    });

                    // Calculate total order price
                     totalPrice = orderLineItems.stream()
                            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    log.info("Total price of order: {}", totalPrice);
                }

                log.info("Calling inventory Service to check and deduct with inventoryCheckList: {}",inventoryCheckList);
                InventoryResponse inventoryResponse = webClientBuilder.build()
                        .post()
                        .uri("http://inventory-service/api/inventory/check-and-deduct")
                        .bodyValue(inventoryCheckList)
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                response -> response.bodyToMono(String.class)
                                        .flatMap(errorMessage -> {
                                            log.error("Inventory service error: {}", errorMessage);
                                            return Mono.error(new CustomException(errorMessage));
                                        })
                        )
                        .bodyToMono(InventoryResponse.class)
                        .block();


                if (inventoryResponse != null && inventoryResponse.isSuccess()) {
                    orderRepository.save(order);
                    kafkaTemplate.send("orderTopic", new OrderPlacedEvent(order));
                    return String.format("Order [%s] placed successfully. Total amount to pay: ₹%.2f",
                            order.getOrderNumber(), totalPrice);
                } else {
                    throw new IllegalArgumentException("Insufficient stock");
                }

            } finally {
                inventoryServiceLookup.end();
            }
        } else {
            log.warn("The following products are missing in product-service: {}", missingProducts);
            throw new IllegalArgumentException("Missing products: " + missingProducts);
        }
    }


    private OrderLineItem mapToDto(OrderLineItemListDto orderLineItemListDto) {

        OrderLineItem orderLineItems = new OrderLineItem();

        orderLineItems.setQuantity(orderLineItemListDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemListDto.getSkuCode());

        return orderLineItems;
    }
}
