package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByCustomerUserIdOrderByCreatedAtDesc(Long customerId);

    Optional<Wishlist> findByCustomerUserIdAndProductId(Long customerId, Long productId);

    boolean existsByCustomerUserIdAndProductId(Long customerId, Long productId);

    void deleteByCustomerUserId(Long customerId);

    @Query("SELECT w FROM Wishlist w WHERE w.customer.userId = :customerId")
    List<Wishlist> findWishlistByCustomerId(@Param("customerId") Long customerId);
}