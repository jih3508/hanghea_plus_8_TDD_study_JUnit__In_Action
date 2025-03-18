package com.coffe.coffeeOrder.point.domain;

import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
public class RefillCardPointRequest {

    @NonNull
    private String cardNumber;

    @NonNull
    private BigDecimal addPoint;
}
