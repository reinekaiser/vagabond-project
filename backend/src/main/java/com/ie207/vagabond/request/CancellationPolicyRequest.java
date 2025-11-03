package com.ie207.vagabond.request;

import lombok.Data;

@Data
public class CancellationPolicyRequest {
    private Boolean isReschedule;
    private String reschedulePolicy;
    private Boolean isRefund;
    private RefundPolicyRequest refundPolicy;
}