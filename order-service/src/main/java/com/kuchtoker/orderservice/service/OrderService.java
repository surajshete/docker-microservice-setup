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

import java.util.List;
import java.util.UUID;

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

        boolean allProductsInStock;

        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");
        boolean isProductExistsInMongoDB = false;

        try {
            log.info("Calling product exists Service");
            isProductExistsInMongoDB = Boolean.TRUE.equals(
                    webClientBuilder.build()
                            .get()
                            .uri("http://product-service/api/product/exists", uriBuilder ->
                                    uriBuilder.queryParam("name", skuCodes).build())
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .block()
            );
            log.info("product exists Service result {}", isProductExistsInMongoDB);
        } catch (Exception e) {
            log.warn("Failed to call product-service: {}", e.getMessage());
        }

        if (isProductExistsInMongoDB) {
            try (Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start())) {
                log.info("Calling inventory Service");
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
                    kafkaTemplate.send("orderTopic", new OrderPlacedEvent(order.getOrderNumber()));
                    return "Order placed successfully";
                } else {
                    throw new IllegalArgumentException("Insufficient stock");
                }

            } finally {
                inventoryServiceLookup.end();
            }
        } else {
            throw new IllegalArgumentException("Product is not in inventory, please try again later");
        }
    }


    private OrderLineItem mapToDto(OrderLineItemListDto orderLineItemListDto) {
        OrderLineItem orderLineItems = new OrderLineItem();
        orderLineItems.setPrice(orderLineItemListDto.getPrice());
        orderLineItems.setQuantity(orderLineItemListDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemListDto.getSkuCode());
        return orderLineItems;
    }
}
