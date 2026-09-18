package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;

public class ShippingRequest {

    private Long orderId;
    private String courierService;
    private String trackingNumber;
    private BigDecimal shippingCost;
    private BigDecimal weight;
    private String shippingMethod;
    private String deliveryLocation;

    public ShippingRequest() {
    }

    public ShippingRequest(Long orderId, String courierService, String trackingNumber, BigDecimal shippingCost) {
        this.orderId = orderId;
        this.courierService = courierService;
        this.trackingNumber = trackingNumber;
        this.shippingCost = shippingCost;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCourierService() {
        return courierService;
    }

    public void setCourierService(String courierService) {
        this.courierService = courierService;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }
}
