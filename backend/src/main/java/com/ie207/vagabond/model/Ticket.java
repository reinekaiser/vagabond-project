package com.ie207.vagabond.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "tickets")
public class Ticket {
    @Id
    private String id;

    private String title;
    private String subtitle;
    private String description;

    private List<PriceInfo> prices;

    private Integer maxQuantity;

    private String overview;
    private String voucherValidity;

    private RedemptionPolicy redemptionPolicy;
    private TicketCancellationPolicy cancellationPolicy;

    private String termsAndConditions;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
