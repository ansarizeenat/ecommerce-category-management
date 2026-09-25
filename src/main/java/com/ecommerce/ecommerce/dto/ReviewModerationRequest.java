package com.ecommerce.ecommerce.dto;

public class ReviewModerationRequest {

    private Boolean status;

    public ReviewModerationRequest() {
    }

    public ReviewModerationRequest(Boolean status) {
        this.status = status;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
