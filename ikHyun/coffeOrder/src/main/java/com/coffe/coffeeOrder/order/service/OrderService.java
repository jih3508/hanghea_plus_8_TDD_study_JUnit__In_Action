package com.coffe.coffeeOrder.order.service;

import com.coffe.coffeeOrder.order.domain.OrderRequest;
import org.springframework.http.ResponseEntity;

public interface OrderService {

    ResponseEntity<Void> order(OrderRequest request);
}
