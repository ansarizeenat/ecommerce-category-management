package com.ecommerce.ecommerce;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.entity.Category;
import com.ecommerce.ecommerce.model.Order;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.model.Shipping;
import com.ecommerce.ecommerce.repository.CategoryRepository;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.ShippingRepository;
import com.ecommerce.ecommerce.service.OrderService;
import com.ecommerce.ecommerce.service.ShippingService;
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
class ShippingServiceTest {

    @Autowired
    private ShippingService shippingService;

    @Autowired
    private ShippingRepository shippingRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Order testOrder;
    private Order testOrder2;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setCategoryName("Electronics");
        category.setDescription("Electronic devices");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setName("Wireless Mouse");
        product.setPrice(30.00);
        product.setSku("MOU-01");
        product.setInventoryCount(50);
        product.setCategory(category);
        product.setStatus(true);
        product = productRepository.save(product);

        OrderRequest orderReq = new OrderRequest();
        orderReq.setCustomerName("Zeenat Ansari");
        orderReq.setCustomerEmail("zeenat" + System.currentTimeMillis() + "@example.com");
        orderReq.setShippingAddress("Kurla West, Mumbai, 400070");
        orderReq.setItems(List.of(new OrderItemRequest(product.getId(), 2)));

        testOrder = orderService.placeOrder(orderReq);

        OrderRequest orderReq2 = new OrderRequest();
        orderReq2.setCustomerName("Aamir Khan");
        orderReq2.setCustomerEmail("aamir" + System.currentTimeMillis() + "@example.com");
        orderReq2.setShippingAddress("Andheri East, Mumbai, 400069");
        orderReq2.setItems(List.of(new OrderItemRequest(product.getId(), 1)));

        testOrder2 = orderService.placeOrder(orderReq2);
    }

    @Test
    void testCalculateShippingCost_DomesticStandard() {
        // Weight: 2.0 kg, Domestic (1.4x), Standard (1.0x)
        // (5.00 + 2*2.00) * 1.4 * 1.0 = 9.00 * 1.4 = 12.60
        ShippingCostCalculationRequest req = new ShippingCostCalculationRequest(
                new BigDecimal("2.00"), "Domestic", "Standard"
        );

        ShippingCostCalculationResponse res = shippingService.calculateShippingCost(req);

        assertNotNull(res);
        assertEquals(new BigDecimal("12.60"), res.getTotalShippingCost());
        assertEquals(new BigDecimal("5.00"), res.getBaseCost());
        assertEquals(new BigDecimal("4.00"), res.getWeightCost());
        assertTrue(res.getEstimatedDeliveryDays() > 0);
    }

    @Test
    void testCalculateShippingCost_InternationalExpress() {
        // Weight: 1.0 kg, International (2.5x), Express (1.5x)
        // (5.00 + 2.00) * 2.5 * 1.5 = 7.00 * 3.75 = 26.25
        ShippingCostCalculationRequest req = new ShippingCostCalculationRequest(
                new BigDecimal("1.00"), "International", "Express"
        );

        ShippingCostCalculationResponse res = shippingService.calculateShippingCost(req);

        assertNotNull(res);
        assertEquals(new BigDecimal("26.25"), res.getTotalShippingCost());
        assertEquals(new BigDecimal("2.50"), res.getLocationMultiplier());
        assertEquals(new BigDecimal("1.50"), res.getMethodMultiplier());
    }

    @Test
    void testCreateShipment_Success() {
        ShippingRequest req = new ShippingRequest();
        req.setOrderId(testOrder.getId());
        req.setCourierService("Blue Dart");
        req.setWeight(new BigDecimal("1.80"));
        req.setDeliveryLocation("Local");
        req.setShippingMethod("Standard");

        Shipping shipping = shippingService.createShipment(req);

        assertNotNull(shipping.getShippingId());
        assertEquals(testOrder.getId(), shipping.getOrderId());
        assertEquals("Blue Dart", shipping.getCourierService());
        assertNotNull(shipping.getTrackingNumber());
        assertTrue(shipping.getTrackingNumber().startsWith("BD-"));
        assertEquals("Shipped", shipping.getShippingStatus());
        assertTrue(shipping.getShippingCost().compareTo(BigDecimal.ZERO) > 0);

        // Check linked order is updated to Shipped
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertEquals("Shipped", updatedOrder.getOrderStatus());
    }

    @Test
    void testCreateShipment_OrderCancelled_ThrowsException() {
        orderService.cancelOrder(testOrder.getId());

        ShippingRequest req = new ShippingRequest();
        req.setOrderId(testOrder.getId());
        req.setCourierService("FedEx");

        assertThrows(IllegalStateException.class, () -> shippingService.createShipment(req));
    }

    @Test
    void testCreateShipment_Duplicate_ThrowsException() {
        ShippingRequest req1 = new ShippingRequest();
        req1.setOrderId(testOrder.getId());
        req1.setCourierService("Blue Dart");
        shippingService.createShipment(req1);

        // Attempting second shipment for same order
        ShippingRequest req2 = new ShippingRequest();
        req2.setOrderId(testOrder.getId());
        req2.setCourierService("DHL");

        assertThrows(IllegalStateException.class, () -> shippingService.createShipment(req2));
    }

    @Test
    void testTrackShipment_Success() {
        ShippingRequest req = new ShippingRequest();
        req.setOrderId(testOrder.getId());
        req.setCourierService("Delhivery");
        Shipping created = shippingService.createShipment(req);

        Map<String, Object> trackingData = shippingService.trackShipment(created.getTrackingNumber());

        assertNotNull(trackingData);
        assertEquals(created.getTrackingNumber(), trackingData.get("trackingNumber"));
        assertEquals("Delhivery", trackingData.get("courierService"));
        assertEquals("Shipped", trackingData.get("currentStatus"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> timeline = (List<Map<String, Object>>) trackingData.get("timeline");
        assertNotNull(timeline);
        assertTrue(timeline.size() >= 4);
    }

    @Test
    void testTrackShipment_NotFound_ThrowsException() {
        assertThrows(RuntimeException.class, () -> shippingService.trackShipment("INVALID-TRK-9999"));
    }

    @Test
    void testUpdateShipping_InformationAndStatus() {
        ShippingRequest req = new ShippingRequest();
        req.setOrderId(testOrder.getId());
        req.setCourierService("FedEx");
        Shipping created = shippingService.createShipment(req);

        // Update courier service and status to Delivered
        ShippingUpdateRequest updateReq = new ShippingUpdateRequest();
        updateReq.setCourierService("DHL Express");
        updateReq.setShippingStatus("Delivered");

        Shipping updated = shippingService.updateShipping(created.getShippingId(), updateReq);

        assertEquals("DHL Express", updated.getCourierService());
        assertEquals("Delivered", updated.getShippingStatus());

        // Verify linked order synchronized to Delivered
        Order linkedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertEquals("Delivered", linkedOrder.getOrderStatus());
    }

    @Test
    void testGetShippingStats() {
        ShippingRequest req1 = new ShippingRequest();
        req1.setOrderId(testOrder.getId());
        req1.setCourierService("Blue Dart");
        Shipping s1 = shippingService.createShipment(req1);

        ShippingRequest req2 = new ShippingRequest();
        req2.setOrderId(testOrder2.getId());
        req2.setCourierService("FedEx");
        Shipping s2 = shippingService.createShipment(req2);

        shippingService.updateShippingStatus(s2.getShippingId(), "Delivered");

        Map<String, Object> stats = shippingService.getShippingStats();
        assertNotNull(stats);
        assertTrue((int) stats.get("totalShipments") >= 2);
        assertTrue((long) stats.get("deliveredCount") >= 1);
        assertNotNull(stats.get("totalShippingCost"));
    }
}
