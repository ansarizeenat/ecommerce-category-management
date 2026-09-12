package com.ecommerce.ecommerce;

import com.ecommerce.ecommerce.dto.OrderItemRequest;
import com.ecommerce.ecommerce.dto.OrderRequest;
import com.ecommerce.ecommerce.entity.Category;
import com.ecommerce.ecommerce.model.Order;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.repository.CategoryRepository;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setCategoryName("Electronics");
        category.setDescription("Electronic items");
        category = categoryRepository.save(category);

        testProduct = new Product();
        testProduct.setName("Wireless Mouse");
        testProduct.setDescription("Ergonomic 2.4G wireless mouse");
        testProduct.setPrice(50.00);
        testProduct.setSku("WM-100");
        testProduct.setInventoryCount(20);
        testProduct.setCategory(category);
        testProduct.setStatus(true);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void testPlaceOrder_Success_AndCalculation() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");
        request.setShippingAddress("123 Main Street, New York, NY");
        request.setItems(List.of(new OrderItemRequest(testProduct.getId(), 2)));

        Order order = orderService.placeOrder(request);

        assertNotNull(order.getId());
        assertEquals("John Doe", order.getCustomerName());
        assertEquals("Pending", order.getOrderStatus());
        assertTrue(order.getStatus());

        // Subtotal = 50.00 * 2 = 100.00
        assertEquals(new BigDecimal("100.00"), order.getSubtotal());
        // Tax 5% = 5.00
        assertEquals(new BigDecimal("5.00"), order.getTaxAmount());
        // Subtotal >= 100 -> Free shipping = 0.00
        assertEquals(new BigDecimal("0.00"), order.getShippingCost());
        // Grand total = 105.00
        assertEquals(new BigDecimal("105.00"), order.getTotalAmount());

        // Verify inventory deducted: 20 - 2 = 18
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(18, updatedProduct.getInventoryCount());
    }

    @Test
    void testPlaceOrder_WithShippingCost() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Jane Smith");
        request.setShippingAddress("456 Elm St, Boston, MA");
        request.setItems(List.of(new OrderItemRequest(testProduct.getId(), 1)));

        Order order = orderService.placeOrder(request);

        // Subtotal = 50.00 (< 100, shipping should be 10.00)
        assertEquals(new BigDecimal("50.00"), order.getSubtotal());
        assertEquals(new BigDecimal("2.50"), order.getTaxAmount());
        assertEquals(new BigDecimal("10.00"), order.getShippingCost());
        assertEquals(new BigDecimal("62.50"), order.getTotalAmount());
    }

    @Test
    void testPlaceOrder_InsufficientInventoryThrowsException() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Bob");
        request.setShippingAddress("789 Oak Rd");
        request.setItems(List.of(new OrderItemRequest(testProduct.getId(), 50)));

        assertThrows(IllegalStateException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void testUpdateOrderStatus() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Alice");
        request.setShippingAddress("321 Pine St");
        request.setItems(List.of(new OrderItemRequest(testProduct.getId(), 1)));

        Order order = orderService.placeOrder(request);
        assertEquals("Pending", order.getOrderStatus());

        Order shippedOrder = orderService.updateOrderStatus(order.getId(), "Shipped");
        assertEquals("Shipped", shippedOrder.getOrderStatus());
    }

    @Test
    void testCancelOrder_SoftDeleteAndInventoryRestock() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("Charlie");
        request.setShippingAddress("999 Maple Ave");
        request.setItems(List.of(new OrderItemRequest(testProduct.getId(), 3)));

        Order order = orderService.placeOrder(request);
        assertEquals(17, productRepository.findById(testProduct.getId()).orElseThrow().getInventoryCount());

        Order cancelledOrder = orderService.cancelOrder(order.getId());

        // Soft delete checks
        assertEquals("Cancelled", cancelledOrder.getOrderStatus());
        assertFalse(cancelledOrder.getStatus());

        // Inventory should be restored back to 20
        Product restockedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(20, restockedProduct.getInventoryCount());
    }

    @Test
    void testCancelOrder_AlreadyShippedFails() {
        OrderRequest request = new OrderRequest();
        request.setCustomerName("David");
        request.setShippingAddress("555 River Rd");
        request.setItems(List.of(new OrderItemRequest(testProduct.getId(), 1)));

        Order order = orderService.placeOrder(request);
        orderService.updateOrderStatus(order.getId(), "Shipped");

        // Rule: Customers and admins can cancel an order if it has not been shipped yet
        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(order.getId()));
    }
}
