package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;

public class ShippingCostCalculationRequest {

    private BigDecimal weight;
    private String deliveryLocation; // Local, Domestic, International
    private String shippingMethod;  // Standard, Express, Overnight

    public ShippingCostCalculationRequest() {
    }

    public ShippingCostCalculationRequest(BigDecimal weight, String deliveryLocation, String shippingMethod) {
        this.weight = weight;
        this.deliveryLocation = deliveryLocation;
        this.shippingMethod = shippingMethod;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }
}
