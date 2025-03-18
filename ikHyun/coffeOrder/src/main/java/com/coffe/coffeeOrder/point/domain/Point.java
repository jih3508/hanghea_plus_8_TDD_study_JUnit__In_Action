package com.coffe.coffeeOrder.point.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "point")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", unique = true)
    private String cardNumber;

    @Column(columnDefinition = "default 0")
    private BigDecimal point; // 잔여 포인트

    /*
     * 포인트 충전하기
     */
    public void addPoint(BigDecimal point) {
        this.point = this.point.add(point);
    }

    /*
     * 포인트 차감하기
     */

    public void minusPoint(BigDecimal point) {
        if(this.point.compareTo(point) < 0) {
            throw new IllegalArgumentException("포인트가 부족 합니다.");
        }
        this.point = this.point.subtract(point);
    }
}
