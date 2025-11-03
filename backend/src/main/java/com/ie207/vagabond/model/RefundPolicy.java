package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPolicy {
    private List<RefundPercentage> refundPercentage;
    private String description;
}
