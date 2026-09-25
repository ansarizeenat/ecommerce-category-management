package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 1. Get all reviews (Admin Dashboard) with optional status & product filtering
     */
    @GetMapping
    public List<ReviewDTO> getAllReviews(@RequestParam(required = false) Boolean status,
                                         @RequestParam(required = false) Long productId) {
        return reviewService.getAllReviews(status, productId);
    }

    /**
     * Platform review statistics
     */
    @GetMapping("/stats")
    public Map<String, Object> getReviewStats() {
        return reviewService.getReviewStats();
    }

    /**
     * Get review by ID
     */
    @GetMapping("/{id}")
    public ReviewDTO getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    /**
     * 2. View Product Reviews (Customer View - Approved only)
     */
    @GetMapping("/product/{productId}")
    public List<ReviewDTO> getProductReviews(@PathVariable Long productId) {
        return reviewService.getProductReviews(productId);
    }

    /**
     * Product Rating Summary (Average rating & star breakdown)
     */
    @GetMapping("/product/{productId}/summary")
    public ProductRatingSummaryDTO getProductRatingSummary(@PathVariable Long productId) {
        return reviewService.getProductRatingSummary(productId);
    }

    /**
     * Helper: Get products purchased by a customer
     */
    @GetMapping("/customer/{customerId}/purchased-products")
    public List<Product> getPurchasedProducts(@PathVariable Long customerId) {
        return reviewService.getPurchasedProductsForCustomer(customerId);
    }

    /**
     * 3. Add a Review and Rating
     */
    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest request) {
        try {
            ReviewDTO created = reviewService.addReview(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not submit review: " + e.getMessage()));
        }
    }

    /**
     * 4. Review Moderation (Approve / Reject)
     */
    @PutMapping("/{id}/moderate")
    public ResponseEntity<?> moderateReview(@PathVariable Long id, @RequestBody ReviewModerationRequest request) {
        try {
            boolean approve = request != null && Boolean.TRUE.equals(request.getStatus());
            ReviewDTO moderated = reviewService.moderateReview(id, approve);
            return ResponseEntity.ok(moderated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not moderate review: " + e.getMessage()));
        }
    }

    /**
     * 5. Update Review
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id,
                                         @RequestParam(required = false) Long customerId,
                                         @RequestBody ReviewUpdateRequest request) {
        try {
            ReviewDTO updated = reviewService.updateReview(id, customerId, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not update review: " + e.getMessage()));
        }
    }

    /**
     * 6. Delete Review (Admin or Review Owner)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id,
                                         @RequestParam(required = false) Long customerId,
                                         @RequestParam(defaultValue = "false") boolean isAdmin) {
        try {
            reviewService.deleteReview(id, customerId, isAdmin);
            return ResponseEntity.ok(Map.of("message", "Review #" + id + " deleted successfully."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not delete review: " + e.getMessage()));
        }
    }
}
