package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.model.Order;
import com.ecommerce.ecommerce.model.Shipping;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.ecommerce.ecommerce.repository.ShippingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;

    public ShippingService(ShippingRepository shippingRepository, OrderRepository orderRepository) {
        this.shippingRepository = shippingRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * 1. Shipping Cost Calculation
     * Calculates shipping costs based on weight, delivery location, and shipping method.
     */
    public ShippingCostCalculationResponse calculateShippingCost(ShippingCostCalculationRequest request) {
        BigDecimal weight = (request.getWeight() != null && request.getWeight().compareTo(BigDecimal.ZERO) > 0)
                ? request.getWeight()
                : BigDecimal.ONE;

        String location = normalizeLocation(request.getDeliveryLocation());
        String method = normalizeMethod(request.getShippingMethod());

        // Base cost: $5.00
        BigDecimal baseCost = new BigDecimal("5.00");
        // Weight fee: $2.00 per kg
        BigDecimal weightRate = new BigDecimal("2.00");
        BigDecimal weightCost = weight.multiply(weightRate).setScale(2, RoundingMode.HALF_UP);

        // Location multiplier & estimated days
        BigDecimal locationMultiplier;
        int baseDays;
        switch (location) {
            case "Local":
                locationMultiplier = new BigDecimal("1.00");
                baseDays = 2;
                break;
            case "International":
                locationMultiplier = new BigDecimal("2.50");
                baseDays = 8;
                break;
            case "Domestic":
            default:
                locationMultiplier = new BigDecimal("1.40");
                baseDays = 4;
                location = "Domestic";
                break;
        }

        // Method multiplier & day adjustment
        BigDecimal methodMultiplier;
        int daysSaved;
        switch (method) {
            case "Express":
                methodMultiplier = new BigDecimal("1.50");
                daysSaved = 2;
                break;
            case "Overnight":
                methodMultiplier = new BigDecimal("2.20");
                daysSaved = 3;
                break;
            case "Standard":
            default:
                methodMultiplier = new BigDecimal("1.00");
                daysSaved = 0;
                method = "Standard";
                break;
        }

        int estimatedDays = Math.max(1, baseDays - daysSaved);

        // Formula: (baseCost + weightCost) * locationMultiplier * methodMultiplier
        BigDecimal subTotal = baseCost.add(weightCost);
        BigDecimal total = subTotal.multiply(locationMultiplier).multiply(methodMultiplier).setScale(2, RoundingMode.HALF_UP);

        ShippingCostCalculationResponse response = new ShippingCostCalculationResponse();
        response.setBaseCost(baseCost);
        response.setWeightCost(weightCost);
        response.setLocationMultiplier(locationMultiplier);
        response.setMethodMultiplier(methodMultiplier);
        response.setTotalShippingCost(total);
        response.setEstimatedDeliveryDays(estimatedDays);
        response.setBreakdown(String.format("Base: $%.2f + Weight (%skg): $%.2f x Loc (%s: %sx) x Method (%s: %sx) = $%.2f",
                baseCost, weight.toPlainString(), weightCost, location, locationMultiplier, method, methodMultiplier, total));

        return response;
    }

    /**
     * 2. Shipping Dashboard
     * Retrieves all shipments or filters by shipping status.
     */
    public List<ShippingDTO> getAllShipments(String statusFilter) {
        List<Shipping> shipments;
        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            shipments = shippingRepository.findByShippingStatusIgnoreCaseOrderByCreatedAtDesc(statusFilter.trim());
        } else {
            shipments = shippingRepository.findAllByOrderByCreatedAtDesc();
        }

        List<ShippingDTO> dtos = new ArrayList<>();
        for (Shipping s : shipments) {
            dtos.add(toDTO(s));
        }
        return dtos;
    }

    public Shipping getShippingById(Long id) {
        return shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping record not found with ID: " + id));
    }

    public Optional<Shipping> getShippingByOrderId(Long orderId) {
        List<Shipping> list = shippingRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * 3. Track Shipment
     * Allows customers and admins to view the current status of their shipment using a tracking number.
     */
    public Map<String, Object> trackShipment(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Tracking number cannot be empty.");
        }

        Shipping shipping = shippingRepository.findByTrackingNumberIgnoreCase(trackingNumber.trim())
                .orElseThrow(() -> new RuntimeException("No shipment found for tracking number: " + trackingNumber.trim()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shipping", toDTO(shipping));
        result.put("trackingNumber", shipping.getTrackingNumber());
        result.put("courierService", shipping.getCourierService());
        result.put("currentStatus", shipping.getShippingStatus());
        result.put("estimatedDelivery", shipping.getEstimatedDelivery());

        // Construct interactive tracking timeline steps
        List<Map<String, Object>> timeline = buildTrackingTimeline(shipping);
        result.put("timeline", timeline);

        return result;
    }

    /**
     * 4. Create Shipment
     * Initiates shipment for an order, saves shipping record, and updates order status to Shipped.
     */
    @Transactional
    public Shipping createShipment(ShippingRequest request) {
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("Order ID is required to create a shipment.");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOrderId()));

        if ("Cancelled".equalsIgnoreCase(order.getOrderStatus()) || Boolean.FALSE.equals(order.getStatus())) {
            throw new IllegalStateException("Cannot ship a cancelled order.");
        }

        if (shippingRepository.existsByOrderId(order.getId())) {
            throw new IllegalStateException("Order #" + order.getId() + " has already been shipped.");
        }

        String courier = (request.getCourierService() != null && !request.getCourierService().isBlank())
                ? request.getCourierService().trim()
                : "Blue Dart";

        String tracking = (request.getTrackingNumber() != null && !request.getTrackingNumber().isBlank())
                ? request.getTrackingNumber().trim()
                : generateTrackingNumber(courier);

        // Check if custom tracking number already exists
        if (shippingRepository.existsByTrackingNumber(tracking)) {
            throw new IllegalStateException("Tracking number '" + tracking + "' is already in use.");
        }

        // Calculate or assign shipping cost
        BigDecimal cost;
        if (request.getShippingCost() != null && request.getShippingCost().compareTo(BigDecimal.ZERO) >= 0) {
            cost = request.getShippingCost();
        } else if (order.getShippingCost() != null && order.getShippingCost().compareTo(BigDecimal.ZERO) > 0) {
            cost = order.getShippingCost();
        } else {
            ShippingCostCalculationRequest calcReq = new ShippingCostCalculationRequest(
                    request.getWeight(),
                    request.getDeliveryLocation(),
                    request.getShippingMethod()
            );
            cost = calculateShippingCost(calcReq).getTotalShippingCost();
        }

        Shipping shipping = new Shipping();
        shipping.setOrder(order);
        shipping.setCourierService(courier);
        shipping.setTrackingNumber(tracking);
        shipping.setShippingStatus("Shipped");
        shipping.setShippingCost(cost);
        shipping.setWeight(request.getWeight() != null ? request.getWeight() : new BigDecimal("1.50"));
        shipping.setShippingMethod(normalizeMethod(request.getShippingMethod()));
        shipping.setDeliveryLocation(normalizeLocation(request.getDeliveryLocation()));

        int days = "Overnight".equalsIgnoreCase(shipping.getShippingMethod()) ? 1
                : ("Express".equalsIgnoreCase(shipping.getShippingMethod()) ? 2 : 4);
        shipping.setEstimatedDelivery(LocalDateTime.now().plusDays(days));
        shipping.setCreatedAt(LocalDateTime.now());
        shipping.setUpdatedAt(LocalDateTime.now());

        Shipping saved = shippingRepository.save(shipping);

        // Synchronize Order status to Shipped
        if (!"Delivered".equalsIgnoreCase(order.getOrderStatus())) {
            order.setOrderStatus("Shipped");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        return saved;
    }

    /**
     * 5. Update Shipping Information
     * Admins can update courier service, tracking number, and shipping status.
     */
    @Transactional
    public Shipping updateShipping(Long id, ShippingUpdateRequest request) {
        Shipping shipping = getShippingById(id);

        if (request.getCourierService() != null && !request.getCourierService().isBlank()) {
            shipping.setCourierService(request.getCourierService().trim());
        }

        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isBlank()) {
            String newTracking = request.getTrackingNumber().trim();
            if (!newTracking.equalsIgnoreCase(shipping.getTrackingNumber()) && shippingRepository.existsByTrackingNumber(newTracking)) {
                throw new IllegalStateException("Tracking number '" + newTracking + "' already belongs to another shipment.");
            }
            shipping.setTrackingNumber(newTracking);
        }

        if (request.getShippingStatus() != null && !request.getShippingStatus().isBlank()) {
            String newStatus = normalizeStatus(request.getShippingStatus());
            shipping.setShippingStatus(newStatus);

            // Synchronize linked order status
            Order order = shipping.getOrder();
            if (order != null && Boolean.TRUE.equals(order.getStatus()) && !"Cancelled".equalsIgnoreCase(order.getOrderStatus())) {
                if ("Delivered".equalsIgnoreCase(newStatus)) {
                    order.setOrderStatus("Delivered");
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);
                } else if ("Shipped".equalsIgnoreCase(newStatus) || "In Transit".equalsIgnoreCase(newStatus)) {
                    if (!"Delivered".equalsIgnoreCase(order.getOrderStatus())) {
                        order.setOrderStatus("Shipped");
                        order.setUpdatedAt(LocalDateTime.now());
                        orderRepository.save(order);
                    }
                }
            }
        }

        shipping.setUpdatedAt(LocalDateTime.now());
        return shippingRepository.save(shipping);
    }

    /**
     * 6. Quick Status Update
     */
    @Transactional
    public Shipping updateShippingStatus(Long id, String newStatus) {
        ShippingUpdateRequest req = new ShippingUpdateRequest();
        req.setShippingStatus(newStatus);
        return updateShipping(id, req);
    }

    /**
     * 7. Dashboard Metrics / Statistics
     */
    public Map<String, Object> getShippingStats() {
        List<Shipping> all = shippingRepository.findAll();
        BigDecimal totalCost = BigDecimal.ZERO;
        long shippedCount = 0;
        long inTransitCount = 0;
        long deliveredCount = 0;

        for (Shipping s : all) {
            if (s.getShippingCost() != null) {
                totalCost = totalCost.add(s.getShippingCost());
            }
            String status = s.getShippingStatus() != null ? s.getShippingStatus().trim().toLowerCase() : "";
            if ("shipped".equals(status)) {
                shippedCount++;
            } else if ("in transit".equals(status) || "in-transit".equals(status)) {
                inTransitCount++;
            } else if ("delivered".equals(status)) {
                deliveredCount++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalShipments", all.size());
        stats.put("shippedCount", shippedCount);
        stats.put("inTransitCount", inTransitCount);
        stats.put("deliveredCount", deliveredCount);
        stats.put("totalShippingCost", totalCost);
        return stats;
    }

    public ShippingDTO toDTO(Shipping shipping) {
        ShippingDTO dto = new ShippingDTO();
        dto.setShippingId(shipping.getShippingId());
        dto.setOrderId(shipping.getOrder() != null ? shipping.getOrder().getId() : null);
        dto.setCustomerName(shipping.getOrder() != null ? shipping.getOrder().getCustomerName() : "N/A");
        dto.setShippingAddress(shipping.getOrder() != null ? shipping.getOrder().getShippingAddress() : "N/A");
        dto.setCourierService(shipping.getCourierService());
        dto.setTrackingNumber(shipping.getTrackingNumber());
        dto.setShippingStatus(shipping.getShippingStatus());
        dto.setShippingCost(shipping.getShippingCost());
        dto.setWeight(shipping.getWeight());
        dto.setShippingMethod(shipping.getShippingMethod());
        dto.setDeliveryLocation(shipping.getDeliveryLocation());
        dto.setEstimatedDelivery(shipping.getEstimatedDelivery());
        dto.setCreatedAt(shipping.getCreatedAt());
        dto.setUpdatedAt(shipping.getUpdatedAt());
        return dto;
    }

    private String generateTrackingNumber(String courier) {
        String prefix;
        String c = courier != null ? courier.toLowerCase() : "";
        if (c.contains("blue") || c.contains("dart")) {
            prefix = "BD";
        } else if (c.contains("fedex")) {
            prefix = "FDX";
        } else if (c.contains("dhl")) {
            prefix = "DHL";
        } else if (c.contains("delhivery")) {
            prefix = "DEL";
        } else if (c.contains("speed") || c.contains("post")) {
            prefix = "SP";
        } else {
            prefix = "TRK";
        }
        long randomPart = (long) (Math.random() * 90000000L + 10000000L);
        return prefix + "-" + randomPart;
    }

    private String normalizeLocation(String loc) {
        if (loc == null || loc.isBlank()) return "Domestic";
        String l = loc.trim().toLowerCase();
        if (l.contains("local")) return "Local";
        if (l.contains("international") || l.contains("global")) return "International";
        return "Domestic";
    }

    private String normalizeMethod(String method) {
        if (method == null || method.isBlank()) return "Standard";
        String m = method.trim().toLowerCase();
        if (m.contains("express")) return "Express";
        if (m.contains("overnight") || m.contains("priority")) return "Overnight";
        return "Standard";
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return "Shipped";
        String s = status.trim().toLowerCase();
        if (s.contains("transit")) return "In Transit";
        if (s.contains("deliver")) return "Delivered";
        return "Shipped";
    }

    private List<Map<String, Object>> buildTrackingTimeline(Shipping shipping) {
        String current = shipping.getShippingStatus() != null ? shipping.getShippingStatus().trim() : "Shipped";
        List<Map<String, Object>> list = new ArrayList<>();

        boolean isShipped = "Shipped".equalsIgnoreCase(current) || "In Transit".equalsIgnoreCase(current) || "Delivered".equalsIgnoreCase(current);
        boolean isInTransit = "In Transit".equalsIgnoreCase(current) || "Delivered".equalsIgnoreCase(current);
        boolean isDelivered = "Delivered".equalsIgnoreCase(current);

        list.add(Map.of(
                "step", "Order Confirmed",
                "completed", true,
                "description", "Order #" + (shipping.getOrder() != null ? shipping.getOrder().getId() : "") + " verified and ready for dispatch.",
                "timestamp", shipping.getCreatedAt() != null ? shipping.getCreatedAt().toString() : ""
        ));

        list.add(Map.of(
                "step", "Shipped",
                "completed", isShipped,
                "description", "Package handed over to " + shipping.getCourierService() + " with tracking #" + shipping.getTrackingNumber() + ".",
                "timestamp", shipping.getCreatedAt() != null ? shipping.getCreatedAt().toString() : ""
        ));

        list.add(Map.of(
                "step", "In Transit",
                "completed", isInTransit,
                "description", isInTransit ? "Package is in transit to destination sorting hub." : "Awaiting transit scan.",
                "timestamp", isInTransit && shipping.getUpdatedAt() != null ? shipping.getUpdatedAt().toString() : ""
        ));

        list.add(Map.of(
                "step", "Delivered",
                "completed", isDelivered,
                "description", isDelivered ? "Package safely delivered to customer." : "Expected delivery by " + (shipping.getEstimatedDelivery() != null ? shipping.getEstimatedDelivery().toLocalDate().toString() : "soon"),
                "timestamp", isDelivered && shipping.getUpdatedAt() != null ? shipping.getUpdatedAt().toString() : ""
        ));

        return list;
    }
}
