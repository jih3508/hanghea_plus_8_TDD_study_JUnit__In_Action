package com.coffe.coffeeOrder.coffee.domain;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCoffeeRequest {

    @NonNull
    private String coffeeName;

    @NonNull
    private BigDecimal coffeePrice;

}
