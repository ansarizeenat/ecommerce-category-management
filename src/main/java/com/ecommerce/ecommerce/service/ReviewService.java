package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.model.*;
import com.ecommerce.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository,
                         OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * 1. Add a Review and Rating
     * Customers can add a review and rating (1 to 5 stars) for products they have purchased.
     */
    @Transactional
    public ReviewDTO addReview(ReviewRequest request) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required.");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be an integer between 1 and 5 stars.");
        }
        if (request.getReviewText() != null && request.getReviewText().length() > 1000) {
            throw new IllegalArgumentException("Review text cannot exceed 1000 characters.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.getProductId()));

        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));

        if (Boolean.FALSE.equals(customer.getStatus())) {
            throw new IllegalStateException("Deactivated customer accounts cannot submit reviews.");
        }

        boolean hasPurchased = hasCustomerPurchasedProduct(customer.getUserId(), product.getId());
        if (!hasPurchased && !request.isBypassPurchaseCheck()) {
            throw new IllegalStateException("Customers can only review products they have purchased. Please place an order for this product first.");
        }

        // Check if customer already reviewed this product -> update existing or create new
        Optional<Review> existingOpt = reviewRepository.findByProductIdAndCustomerId(product.getId(), customer.getUserId());
        Review review;
        if (existingOpt.isPresent()) {
            review = existingOpt.get();
            review.setRating(request.getRating());
            review.setReviewText(request.getReviewText());
            review.setVerifiedPurchase(hasPurchased);
            review.setStatus(false); // Reset to pending moderation upon edit
            review.setUpdatedAt(LocalDateTime.now());
        } else {
            review = new Review();
            review.setProduct(product);
            review.setCustomer(customer);
            review.setRating(request.getRating());
            review.setReviewText(request.getReviewText());
            review.setVerifiedPurchase(hasPurchased);
            review.setStatus(false); // Default to unapproved/pending moderation
            review.setCreatedAt(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());
        }

        Review saved = reviewRepository.save(review);
        return toDTO(saved);
    }

    /**
     * 2. Review Moderation
     * Admins can approve or reject reviews based on quality and adherence to guidelines.
     */
    @Transactional
    public ReviewDTO moderateReview(Long reviewId, boolean approve) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        review.setStatus(approve);
        review.setUpdatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        return toDTO(saved);
    }

    /**
     * 3. View Product Reviews
     * Retrieve approved reviews for a specific product.
     */
    public List<ReviewDTO> getProductReviews(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, true);
        List<ReviewDTO> dtos = new ArrayList<>();
        for (Review r : reviews) {
            dtos.add(toDTO(r));
        }
        return dtos;
    }

    /**
     * Product Rating Summary (Average rating, total reviews, and star breakdown)
     */
    public ProductRatingSummaryDTO getProductRatingSummary(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        List<Review> approvedReviews = reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, true);

        ProductRatingSummaryDTO summary = new ProductRatingSummaryDTO();
        summary.setProductId(product.getId());
        summary.setProductName(product.getName());

        long total = approvedReviews.size();
        summary.setTotalReviews(total);

        if (total == 0) {
            summary.setAverageRating(0.0);
            return summary;
        }

        long s5 = 0, s4 = 0, s3 = 0, s2 = 0, s1 = 0;
        int sumRating = 0;

        for (Review r : approvedReviews) {
            int rating = r.getRating() != null ? r.getRating() : 0;
            sumRating += rating;
            switch (rating) {
                case 5: s5++; break;
                case 4: s4++; break;
                case 3: s3++; break;
                case 2: s2++; break;
                case 1: s1++; break;
            }
        }

        double avg = (double) sumRating / total;
        BigDecimal roundedAvg = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
        summary.setAverageRating(roundedAvg.doubleValue());

        summary.setStar5Count(s5);
        summary.setStar4Count(s4);
        summary.setStar3Count(s3);
        summary.setStar2Count(s2);
        summary.setStar1Count(s1);

        summary.setStar5Percent((int) Math.round(((double) s5 / total) * 100));
        summary.setStar4Percent((int) Math.round(((double) s4 / total) * 100));
        summary.setStar3Percent((int) Math.round(((double) s3 / total) * 100));
        summary.setStar2Percent((int) Math.round(((double) s2 / total) * 100));
        summary.setStar1Percent((int) Math.round(((double) s1 / total) * 100));

        return summary;
    }

    /**
     * 4. Delete / Update Reviews
     */
    @Transactional
    public ReviewDTO updateReview(Long reviewId, Long customerId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        // If customerId is provided, verify ownership
        if (customerId != null && !review.getCustomer().getUserId().equals(customerId)) {
            throw new IllegalStateException("You can only update your own review.");
        }

        if (request.getRating() != null) {
            if (request.getRating() < 1 || request.getRating() > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
            }
            review.setRating(request.getRating());
        }

        if (request.getReviewText() != null) {
            if (request.getReviewText().length() > 1000) {
                throw new IllegalArgumentException("Review text cannot exceed 1000 characters.");
            }
            review.setReviewText(request.getReviewText());
        }

        // If updated by customer, send back to pending moderation
        if (customerId != null) {
            review.setStatus(false);
        }

        review.setUpdatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        return toDTO(saved);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long customerId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        if (!isAdmin && (customerId == null || !review.getCustomer().getUserId().equals(customerId))) {
            throw new IllegalStateException("You are not authorized to delete this review.");
        }

        reviewRepository.delete(review);
    }

    /**
     * Review Dashboard (Admin View - filter by status and product)
     */
    public List<ReviewDTO> getAllReviews(Boolean statusFilter, Long productIdFilter) {
        List<Review> reviews;

        if (productIdFilter != null && statusFilter != null) {
            reviews = reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productIdFilter, statusFilter);
        } else if (productIdFilter != null) {
            reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productIdFilter);
        } else if (statusFilter != null) {
            reviews = reviewRepository.findByStatusOrderByCreatedAtDesc(statusFilter);
        } else {
            reviews = reviewRepository.findAllByOrderByCreatedAtDesc();
        }

        List<ReviewDTO> dtos = new ArrayList<>();
        for (Review r : reviews) {
            dtos.add(toDTO(r));
        }
        return dtos;
    }

    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + id));
        return toDTO(review);
    }

    /**
     * Global Platform Review Statistics
     */
    public Map<String, Object> getReviewStats() {
        List<Review> all = reviewRepository.findAll();
        long approvedCount = 0;
        long pendingCount = 0;
        int totalStars = 0;

        for (Review r : all) {
            if (Boolean.TRUE.equals(r.getStatus())) {
                approvedCount++;
                if (r.getRating() != null) {
                    totalStars += r.getRating();
                }
            } else {
                pendingCount++;
            }
        }

        double avgPlatformRating = approvedCount > 0
                ? BigDecimal.valueOf((double) totalStars / approvedCount).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReviews", all.size());
        stats.put("approvedCount", approvedCount);
        stats.put("pendingCount", pendingCount);
        stats.put("averagePlatformRating", avgPlatformRating);
        return stats;
    }

    /**
     * Helper: Check if customer has purchased a given product in any non-cancelled order
     */
    public boolean hasCustomerPurchasedProduct(Long customerId, Long productId) {
        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(customerId);
        for (Order o : orders) {
            if (Boolean.TRUE.equals(o.getStatus()) && !"Cancelled".equalsIgnoreCase(o.getOrderStatus())) {
                if (o.getItems() != null) {
                    for (OrderItem item : o.getItems()) {
                        if (item.getProduct() != null && item.getProduct().getId().equals(productId)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Helper: Get list of distinct products purchased by a customer
     */
    public List<Product> getPurchasedProductsForCustomer(Long customerId) {
        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(customerId);
        Map<Long, Product> uniqueProducts = new LinkedHashMap<>();

        for (Order o : orders) {
            if (Boolean.TRUE.equals(o.getStatus()) && !"Cancelled".equalsIgnoreCase(o.getOrderStatus())) {
                if (o.getItems() != null) {
                    for (OrderItem item : o.getItems()) {
                        if (item.getProduct() != null) {
                            uniqueProducts.putIfAbsent(item.getProduct().getId(), item.getProduct());
                        }
                    }
                }
            }
        }

        return new ArrayList<>(uniqueProducts.values());
    }

    public ReviewDTO toDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setProductId(review.getProduct() != null ? review.getProduct().getId() : null);
        dto.setProductName(review.getProduct() != null ? review.getProduct().getName() : "N/A");
        dto.setCustomerId(review.getCustomer() != null ? review.getCustomer().getUserId() : null);
        dto.setCustomerName(review.getCustomer() != null ? review.getCustomer().getName() : "N/A");
        dto.setRating(review.getRating());
        dto.setReviewText(review.getReviewText());
        dto.setStatus(review.getStatus());
        dto.setVerifiedPurchase(review.getVerifiedPurchase());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }
}
