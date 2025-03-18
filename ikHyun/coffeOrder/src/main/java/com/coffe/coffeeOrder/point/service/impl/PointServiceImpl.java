package com.coffe.coffeeOrder.point.service.impl;

import com.coffe.coffeeOrder.point.domain.CreatePointCardRequest;
import com.coffe.coffeeOrder.point.domain.Point;
import com.coffe.coffeeOrder.point.domain.RefillCardPointRequest;
import com.coffe.coffeeOrder.point.repository.PointRepository;
import com.coffe.coffeeOrder.point.service.PointService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointServiceImpl implements PointService {


    private final PointRepository repository;

    @Override
    @Transactional
    public ResponseEntity<Void> create(CreatePointCardRequest request) {
        Point point = Point.builder()
                .cardNumber(request.getCardNumber())
                .build();

        if(request.getPoint() != null){
            point.setPoint(request.getPoint());
        }

        repository.save(point);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /*
     * method: pointRefill
     * description: 포인트 충전하기
     */
    @Override
    @Transactional
    public ResponseEntity<Void> pointRefill(RefillCardPointRequest request) {

        Point point = repository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        point.addPoint(request.getAddPoint());
        return ResponseEntity.ok().build();
    }
}
