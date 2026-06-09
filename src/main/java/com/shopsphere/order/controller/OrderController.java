package com.shopsphere.order.controller;

import com.shopsphere.order.dto.CreateOrderRequest;
import com.shopsphere.order.entity.Order;
import com.shopsphere.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> create(
            @Valid @RequestBody CreateOrderRequest req,
            @RequestHeader("X-User-Email") String userEmail) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(req, userEmail));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Order>> myOrders(@RequestHeader("X-User-Email") String userEmail) {
        return ResponseEntity.ok(orderService.findByUser(userEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancel(@PathVariable Long id, @RequestHeader("X-User-Email") String userEmail) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userEmail));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "order-service is running"));
    }
}
