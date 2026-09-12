package com.ecommerce.ecommerce;

import com.ecommerce.ecommerce.dto.OrderItemRequest;
import com.ecommerce.ecommerce.dto.OrderRequest;
import com.ecommerce.ecommerce.dto.PaymentDTO;
import com.ecommerce.ecommerce.dto.PaymentRequest;
import com.ecommerce.ecommerce.entity.Category;
import com.ecommerce.ecommerce.model.Order;
import com.ecommerce.ecommerce.model.Payment;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.repository.CategoryRepository;
import com.ecommerce.ecommerce.repository.PaymentRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.service.OrderService;
import com.ecommerce.ecommerce.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setCategoryName("Audio");
        category.setDescription("Audio accessories");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Bluetooth Earbuds");
        product.setPrice(60.00);
        product.setSku("EAR-01");
        product.setInventoryCount(100);
        product.setCategory(category);
        product.setStatus(true);
        product = productRepository.save(product);

        OrderRequest orderReq = new OrderRequest();
        orderReq.setCustomerName("Fatima Ansari");
        orderReq.setCustomerEmail("fatima" + System.currentTimeMillis() + "@example.com");
        orderReq.setShippingAddress("Bandra West, Mumbai");
        orderReq.setItems(List.of(new OrderItemRequest(product.getId(), 2)));

        testOrder = orderService.placeOrder(orderReq);
    }

    @Test
    void testProcessPayment_Success() {
        PaymentRequest req = new PaymentRequest(testOrder.getId(), testOrder.getTotalAmount(), "Credit Card");
        Payment payment = paymentService.processPayment(req);

        assertNotNull(payment.getPaymentId());
        assertEquals(testOrder.getId(), payment.getOrderId());
        assertEquals("Credit Card", payment.getPaymentMethod());
        assertEquals("Paid", payment.getPaymentStatus());
        assertEquals(testOrder.getTotalAmount(), payment.getAmount());
        assertNotNull(payment.getTransactionId());
        assertTrue(payment.getTransactionId().startsWith("STRIPE-"));
        assertNotNull(payment.getCreatedAt());
    }

    @Test
    void testProcessPayment_AlreadyPaidThrowsException() {
        PaymentRequest req = new PaymentRequest(testOrder.getId(), testOrder.getTotalAmount(), "PayPal");
        paymentService.processPayment(req);

        // Attempting to pay second time should fail
        PaymentRequest req2 = new PaymentRequest(testOrder.getId(), testOrder.getTotalAmount(), "Credit Card");
        assertThrows(IllegalStateException.class, () -> paymentService.processPayment(req2));
    }

    @Test
    void testProcessPayment_CancelledOrderThrowsException() {
        orderService.cancelOrder(testOrder.getId());

        PaymentRequest req = new PaymentRequest(testOrder.getId(), testOrder.getTotalAmount(), "Debit Card");
        assertThrows(IllegalStateException.class, () -> paymentService.processPayment(req));
    }

    @Test
    void testRefundPayment_Success() {
        PaymentRequest req = new PaymentRequest(testOrder.getId(), testOrder.getTotalAmount(), "Credit Card");
        Payment payment = paymentService.processPayment(req);
        assertEquals("Paid", payment.getPaymentStatus());

        Payment refunded = paymentService.refundPayment(payment.getPaymentId(), "Order cancelled by customer");
        assertEquals("Refunded", refunded.getPaymentStatus());
        assertNotNull(refunded.getUpdatedAt());

        // Verify updated in DB
        Payment inDb = paymentService.getPaymentById(payment.getPaymentId());
        assertEquals("Refunded", inDb.getPaymentStatus());
    }

    @Test
    void testRefundPayment_AlreadyRefundedThrowsException() {
        PaymentRequest req = new PaymentRequest(testOrder.getId(), testOrder.getTotalAmount(), "Bank Transfer");
        Payment payment = paymentService.processPayment(req);

        paymentService.refundPayment(payment.getPaymentId(), "First refund");
        assertThrows(IllegalStateException.class, () -> paymentService.refundPayment(payment.getPaymentId(), "Second refund"));
    }

    @Test
    void testGetAllPayments_FilterByStatus() {
        PaymentRequest req = new PaymentRequest(testOrder.getId(), testOrder.getTotalAmount(), "PayPal");
        paymentService.processPayment(req);

        List<PaymentDTO> paidList = paymentService.getAllPayments("Paid");
        assertFalse(paidList.isEmpty());
        assertTrue(paidList.stream().allMatch(p -> "Paid".equalsIgnoreCase(p.getPaymentStatus())));

        List<PaymentDTO> refundedList = paymentService.getAllPayments("Refunded");
        assertTrue(refundedList.stream().allMatch(p -> "Refunded".equalsIgnoreCase(p.getPaymentStatus())));
    }

    @Test
    void testProcessPayment_FailedGatewayIsLogged() {
        PaymentRequest req = new PaymentRequest(testOrder.getId(), testOrder.getTotalAmount(), "Credit Card");
        req.setSimulateFailure(true);

        Payment payment = paymentService.processPayment(req);
        assertEquals("Failed", payment.getPaymentStatus());
        assertTrue(payment.getTransactionId().startsWith("STRIPE-"));

        PaymentRequest retry = new PaymentRequest(testOrder.getId(), testOrder.getTotalAmount(), "PayPal");
        Payment paid = paymentService.processPayment(retry);
        assertEquals("Paid", paid.getPaymentStatus());
        assertTrue(paid.getTransactionId().startsWith("PAYPAL-"));
    }

    @Test
    void testGetPaymentStats() {
        PaymentRequest req = new PaymentRequest(testOrder.getId(), testOrder.getTotalAmount(), "Credit Card");
        paymentService.processPayment(req);

        Map<String, Object> stats = paymentService.getPaymentStats();
        assertNotNull(stats.get("totalRevenue"));
        assertTrue(((BigDecimal) stats.get("totalRevenue")).compareTo(BigDecimal.ZERO) > 0);
        assertTrue((Long) stats.get("paidCount") >= 1);
    }
}
