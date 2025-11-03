package com.ie207.vagabond.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateTicketRequest {
    private String title;
    private String subtitle;
    private String description;
    private List<PriceInfoRequest> prices;
    private Integer maxQuantity;
    private String overview;
    private String voucherValidity;
    private RedemptionPolicyRequest redemptionPolicy;
    private CancellationPolicyRequest cancellationPolicy;
    private String termsAndConditions;
}
