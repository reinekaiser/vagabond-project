package com.ie207.vagabond.request;

import lombok.*;

import java.util.List;


@Data
public class UpdateTicketRequest {
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

    // Không có createdAt, updatedAt - các trường này sẽ tự động được quản lý
}