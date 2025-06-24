package com.kuchtoker.product_service.service;


import com.kuchtoker.product_service.dto.InventoryCheckRequest;
import com.kuchtoker.product_service.dto.ProductRequest;
import com.kuchtoker.product_service.dto.ProductResponse;
import com.kuchtoker.product_service.exception.CustomException;
import com.kuchtoker.product_service.model.Product;
import com.kuchtoker.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final WebClient.Builder webClientBuilder;
    public void createProduct(ProductRequest productRequest) {
        // 1. Check if product already exists (based on name in this example)
        Optional<Product> existingProductOpt = productRepository.findByName(productRequest.getName());

        Product product;
        if (existingProductOpt.isPresent()) {
            product = existingProductOpt.get();
            boolean isUpdated = false;
            log.info("Product {} already exists, skipping creation", product.getName());

            if(!(product.getDescription().equalsIgnoreCase(productRequest.getDescription()))){
                log.info("Product {} already exists, found new Description: {}", product.getName(), productRequest.getDescription());
                product.setDescription(productRequest.getDescription());
                isUpdated = true;
            }
            if(!(product.getPrice().equals(productRequest.getPrice()))){
                log.info("Product {} already exists, found new price: {} updated", product.getName(), productRequest.getPrice());
                product.setPrice(productRequest.getPrice());
                isUpdated = true;
            }
            if (isUpdated) {
                productRepository.save(product);
            }
        } else {
            // 2. Save new product in DB
            product = Product.builder()
                    .name(productRequest.getName())
                    .description(productRequest.getDescription())
                    .price(productRequest.getPrice())
                    .build();

            productRepository.save(product);
            log.info("Product {} is created and saved", product.getId());
        }

        // 3. Prepare inventory request
        InventoryCheckRequest inventoryRequest = new InventoryCheckRequest(
                productRequest.getName(),  // use name as SKU code
                productRequest.getQuantity()
        );

        // 4. Call inventory-service
        try {
            webClientBuilder.build()
                    .post()
                    .uri("http://inventory-service/api/inventory/check-and-add")
                    .bodyValue(List.of(inventoryRequest)) // inventory API expects a list
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        log.error("Inventory service error: {}", errorMessage);
                                        return Mono.error(new CustomException(errorMessage));
                                    })
                    )
                    .bodyToMono(Void.class)
                    .block();

            log.info("Inventory added/updated for SKU: {}", productRequest.getName());
        } catch (Exception e) {
            log.error("Failed to update inventory for SKU {}: {}", productRequest.getName(), e.getMessage(), e);
            throw new CustomException("Inventory update failed");
        }
    }



    public List<ProductResponse> getAllProducts() {
        List <Product> products = productRepository.findAll();
        return products.stream().map(this::mapToProductResponse).toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .description(product.getDescription())
                .name(product.getName())
                .price(product.getPrice())
                .build();
    }
    public boolean productExistsByName(String name) {
        return productRepository.findByName(name).isPresent();
    }
}
