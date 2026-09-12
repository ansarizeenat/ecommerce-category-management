package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.OrderItemRequest;
import com.ecommerce.ecommerce.dto.OrderRequest;
import com.ecommerce.ecommerce.model.Order;
import com.ecommerce.ecommerce.model.OrderItem;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.model.User;
import com.ecommerce.ecommerce.repository.OrderItemRepository;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Order> getAllOrders(String statusFilter) {
        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            return orderRepository.findByOrderStatusIgnoreCaseOrderByCreatedAtDesc(statusFilter.trim());
        }
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
    }

    @Transactional
    public Order placeOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty. Please select at least one product.");
        }
        if (request.getShippingAddress() == null || request.getShippingAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Shipping address is required.");
        }

        // Find or create user
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }
        if (user == null && request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
            user = userRepository.findByEmail(request.getCustomerEmail()).orElse(null);
        }
        if (user == null) {
            String userName = (request.getCustomerName() != null && !request.getCustomerName().isBlank())
                    ? request.getCustomerName()
                    : "Customer";
            String userEmail = (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank())
                    ? request.getCustomerEmail()
                    : "customer" + System.currentTimeMillis() + "@example.com";
            User newUser = new User();
            newUser.setName(userName);
            newUser.setEmail(userEmail);
            newUser.setPhone(request.getCustomerPhone());
            user = userRepository.save(newUser);
        }

        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(request.getCustomerName() != null && !request.getCustomerName().isBlank()
                ? request.getCustomerName()
                : user.getName());
        order.setShippingAddress(request.getShippingAddress().trim());
        order.setOrderStatus("Pending");
        order.setStatus(true);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getProductId() == null) {
                throw new IllegalArgumentException("Product ID is required for each cart item.");
            }
            int quantity = itemReq.getQuantity() != null && itemReq.getQuantity() > 0 ? itemReq.getQuantity() : 1;

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemReq.getProductId()));

            if (!product.isStatus()) {
                throw new IllegalStateException("Product '" + product.getName() + "' is currently inactive.");
            }

            if (product.getInventoryCount() < quantity) {
                throw new IllegalStateException("Insufficient inventory for product '" + product.getName() +
                        "'. Available: " + product.getInventoryCount() + ", Requested: " + quantity);
            }

            // Deduct inventory
            product.setInventoryCount(product.getInventoryCount() - quantity);
            productRepository.save(product);

            BigDecimal itemPrice = BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(quantity);
            orderItem.setPrice(itemPrice);
            orderItem.setItemTotal(itemTotal);

            order.addItem(orderItem);
        }

        // Calculate 5% tax
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);

        // Calculate shipping: Free if subtotal >= 100, else 10.00
        BigDecimal shippingCost = subtotal.compareTo(new BigDecimal("100.00")) >= 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : new BigDecimal("10.00").setScale(2, RoundingMode.HALF_UP);

        BigDecimal grandTotal = subtotal.add(taxAmount).add(shippingCost).setScale(2, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setShippingCost(shippingCost);
        order.setTotalAmount(grandTotal);

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Long id, String newStatus) {
        Order order = getOrderById(id);

        if ("Cancelled".equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("Cannot update status of an already cancelled order.");
        }

        String normalizedStatus = newStatus != null ? newStatus.trim() : "";
        if (normalizedStatus.isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty.");
        }

        if ("Cancelled".equalsIgnoreCase(normalizedStatus)) {
            return cancelOrder(id);
        }

        order.setOrderStatus(normalizedStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long id) {
        Order order = getOrderById(id);

        if ("Cancelled".equalsIgnoreCase(order.getOrderStatus()) || Boolean.FALSE.equals(order.getStatus())) {
            throw new IllegalStateException("Order is already cancelled.");
        }

        // Rule: Customers and admins can cancel an order if it has not been shipped yet
        if ("Shipped".equalsIgnoreCase(order.getOrderStatus()) || "Delivered".equalsIgnoreCase(order.getOrderStatus())) {
            throw new IllegalStateException("Order cannot be cancelled because it has already been " + order.getOrderStatus().toLowerCase() + ".");
        }

        // Soft delete: status = false, order_status = Cancelled
        order.setOrderStatus("Cancelled");
        order.setStatus(false);

        // Restock inventory
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.setInventoryCount(product.getInventoryCount() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        return orderRepository.save(order);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
