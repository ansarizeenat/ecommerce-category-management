package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.model.Shipping;
import com.ecommerce.ecommerce.service.ShippingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    /**
     * 1. Shipping Dashboard - List all shipments with optional status filter
     */
    @GetMapping
    public List<ShippingDTO> getAllShipments(@RequestParam(required = false) String status) {
        return shippingService.getAllShipments(status);
    }

    /**
     * Dashboard statistics
     */
    @GetMapping("/stats")
    public Map<String, Object> getShippingStats() {
        return shippingService.getShippingStats();
    }

    /**
     * Get shipment by ID
     */
    @GetMapping("/{id}")
    public ShippingDTO getShippingById(@PathVariable Long id) {
        return shippingService.toDTO(shippingService.getShippingById(id));
    }

    /**
     * Get shipment by Order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getShippingByOrderId(@PathVariable Long orderId) {
        return shippingService.getShippingByOrderId(orderId)
                .map(s -> ResponseEntity.ok(shippingService.toDTO(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 2. Shipping Cost Calculation API
     */
    @PostMapping("/calculate-cost")
    public ResponseEntity<ShippingCostCalculationResponse> calculateShippingCost(@RequestBody ShippingCostCalculationRequest request) {
        ShippingCostCalculationResponse response = shippingService.calculateShippingCost(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. Track Shipment by tracking number
     */
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<?> trackShipment(@PathVariable String trackingNumber) {
        try {
            Map<String, Object> trackingInfo = shippingService.trackShipment(trackingNumber);
            return ResponseEntity.ok(trackingInfo);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create Shipment for an Order
     */
    @PostMapping
    public ResponseEntity<?> createShipment(@RequestBody ShippingRequest request) {
        try {
            Shipping shipping = shippingService.createShipment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(shippingService.toDTO(shipping));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not create shipment: " + e.getMessage()));
        }
    }

    /**
     * 4. Update Shipping Information (courier, tracking, status)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateShipping(@PathVariable Long id, @RequestBody ShippingUpdateRequest request) {
        try {
            Shipping shipping = shippingService.updateShipping(id, request);
            return ResponseEntity.ok(shippingService.toDTO(shipping));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not update shipping: " + e.getMessage()));
        }
    }

    /**
     * Update Shipping Status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateShippingStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String status = payload.get("shippingStatus");
            if (status == null || status.isBlank()) {
                status = payload.get("status");
            }
            Shipping shipping = shippingService.updateShippingStatus(id, status);
            return ResponseEntity.ok(shippingService.toDTO(shipping));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not update shipping status: " + e.getMessage()));
        }
    }
}
