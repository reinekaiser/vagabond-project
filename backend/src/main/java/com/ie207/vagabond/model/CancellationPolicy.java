package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancellationPolicy {
    private String refund;
    private Integer day;
    private Integer percentBeforeDay;
    private Integer percentAfterDay;
}
