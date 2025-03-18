package com.coffe.coffeeOrder.coffee.service;

import com.coffe.coffeeOrder.coffee.domain.CoffeeListResponse;
import com.coffe.coffeeOrder.coffee.domain.CreateCoffeeRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CoffeeService {

    List<CoffeeListResponse> getList();

    ResponseEntity<Void> create(CreateCoffeeRequest request);
}
