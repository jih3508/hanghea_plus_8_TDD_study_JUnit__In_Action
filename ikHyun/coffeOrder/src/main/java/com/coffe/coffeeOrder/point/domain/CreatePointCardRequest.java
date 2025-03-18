package com.coffe.coffeeOrder.point.domain;

import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
public class CreatePointCardRequest {

    @NonNull
    private String cardNumber;

    private BigDecimal point;
}
