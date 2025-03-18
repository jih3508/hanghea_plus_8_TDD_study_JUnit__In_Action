package com.coffe.coffeeOrder.point.repository;

import com.coffe.coffeeOrder.point.domain.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point,Long> {

    Optional<Point> findByCardNumber(String cardNumber);
}
