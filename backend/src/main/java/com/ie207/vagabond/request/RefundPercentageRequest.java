package com.ie207.vagabond.request;

import lombok.Data;

@Data
public class RefundPercentageRequest {
    private Integer daysBefore;
    private Double percent;
}
