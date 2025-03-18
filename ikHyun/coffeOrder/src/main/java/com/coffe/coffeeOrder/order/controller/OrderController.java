package com.coffe.coffeeOrder.order.controller;

import com.coffe.coffeeOrder.order.domain.OrderRequest;
import com.coffe.coffeeOrder.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/coffee")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    ResponseEntity<Void> order(@RequestBody final OrderRequest orderRequest){
        return service.order(orderRequest);
    }
}
