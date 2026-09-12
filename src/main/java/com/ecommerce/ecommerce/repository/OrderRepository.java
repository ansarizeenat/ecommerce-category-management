package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByOrderStatusIgnoreCaseOrderByCreatedAtDesc(String orderStatus);

    List<Order> findByStatusOrderByCreatedAtDesc(Boolean status);

    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId);
}
