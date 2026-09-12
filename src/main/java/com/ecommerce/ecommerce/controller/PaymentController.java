package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.PaymentDTO;
import com.ecommerce.ecommerce.dto.PaymentRequest;
import com.ecommerce.ecommerce.dto.RefundRequest;
import com.ecommerce.ecommerce.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<PaymentDTO> getAllPayments(@RequestParam(required = false) String status) {
        return paymentService.getAllPayments(status);
    }

    @GetMapping("/stats")
    public Map<String, Object> getPaymentStats() {
        return paymentService.getPaymentStats();
    }

    @GetMapping("/{id}")
    public PaymentDTO getPaymentById(@PathVariable Long id) {
        return paymentService.toDTO(paymentService.getPaymentById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getPaymentByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrderId(orderId)
                .map(p -> ResponseEntity.ok(paymentService.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest request) {
        try {
            PaymentDTO payment = paymentService.toDTO(paymentService.processPayment(request));
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not process payment: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<?> refundPayment(@PathVariable Long id, @RequestBody(required = false) RefundRequest request) {
        try {
            String reason = request != null ? request.getReason() : "Customer requested refund";
            PaymentDTO payment = paymentService.toDTO(paymentService.refundPayment(id, reason));
            return ResponseEntity.ok(payment);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not issue refund: " + e.getMessage()));
        }
    }
}
