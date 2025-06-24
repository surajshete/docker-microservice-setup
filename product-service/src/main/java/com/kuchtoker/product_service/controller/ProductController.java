package com.kuchtoker.product_service.controller;

import com.kuchtoker.product_service.dto.ProductRequest;
import com.kuchtoker.product_service.dto.ProductResponse;
import com.kuchtoker.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    @PostMapping("/create")
	@ResponseStatus(HttpStatus.CREATED)
	public void createProduct(@RequestBody ProductRequest productRequest){
	productService.createProduct(productRequest);
	}

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts(){
        return productService.getAllProducts();
    }

	@GetMapping("/exists")
	public ResponseEntity<Boolean> doesProductExist(@RequestParam String name) {
		return ResponseEntity.ok(productService.productExistsByName(name));
	}

}
