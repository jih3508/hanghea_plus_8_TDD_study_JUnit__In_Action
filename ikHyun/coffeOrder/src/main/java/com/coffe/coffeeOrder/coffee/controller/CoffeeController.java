package com.coffe.coffeeOrder.coffee.controller;

import com.coffe.coffeeOrder.coffee.domain.CoffeeListResponse;
import com.coffe.coffeeOrder.coffee.domain.CreateCoffeeRequest;
import com.coffe.coffeeOrder.coffee.service.CoffeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/coffee")
@RequiredArgsConstructor
public class CoffeeController {

    private final CoffeeService service;

    @PostMapping
    ResponseEntity<Void> create(@RequestBody final CreateCoffeeRequest request){
        return service.create(request);
    }

    /*
     * method : getList
     * description: 전체 메뉴 뽑기
     */
    @GetMapping("")
    List<CoffeeListResponse> getList(){
        return service.getList();
    }



}
