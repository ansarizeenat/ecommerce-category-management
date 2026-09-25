package com.ecommerce.ecommerce;

import com.ecommerce.ecommerce.dto.*;
import com.ecommerce.ecommerce.entity.Category;
import com.ecommerce.ecommerce.model.*;
import com.ecommerce.ecommerce.repository.*;
import com.ecommerce.ecommerce.service.OrderService;
import com.ecommerce.ecommerce.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderService orderService;

    private User testCustomer;
    private User otherCustomer;
    private Product testProduct;
    private Product unpurchasedProduct;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setCategoryName("Audio Gadgets");
        category.setDescription("Sound and acoustics");
        category = categoryRepository.save(category);

        testProduct = new Product();
        testProduct.setName("Noise Cancelling Headphones");
        testProduct.setPrice(150.00);
        testProduct.setSku("NCH-01");
        testProduct.setInventoryCount(20);
        testProduct.setCategory(category);
        testProduct.setStatus(true);
        testProduct = productRepository.save(testProduct);

        unpurchasedProduct = new Product();
        unpurchasedProduct.setName("Studio Microphone");
        unpurchasedProduct.setPrice(90.00);
        unpurchasedProduct.setSku("MIC-01");
        unpurchasedProduct.setInventoryCount(15);
        unpurchasedProduct.setCategory(category);
        unpurchasedProduct.setStatus(true);
        unpurchasedProduct = productRepository.save(unpurchasedProduct);

        testCustomer = new User();
        testCustomer.setFirstName("Zeenat");
        testCustomer.setLastName("Ansari");
        testCustomer.setEmail("zeenat.rev" + System.currentTimeMillis() + "@example.com");
        testCustomer.setPhone("+91 9988776655");
        testCustomer.setStatus(true);
        testCustomer = userRepository.save(testCustomer);

        otherCustomer = new User();
        otherCustomer.setFirstName("Rahul");
        otherCustomer.setLastName("Sharma");
        otherCustomer.setEmail("rahul.rev" + System.currentTimeMillis() + "@example.com");
        otherCustomer.setPhone("+91 9123456780");
        otherCustomer.setStatus(true);
        otherCustomer = userRepository.save(otherCustomer);

        // Place an order for testCustomer with testProduct so purchase verification passes
        OrderRequest orderReq = new OrderRequest();
        orderReq.setUserId(testCustomer.getUserId());
        orderReq.setCustomerName("Zeenat Ansari");
        orderReq.setCustomerEmail(testCustomer.getEmail());
        orderReq.setShippingAddress("Kurla, Mumbai");
        orderReq.setItems(List.of(new OrderItemRequest(testProduct.getId(), 1)));
        orderService.placeOrder(orderReq);
    }

    @Test
    void testAddReview_Success() {
        ReviewRequest req = new ReviewRequest(testProduct.getId(), testCustomer.getUserId(), 5, "Outstanding sound quality and active noise cancellation!");
        ReviewDTO created = reviewService.addReview(req);

        assertNotNull(created.getReviewId());
        assertEquals(5, created.getRating());
        assertEquals("Outstanding sound quality and active noise cancellation!", created.getReviewText());
        assertFalse(created.getStatus(), "New review must default to unapproved / pending moderation");
        assertTrue(created.getVerifiedPurchase(), "Customer has purchased the product");
    }

    @Test
    void testAddReview_UnpurchasedProduct_ThrowsException() {
        ReviewRequest req = new ReviewRequest(unpurchasedProduct.getId(), testCustomer.getUserId(), 4, "Looks nice, haven't bought it yet.");
        assertThrows(IllegalStateException.class, () -> reviewService.addReview(req));
    }

    @Test
    void testAddReview_InvalidRating_ThrowsException() {
        ReviewRequest reqZero = new ReviewRequest(testProduct.getId(), testCustomer.getUserId(), 0, "Too low rating");
        assertThrows(IllegalArgumentException.class, () -> reviewService.addReview(reqZero));

        ReviewRequest reqSix = new ReviewRequest(testProduct.getId(), testCustomer.getUserId(), 6, "Rating too high");
        assertThrows(IllegalArgumentException.class, () -> reviewService.addReview(reqSix));
    }

    @Test
    void testModerateReview_ApproveAndReject() {
        ReviewRequest req = new ReviewRequest(testProduct.getId(), testCustomer.getUserId(), 5, "Great headphones!");
        ReviewDTO created = reviewService.addReview(req);

        // Approve
        ReviewDTO approved = reviewService.moderateReview(created.getReviewId(), true);
        assertTrue(approved.getStatus());

        // Reject / Unapprove
        ReviewDTO rejected = reviewService.moderateReview(created.getReviewId(), false);
        assertFalse(rejected.getStatus());
    }

    @Test
    void testGetProductReviews_OnlyApprovedReviews() {
        ReviewRequest req = new ReviewRequest(testProduct.getId(), testCustomer.getUserId(), 4, "Good build quality.");
        ReviewDTO created = reviewService.addReview(req);

        // Initially unapproved: should not appear in public product reviews
        List<ReviewDTO> publicReviewsBefore = reviewService.getProductReviews(testProduct.getId());
        assertEquals(0, publicReviewsBefore.size());

        // Once approved: appears in public product reviews
        reviewService.moderateReview(created.getReviewId(), true);
        List<ReviewDTO> publicReviewsAfter = reviewService.getProductReviews(testProduct.getId());
        assertEquals(1, publicReviewsAfter.size());
        assertEquals(created.getReviewId(), publicReviewsAfter.get(0).getReviewId());
    }

    @Test
    void testGetProductRatingSummary() {
        ReviewRequest req = new ReviewRequest(testProduct.getId(), testCustomer.getUserId(), 5, "Best purchase ever!");
        ReviewDTO created = reviewService.addReview(req);
        reviewService.moderateReview(created.getReviewId(), true);

        ProductRatingSummaryDTO summary = reviewService.getProductRatingSummary(testProduct.getId());

        assertNotNull(summary);
        assertEquals(5.0, summary.getAverageRating());
        assertEquals(1L, summary.getTotalReviews());
        assertEquals(1L, summary.getStar5Count());
        assertEquals(100, summary.getStar5Percent());
    }

    @Test
    void testUpdateReview_Success() {
        ReviewRequest req = new ReviewRequest(testProduct.getId(), testCustomer.getUserId(), 4, "Very good!");
        ReviewDTO created = reviewService.addReview(req);
        reviewService.moderateReview(created.getReviewId(), true);

        // Customer edits their review
        ReviewUpdateRequest updateReq = new ReviewUpdateRequest(5, "Updated: After burning in, sound is even better!");
        ReviewDTO updated = reviewService.updateReview(created.getReviewId(), testCustomer.getUserId(), updateReq);

        assertEquals(5, updated.getRating());
        assertEquals("Updated: After burning in, sound is even better!", updated.getReviewText());
        assertFalse(updated.getStatus(), "Edited review by customer must return to pending moderation");
    }

    @Test
    void testDeleteReview_AdminAndOwner() {
        ReviewRequest req = new ReviewRequest(testProduct.getId(), testCustomer.getUserId(), 3, "Decent.");
        ReviewDTO created = reviewService.addReview(req);

        // Unauthorized user attempts deletion -> throws IllegalStateException
        assertThrows(IllegalStateException.class, () -> reviewService.deleteReview(created.getReviewId(), otherCustomer.getUserId(), false));

        // Admin deletes review -> success
        reviewService.deleteReview(created.getReviewId(), null, true);
        assertFalse(reviewRepository.findById(created.getReviewId()).isPresent());
    }

    @Test
    void testGetReviewStats() {
        ReviewRequest req = new ReviewRequest(testProduct.getId(), testCustomer.getUserId(), 5, "Five star product!");
        ReviewDTO created = reviewService.addReview(req);
        reviewService.moderateReview(created.getReviewId(), true);

        Map<String, Object> stats = reviewService.getReviewStats();
        assertNotNull(stats);
        assertTrue((int) stats.get("totalReviews") >= 1);
        assertTrue((long) stats.get("approvedCount") >= 1);
        assertTrue((double) stats.get("averagePlatformRating") > 0);
    }
}
