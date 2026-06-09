package com.shopsphere.order.repository;

import com.shopsphere.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order>     findByUserEmailOrderByCreatedAtDesc(String email);
    Optional<Order> findByOrderNumber(String orderNumber);
}
