package com.coffe.coffeeOrder.order.repository;

import com.coffe.coffeeOrder.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
