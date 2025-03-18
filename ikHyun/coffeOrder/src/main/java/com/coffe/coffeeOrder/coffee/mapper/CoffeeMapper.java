package com.coffe.coffeeOrder.coffee.mapper;

import com.coffe.coffeeOrder.coffee.domain.Coffee;
import com.coffe.coffeeOrder.coffee.domain.CoffeeListResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CoffeeMapper {

    public List<CoffeeListResponse> toCoffeeListResponse(List<Coffee> coffees);
}
