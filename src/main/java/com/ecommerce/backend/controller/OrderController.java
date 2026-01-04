package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.service.OrderService;
import com.ecommerce.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
// Allow Vercel and local testing
@CrossOrigin(origins = "${app.frontend-url}")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        // 1. Fix the time
        if (order.getDate() != null) {
            order.setDate(order.getDate().plusHours(1));
        }

        // 2. Save to DB
        Order savedOrder = orderRepository.save(order);

        // 3. Calculate total
        double totalAmount = 0;
        if (savedOrder.getProducts() != null) {
            totalAmount = savedOrder.getProducts().stream()
                    .mapToDouble(p -> p.getPrice() * p.getQuantity())
                    .sum();
        }

        // 4. Send Notification (NO TRY-CATCH HERE)
        // Let the @Async service handle the background work and logging
        emailService.sendOrderNotification(
                savedOrder.getCustomerName(),
                totalAmount,
                savedOrder.getId()
        );

        return savedOrder;
    }

    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable Long id, @RequestBody Order orderDetails) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setCustomerName(orderDetails.getCustomerName());
        order.setCustomerEmail(orderDetails.getCustomerEmail());
        order.setCustomerPhone(orderDetails.getCustomerPhone());
        order.setCustomerAddress(orderDetails.getCustomerAddress());
        order.setCity(orderDetails.getCity());
        order.setDate(orderDetails.getDate());
        order.setStatus(orderDetails.getStatus());
        order.setProducts(orderDetails.getProducts());

        return orderRepository.save(order);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        Order updatedOrder = orderService.updateStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
    }
}