package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.model.Order;
import com.ecommerce.ecommerce.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrderByCreatedAtDesc();

    List<Payment> findByPaymentStatusIgnoreCaseOrderByCreatedAtDesc(String status);

    List<Payment> findByOrderOrderByCreatedAtDesc(Order order);

    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId ORDER BY p.createdAt DESC")
    List<Payment> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p WHERE p.order.id = :orderId AND LOWER(p.paymentStatus) = LOWER(:status)")
    boolean existsByOrderIdAndPaymentStatus(@Param("orderId") Long orderId, @Param("status") String status);
}
