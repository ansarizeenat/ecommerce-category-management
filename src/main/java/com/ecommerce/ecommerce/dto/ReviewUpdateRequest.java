package com.ecommerce.ecommerce.dto;

public class ReviewUpdateRequest {

    private Integer rating;
    private String reviewText;

    public ReviewUpdateRequest() {
    }

    public ReviewUpdateRequest(Integer rating, String reviewText) {
        this.rating = rating;
        this.reviewText = reviewText;
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
}
