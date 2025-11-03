package com.ie207.vagabond.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancellationPolicy {
    private String refund;
    private Integer day;
    private Integer percentBeforeDay;
    private Integer percentAfterDay;
}
