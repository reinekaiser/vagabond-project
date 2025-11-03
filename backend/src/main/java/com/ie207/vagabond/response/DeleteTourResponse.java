package com.ie207.vagabond.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteTourResponse {
    private String message;
    private String deletedTourId;
    private int deletedTicketCount;
}