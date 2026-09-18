package com.ecommerce.ecommerce.dto;

public class ShippingUpdateRequest {

    private String courierService;
    private String trackingNumber;
    private String shippingStatus;

    public ShippingUpdateRequest() {
    }

    public ShippingUpdateRequest(String courierService, String trackingNumber, String shippingStatus) {
        this.courierService = courierService;
        this.trackingNumber = trackingNumber;
        this.shippingStatus = shippingStatus;
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

    public String getShippingStatus() {
        return shippingStatus;
    }

    public void setShippingStatus(String shippingStatus) {
        this.shippingStatus = shippingStatus;
    }
}
