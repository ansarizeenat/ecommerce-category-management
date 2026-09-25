package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByOrderByCreatedAtDesc();

    List<Review> findByStatusOrderByCreatedAtDesc(Boolean status);

    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.status = :status ORDER BY r.createdAt DESC")
    List<Review> findByProductIdAndStatusOrderByCreatedAtDesc(@Param("productId") Long productId, @Param("status") Boolean status);

    @Query("SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.createdAt DESC")
    List<Review> findByProductIdOrderByCreatedAtDesc(@Param("productId") Long productId);

    @Query("SELECT r FROM Review r WHERE r.customer.userId = :customerId ORDER BY r.createdAt DESC")
    List<Review> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.customer.userId = :customerId")
    Optional<Review> findByProductIdAndCustomerId(@Param("productId") Long productId, @Param("customerId") Long customerId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.product.id = :productId AND r.customer.userId = :customerId")
    boolean existsByProductIdAndCustomerId(@Param("productId") Long productId, @Param("customerId") Long customerId);

    long countByStatus(Boolean status);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.status = :status")
    long countByProductIdAndStatus(@Param("productId") Long productId, @Param("status") Boolean status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.status = true")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.status = true GROUP BY r.rating")
    List<Object[]> countReviewsByRatingForProduct(@Param("productId") Long productId);
}
