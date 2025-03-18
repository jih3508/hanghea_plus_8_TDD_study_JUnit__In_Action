package com.coffe.coffeeOrder.coffee.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;

@Entity
@Table(name = "coffee")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coffee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    @NonNull
    private String name;

    @NonNull
    private BigDecimal price;

    @Column(columnDefinition = "default 0")
    private Long hits; // 주문 횟수

}
