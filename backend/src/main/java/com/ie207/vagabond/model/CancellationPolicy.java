package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationPolicy {
    private Boolean isReschedule;
    private String reschedulePolicy;
    private Boolean isRefund;
    private RefundPolicy refundPolicy;
}

