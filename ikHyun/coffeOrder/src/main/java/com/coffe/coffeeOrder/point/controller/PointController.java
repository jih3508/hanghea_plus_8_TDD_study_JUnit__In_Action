package com.coffe.coffeeOrder.point.controller;

import com.coffe.coffeeOrder.point.domain.CreatePointCardRequest;
import com.coffe.coffeeOrder.point.domain.RefillCardPointRequest;
import com.coffe.coffeeOrder.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService service;

    @PostMapping
    ResponseEntity<Void> create(@RequestBody final CreatePointCardRequest request){
        return service.create(request);
    }

    /*
     * method: refill
     * description: 포인트 충전 하기
     */
    @PatchMapping("/refill")
    ResponseEntity<Void> refill(@RequestBody final RefillCardPointRequest request){
        return service.pointRefill(request);
    }
}
