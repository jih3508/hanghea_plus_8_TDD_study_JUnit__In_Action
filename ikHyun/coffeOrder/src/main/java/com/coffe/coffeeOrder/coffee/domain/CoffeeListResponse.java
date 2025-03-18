package com.coffe.coffeeOrder.coffee.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;


@Getter
@Setter
@ToString
public class CoffeeListResponse {

    private Long id;

    private String coffeeName;

    private BigDecimal coffeePrice;
}
