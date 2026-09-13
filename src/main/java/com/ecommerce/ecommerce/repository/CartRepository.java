package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.model.Cart;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findAllByOrderByCreatedAtDesc();

    @Query("SELECT c FROM Cart c WHERE c.customer.userId = :customerId ORDER BY c.createdAt DESC")
    List<Cart> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    @Query("SELECT c FROM Cart c WHERE c.customer.userId = :customerId AND c.product.id = :productId")
    Optional<Cart> findByCustomerIdAndProductId(@Param("customerId") Long customerId, @Param("productId") Long productId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cart c WHERE c.customer.userId = :customerId AND c.product.id = :productId")
    boolean existsByCustomerIdAndProductId(@Param("customerId") Long customerId, @Param("productId") Long productId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.customer.userId = :customerId")
    void deleteByCustomerId(@Param("customerId") Long customerId);

    List<Cart> findByCustomer(User customer);

    List<Cart> findByProduct(Product product);

    @Query("SELECT COUNT(DISTINCT c.customer.userId) FROM Cart c")
    long countDistinctCustomers();

    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM Cart c")
    long sumAllQuantities();

    @Query("SELECT COALESCE(SUM(c.totalPrice), 0) FROM Cart c")
    java.math.BigDecimal sumAllTotalPrices();
}
