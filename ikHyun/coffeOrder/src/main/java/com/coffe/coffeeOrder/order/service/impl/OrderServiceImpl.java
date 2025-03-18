package com.coffe.coffeeOrder.order.service.impl;

import com.coffe.coffeeOrder.coffee.domain.Coffee;
import com.coffe.coffeeOrder.coffee.repository.CoffeeRepository;
import com.coffe.coffeeOrder.order.domain.Order;
import com.coffe.coffeeOrder.order.domain.OrderGroup;
import com.coffe.coffeeOrder.order.domain.OrderRequest;
import com.coffe.coffeeOrder.order.repository.OrderGroupRepository;
import com.coffe.coffeeOrder.order.repository.OrderRepository;
import com.coffe.coffeeOrder.order.service.OrderService;
import com.coffe.coffeeOrder.point.domain.Point;
import com.coffe.coffeeOrder.point.repository.PointRepository;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final OrderGroupRepository orderGroupRepository;
    private final CoffeeRepository coffeeRepository;
    private final PointRepository pointRepository;

    /*
     * method: order
     * description: 주문 하기
     */
    @Override
    @Transactional
    public ResponseEntity<Void> order(OrderRequest request) {

        // 포인트 카드 정보 가져 오기
        Point point = pointRepository.findByCardNumber(request.getCardNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        OrderGroup orderGroup = createOrderNumber();

        // 총 금액
        BigDecimal totalPrice = new BigDecimal(0);



        for(OrderRequest.OrderCoffeeList orderCoffee : request.getCoffeeList()) {
            Coffee coffee = coffeeRepository.findById(orderCoffee.getCoffeeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            BigDecimal coffeePrice = coffee.getPrice().multiply(new BigDecimal(orderCoffee.getQuantity()));
            Order order = Order.builder()
                    .coffee(coffee)
                    .quantity(orderCoffee.getQuantity())
                    .orderGroup(orderGroup)
                    .price(coffeePrice)
                    .build();
            repository.save(order);

            coffee.addHist(order.getQuantity());
            coffeeRepository.save(coffee);

            totalPrice = totalPrice.add(coffeePrice);

        }

        orderGroup.setTotalPrice(totalPrice);

        return ResponseEntity.ok().build();
    }

    /*
     * 주문 번호 그룹 생성 하기
     */
    private OrderGroup createOrderNumber() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();
        String formatDate = now.format(formatter);
        String orderNumber = null;
        OrderGroup orderGroup = null;
        int sequence = 0;
        do{
            orderNumber = formatDate + String.format("%04d", sequence++);
            orderGroup = orderGroupRepository.findByOrderNumber(orderNumber).orElse(null);
            if(orderGroup == null) {
                orderGroup = OrderGroup.builder()
                        .orderNumber(orderNumber)
                        .build();
                orderGroupRepository.save(orderGroup);
                break;
            }
        }while (true);


        return orderGroupRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
