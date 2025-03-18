package com.coffe.coffeeOrder.coffee.service.impl;

import com.coffe.coffeeOrder.coffee.domain.Coffee;
import com.coffe.coffeeOrder.coffee.domain.CoffeeListResponse;
import com.coffe.coffeeOrder.coffee.domain.CreateCoffeeRequest;
import com.coffe.coffeeOrder.coffee.mapper.CoffeeMapper;
import com.coffe.coffeeOrder.coffee.repository.CoffeeRepository;
import com.coffe.coffeeOrder.coffee.service.CoffeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoffeeServiceImpl implements CoffeeService {

    private CoffeeRepository repository;

    private CoffeeMapper mapper;

    /*
     * method: getList
     * description: 전체 커피 목록 불려 오기
     */
    @Override
    public List<CoffeeListResponse> getList() {
        List<Coffee> coffees = repository.findAll();

        return mapper.toCoffeeListResponse(coffees);
    }

    /*
     * method: getList
     * description: 전체 등록하기
     */
    @Override
    public ResponseEntity<Void> create(CreateCoffeeRequest request) {
        Coffee coffee = Coffee.builder()
                .name(request.getCoffeeName())
                .price(request.getCoffeePrice())
                .build();
        repository.save(coffee);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
