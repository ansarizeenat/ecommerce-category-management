package com.ecommerce.ecommerce.dto;

public class ProductRatingSummaryDTO {

    private Long productId;
    private String productName;
    private Double averageRating = 0.0;
    private Long totalReviews = 0L;
    private Long star5Count = 0L;
    private Long star4Count = 0L;
    private Long star3Count = 0L;
    private Long star2Count = 0L;
    private Long star1Count = 0L;
    private Integer star5Percent = 0;
    private Integer star4Percent = 0;
    private Integer star3Percent = 0;
    private Integer star2Percent = 0;
    private Integer star1Percent = 0;

    public ProductRatingSummaryDTO() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Long getStar5Count() {
        return star5Count;
    }

    public void setStar5Count(Long star5Count) {
        this.star5Count = star5Count;
    }

    public Long getStar4Count() {
        return star4Count;
    }

    public void setStar4Count(Long star4Count) {
        this.star4Count = star4Count;
    }

    public Long getStar3Count() {
        return star3Count;
    }

    public void setStar3Count(Long star3Count) {
        this.star3Count = star3Count;
    }

    public Long getStar2Count() {
        return star2Count;
    }

    public void setStar2Count(Long star2Count) {
        this.star2Count = star2Count;
    }

    public Long getStar1Count() {
        return star1Count;
    }

    public void setStar1Count(Long star1Count) {
        this.star1Count = star1Count;
    }

    public Integer getStar5Percent() {
        return star5Percent;
    }

    public void setStar5Percent(Integer star5Percent) {
        this.star5Percent = star5Percent;
    }

    public Integer getStar4Percent() {
        return star4Percent;
    }

    public void setStar4Percent(Integer star4Percent) {
        this.star4Percent = star4Percent;
    }

    public Integer getStar3Percent() {
        return star3Percent;
    }

    public void setStar3Percent(Integer star3Percent) {
        this.star3Percent = star3Percent;
    }

    public Integer getStar2Percent() {
        return star2Percent;
    }

    public void setStar2Percent(Integer star2Percent) {
        this.star2Percent = star2Percent;
    }

    public Integer getStar1Percent() {
        return star1Percent;
    }

    public void setStar1Percent(Integer star1Percent) {
        this.star1Percent = star1Percent;
    }
}
