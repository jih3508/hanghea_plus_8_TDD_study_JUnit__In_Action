package com.coffe.coffeeOrder.order.domain;

import lombok.*;

import java.math.BigInteger;
import java.util.List;

@Data
public class OrderRequest {

    private String cardNumber;

    private List<OrderCoffeeList> coffeeList;

    @Getter
    @Setter
    @ToString
    public static class OrderCoffeeList{

        @NonNull
        private BigInteger coffeeId;

        @NonNull
        private Long quantity; // 수량
    }
}
