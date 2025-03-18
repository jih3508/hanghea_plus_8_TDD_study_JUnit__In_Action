package com.coffe.coffeeOrder.order.repository;

import com.coffe.coffeeOrder.order.domain.OrderGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderGroupRepository extends JpaRepository<OrderGroup, Long> {

    Optional<OrderGroup> findByOrderNumber(String orderNumber);
}
