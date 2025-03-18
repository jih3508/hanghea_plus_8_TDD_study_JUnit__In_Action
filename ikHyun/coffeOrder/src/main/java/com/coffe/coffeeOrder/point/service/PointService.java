package com.coffe.coffeeOrder.point.service;

import com.coffe.coffeeOrder.point.domain.CreatePointCardRequest;
import com.coffe.coffeeOrder.point.domain.RefillCardPointRequest;
import org.springframework.http.ResponseEntity;

public interface PointService {

    ResponseEntity<Void> create(CreatePointCardRequest request);

    ResponseEntity<Void> pointRefill(RefillCardPointRequest request);
}
