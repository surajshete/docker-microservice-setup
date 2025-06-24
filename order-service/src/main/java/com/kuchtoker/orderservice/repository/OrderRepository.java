package com.kuchtoker.orderservice.repository;

import com.kuchtoker.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
