package com.coffe.coffeeOrder.order.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, name = "group_number")
    private String orderNumber; // 주문 번호

    @Column(nullable = false, name = "total_price")
    private BigDecimal totalPrice; // 총 금액

    @CreationTimestamp
    private LocalDateTime orderDateTime;
}
