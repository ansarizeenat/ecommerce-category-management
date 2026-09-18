package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.model.Order;
import com.ecommerce.ecommerce.model.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShippingRepository extends JpaRepository<Shipping, Long> {

    List<Shipping> findAllByOrderByCreatedAtDesc();

    List<Shipping> findByShippingStatusIgnoreCaseOrderByCreatedAtDesc(String status);

    Optional<Shipping> findByTrackingNumberIgnoreCase(String trackingNumber);

    List<Shipping> findByOrderOrderByCreatedAtDesc(Order order);

    @Query("SELECT s FROM Shipping s WHERE s.order.id = :orderId ORDER BY s.createdAt DESC")
    List<Shipping> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId);

    boolean existsByTrackingNumber(String trackingNumber);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Shipping s WHERE s.order.id = :orderId")
    boolean existsByOrderId(@Param("orderId") Long orderId);
}
