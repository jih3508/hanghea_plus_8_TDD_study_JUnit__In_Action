package com.coffe.coffeeOrder.coffee.repository;

import com.coffe.coffeeOrder.coffee.domain.Coffee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface CoffeeRepository extends JpaRepository<Coffee, BigInteger> {
}
