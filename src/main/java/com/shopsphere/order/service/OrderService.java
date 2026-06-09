package com.shopsphere.order.service;

import com.shopsphere.order.dto.CreateOrderRequest;
import com.shopsphere.order.entity.*;
import com.shopsphere.order.event.OrderCreatedEvent;
import com.shopsphere.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class OrderService {

    private final OrderRepository orderRepo;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Transactional
    public Order createOrder(CreateOrderRequest req, String userEmail) {
        String orderNumber = "SS-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        BigDecimal total = req.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userEmail(userEmail)
                .total(total)
                .shippingName(req.getShippingName())
                .shippingAddress(req.getShippingAddress())
                .shippingCity(req.getShippingCity())
                .shippingZip(req.getShippingZip())
                .shippingCountry(req.getShippingCountry())
                .build();

        List<OrderItem> items = req.getItems().stream().map(i -> OrderItem.builder()
                .order(order).productId(i.getProductId())
                .productName(i.getProductName()).selectedStrap(i.getSelectedStrap())
                .quantity(i.getQuantity()).unitPrice(i.getUnitPrice()).build()
        ).collect(Collectors.toList());

        order.setItems(items);
        Order saved = orderRepo.save(order);

        publishOrderCreated(saved);
        log.info("Order created: {}", orderNumber);
        return saved;
    }

    private void publishOrderCreated(Order order) {
        try {
            List<OrderCreatedEvent.Item> items = order.getItems().stream()
                    .map(i -> new OrderCreatedEvent.Item(i.getProductId(), i.getProductName(), i.getSelectedStrap(), i.getQuantity(), i.getUnitPrice()))
                    .collect(Collectors.toList());

            kafkaTemplate.send("order-created", order.getId().toString(),
                    OrderCreatedEvent.builder()
                            .orderId(order.getId()).orderNumber(order.getOrderNumber())
                            .userEmail(order.getUserEmail()).total(order.getTotal())
                            .status("PENDING").items(items).build());
        } catch (Exception e) {
            log.error("Failed to publish order-created event: {}", e.getMessage());
        }
    }

    public List<Order> findByUser(String email) {
        return orderRepo.findByUserEmailOrderByCreatedAtDesc(email);
    }

    public Order findById(Long id) {
        return orderRepo.findById(id).orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    @Transactional
    public Order cancelOrder(Long id, String userEmail) {
        Order order = findById(id);
        if (!order.getUserEmail().equals(userEmail)) throw new RuntimeException("Unauthorized");
        order.setStatus(Order.OrderStatus.CANCELLED);
        return orderRepo.save(order);
    }
}
