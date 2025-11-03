package com.ie207.vagabond.request;

import lombok.Data;

import java.util.List;

@Data
public class RefundPolicyRequest {
    private List<RefundPercentageRequest> refundPercentage;
    private String description;
}