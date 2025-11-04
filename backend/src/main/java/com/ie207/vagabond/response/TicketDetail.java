package com.ie207.vagabond.response;

import com.ie207.vagabond.model.PriceInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetail {
    private String _id;
    private String title;
    private List<PriceInfo> prices;
}
