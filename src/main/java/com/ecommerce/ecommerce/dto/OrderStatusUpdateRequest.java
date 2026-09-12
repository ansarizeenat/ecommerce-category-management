package com.ecommerce.ecommerce.dto;

public class OrderStatusUpdateRequest {

    private String orderStatus;

    public OrderStatusUpdateRequest() {
    }

    public OrderStatusUpdateRequest(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
