package com.kuchtoker.product_service.repository;

import com.kuchtoker.product_service.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product,String> {
	Optional<Product> findByName(String name);
	List<Product> findByNameIn(List<String> names);
}
