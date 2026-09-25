package com.ecommerce.ecommerce.dto;

public class ReviewRequest {

    private Long productId;
    private Long customerId;
    private Integer rating;
    private String reviewText;
    private boolean bypassPurchaseCheck = false;

    public ReviewRequest() {
    }

    public ReviewRequest(Long productId, Long customerId, Integer rating, String reviewText) {
        this.productId = productId;
        this.customerId = customerId;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public boolean isBypassPurchaseCheck() {
        return bypassPurchaseCheck;
    }

    public void setBypassPurchaseCheck(boolean bypassPurchaseCheck) {
        this.bypassPurchaseCheck = bypassPurchaseCheck;
    }
}
