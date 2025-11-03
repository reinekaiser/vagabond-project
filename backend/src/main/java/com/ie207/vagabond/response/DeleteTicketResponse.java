package com.ie207.vagabond.response;

import com.ie207.vagabond.model.Tour;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteTicketResponse {
    private String message;
    private boolean tourDeleted;
    private Tour tour;
}