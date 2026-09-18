package com.ecommerce.ecommerce.dto;

import java.math.BigDecimal;

public class ShippingCostCalculationResponse {

    private BigDecimal baseCost;
    private BigDecimal weightCost;
    private BigDecimal locationMultiplier;
    private BigDecimal methodMultiplier;
    private BigDecimal totalShippingCost;
    private int estimatedDeliveryDays;
    private String breakdown;

    public ShippingCostCalculationResponse() {
    }

    public BigDecimal getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(BigDecimal baseCost) {
        this.baseCost = baseCost;
    }

    public BigDecimal getWeightCost() {
        return weightCost;
    }

    public void setWeightCost(BigDecimal weightCost) {
        this.weightCost = weightCost;
    }

    public BigDecimal getLocationMultiplier() {
        return locationMultiplier;
    }

    public void setLocationMultiplier(BigDecimal locationMultiplier) {
        this.locationMultiplier = locationMultiplier;
    }

    public BigDecimal getMethodMultiplier() {
        return methodMultiplier;
    }

    public void setMethodMultiplier(BigDecimal methodMultiplier) {
        this.methodMultiplier = methodMultiplier;
    }

    public BigDecimal getTotalShippingCost() {
        return totalShippingCost;
    }

    public void setTotalShippingCost(BigDecimal totalShippingCost) {
        this.totalShippingCost = totalShippingCost;
    }

    public int getEstimatedDeliveryDays() {
        return estimatedDeliveryDays;
    }

    public void setEstimatedDeliveryDays(int estimatedDeliveryDays) {
        this.estimatedDeliveryDays = estimatedDeliveryDays;
    }

    public String getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(String breakdown) {
        this.breakdown = breakdown;
    }
}
