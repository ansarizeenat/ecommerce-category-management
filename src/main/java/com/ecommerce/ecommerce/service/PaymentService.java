package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.PaymentDTO;
import com.ecommerce.ecommerce.dto.PaymentRequest;
import com.ecommerce.ecommerce.model.Order;
import com.ecommerce.ecommerce.model.Payment;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.ecommerce.ecommerce.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public List<PaymentDTO> getAllPayments(String statusFilter) {
        List<Payment> payments;
        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            payments = paymentRepository.findByPaymentStatusIgnoreCaseOrderByCreatedAtDesc(statusFilter.trim());
        } else {
            payments = paymentRepository.findAllByOrderByCreatedAtDesc();
        }

        List<PaymentDTO> dtos = new ArrayList<>();
        for (Payment p : payments) {
            dtos.add(toDTO(p));
        }
        return dtos;
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));
    }

    public Optional<Payment> getPaymentByOrderId(Long orderId) {
        List<Payment> list = paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Transactional
    public Payment processPayment(PaymentRequest request) {
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("Order ID is required to process payment.");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required (e.g. Credit Card, Debit Card, PayPal, Bank Transfer).");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOrderId()));

        if ("Cancelled".equalsIgnoreCase(order.getOrderStatus()) || Boolean.FALSE.equals(order.getStatus())) {
            throw new IllegalStateException("Cannot process payment for a cancelled order.");
        }

        // Check if order already has an approved payment
        boolean alreadyPaid = paymentRepository.existsByOrderIdAndPaymentStatus(order.getId(), "Paid");
        if (alreadyPaid) {
            throw new IllegalStateException("Order #" + order.getId() + " is already paid.");
        }

        BigDecimal paymentAmount = (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0)
                ? request.getAmount()
                : order.getTotalAmount();

        String gateway = gatewayName(request.getPaymentMethod());
        String txnId = gateway + "-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 9000 + 1000);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(paymentAmount);
        payment.setPaymentMethod(normalizePaymentMethod(request.getPaymentMethod()));
        payment.setTransactionId(txnId);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        if (isGatewayDeclined(request)) {
            payment.setPaymentStatus("Failed");
            return paymentRepository.save(payment);
        }

        payment.setPaymentStatus("Paid");
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(Long paymentId, String reason) {
        Payment payment = getPaymentById(paymentId);

        if (!"Paid".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new IllegalStateException("Only completed 'Paid' payments can be refunded. Current status is: " + payment.getPaymentStatus());
        }

        payment.setPaymentStatus("Refunded");
        payment.setUpdatedAt(LocalDateTime.now());
        if (reason != null && !reason.isBlank()) {
            payment.setRefundReason(reason);
        }

        // Update order status if order was still active
        Order order = payment.getOrder();
        if (order != null && Boolean.TRUE.equals(order.getStatus())) {
            order.setOrderStatus("Cancelled");
            order.setStatus(false);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }

        return paymentRepository.save(payment);
    }

    public Map<String, Object> getPaymentStats() {
        List<Payment> all = paymentRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalRefunded = BigDecimal.ZERO;
        long paidCount = 0;
        long failedCount = 0;
        long refundedCount = 0;

        for (Payment p : all) {
            if ("Paid".equalsIgnoreCase(p.getPaymentStatus())) {
                totalRevenue = totalRevenue.add(p.getAmount());
                paidCount++;
            } else if ("Refunded".equalsIgnoreCase(p.getPaymentStatus())) {
                totalRefunded = totalRefunded.add(p.getAmount());
                refundedCount++;
            } else if ("Failed".equalsIgnoreCase(p.getPaymentStatus())) {
                failedCount++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalRefunded", totalRefunded);
        stats.put("paidCount", paidCount);
        stats.put("failedCount", failedCount);
        stats.put("refundedCount", refundedCount);
        stats.put("totalTransactions", all.size());
        return stats;
    }

    public PaymentDTO toDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        dto.setCustomerName(payment.getOrder() != null ? payment.getOrder().getCustomerName() : "N/A");
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setRefundReason(payment.getRefundReason());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }

    private String normalizePaymentMethod(String raw) {
        String trimmed = raw.trim();
        if (trimmed.equalsIgnoreCase("credit") || trimmed.equalsIgnoreCase("credit card")) return "Credit Card";
        if (trimmed.equalsIgnoreCase("debit") || trimmed.equalsIgnoreCase("debit card")) return "Debit Card";
        if (trimmed.equalsIgnoreCase("paypal")) return "PayPal";
        if (trimmed.equalsIgnoreCase("bank") || trimmed.equalsIgnoreCase("bank transfer")) return "Bank Transfer";
        return trimmed;
    }

    private String gatewayName(String method) {
        String normalized = normalizePaymentMethod(method);
        if ("PayPal".equals(normalized)) {
            return "PAYPAL";
        }
        if ("Bank Transfer".equals(normalized)) {
            return "BANK";
        }
        return "STRIPE";
    }

    /**
     * Simulated Stripe / PayPal / bank gateway. Declines when the admin
     * requests a failed demo, or when test card digits end with 0002.
     */
    private boolean isGatewayDeclined(PaymentRequest request) {
        if (request.isSimulateFailure()) {
            return true;
        }
        String card = request.getCardDetails();
        if (card == null || card.isBlank()) {
            return false;
        }
        String digits = card.replaceAll("\\D", "");
        return digits.endsWith("0002");
    }
}
