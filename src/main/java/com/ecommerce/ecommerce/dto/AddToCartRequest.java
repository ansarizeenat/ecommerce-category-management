package com.ecommerce.ecommerce.dto;

public class AddToCartRequest {

    private Long customerId;
    private Long productId;
    private Integer quantity;

    public AddToCartRequest() {
    }

    public AddToCartRequest(Long customerId, Long productId, Integer quantity) {
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
