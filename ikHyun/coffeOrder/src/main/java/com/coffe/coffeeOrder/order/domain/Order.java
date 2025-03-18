package com.coffe.coffeeOrder.order.domain;

import com.coffe.coffeeOrder.coffee.domain.Coffee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Coffee coffee;

    private Long quantity; // 수량

    private BigDecimal price; // 커피 항목당 구매

    @ManyToOne(cascade = CascadeType.ALL)
    private OrderGroup orderGroup;

}
