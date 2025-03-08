# 색션 6. 주문 결제 API 개발

## POJO 주문 결제  기능 구현하기

### 주문 결제 관련 Class
- Payment → 결제 객체
- PaymentService → 결제 구현체
- PaymentRequest → 결제 요청 Class 
- PaymentPort
- PaymentAdapter 
- PaymentRepository → 결제 DB
- PaymentGateway → 
- ConsolePaymentGateway → 결제 연동

### Payment
```java
import com.example.productorderservice.order.Order;
import org.springframework.util.Assert;

class Payment {

    private Long id;

    private final Order order;
    private final String cardNumber;

    public Payment(final Order order, final String cardNumber) {
        Assert.notNull(order, "주문은 필수입니다.");
        Assert.hasText(cardNumber, "카드 번호는 필수입니다.");
        this.order = order;
        this.cardNumber = cardNumber;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public int getPrice(){
        return order.getTotalPrice();
    }

    public String getCardNumber(){
        return this.cardNumber;
    }
}

```
- order에  getTotalPrice(주문 총액) 추가 하기
```java
public int getTotalPrice() {
    return product.getDiscountedPrice() * this.quantity;
}
```
- price에  getDiscountedPrice()(할인 후 금액) 추가 하기
```java
public int getDiscountedPrice() {
    return discountPolicy.applyDiscount(this.price);
}
```
- DiscountPolicy applyDiscount(final int price)(할인 적용 한 금액) 추가 하기
  - `NONE`: 할인 X
  - `FIX_1000_AMOUNT` : 1000원 할인
```java
public enum DiscountPolicy {
    NONE{
        @Override
        int applyDiscount(int price) {
            return price;
        }
    },
    FIX_1000_AMOUNT{
        @Override
        int applyDiscount(int price) {
            return Math.max(price - 1000, 0);
        }
    };

    abstract int applyDiscount(final int price);
}
```

### PaymentService
```java
import com.example.productorderservice.order.Order;

class PaymentService {

    private PaymentPort paymentPort;

    public PaymentService(PaymentPort paymentPort) {
        this.paymentPort = paymentPort;
    }

    public void payment(final PaymentRequest request) {
        Order order = paymentPort.getOrder(request.orderId());

        final Payment payment = new Payment(order, request.cardNumber());

        paymentPort.pay(payment.getPrice(), payment.getCardNumber());
        paymentPort.save(payment);
    }
}

```
### PaymentRequest
```java
package com.example.productorderservice.payment;

import org.springframework.util.Assert;

record PaymentRequest(Long orderId, String cardNumber) {
    PaymentRequest {
        Assert.notNull(orderId, "주문 ID는 필수 입니다.");
        Assert.hasText(cardNumber, "카드 번호는 필수입니다.");
    }
}
```
### PaymentPort
```java
import com.example.productorderservice.order.Order;

interface PaymentPort {
    Order getOrder(Long orderId);

    void pay(int totalPrice, String cardNumber);

    void save(Payment payment);
}
```
### PaymentAdapter
```java
import com.example.productorderservice.order.Order;
import com.example.productorderservice.product.DiscountPolicy;
import com.example.productorderservice.product.Product;

class PaymentAdapter implements PaymentPort {

    private PaymentGateway paymentGateway;
    private PaymentRepository paymentRepository;

    public PaymentAdapter(PaymentGateway paymentGateway, PaymentRepository paymentRepository) {
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Order getOrder(final Long orderId) {
        return new Order(new Product("상품1", 1000, DiscountPolicy.NONE), 2);
    }

    @Override
    public void pay(int totalPrice, String cardNumber) {
        paymentGateway.excute(totalPrice, cardNumber);
    }

    @Override
    public void save(Payment payment) {
        paymentRepository.save(payment);
    }
}

```
### PaymentRepository
```java
import java.util.HashMap;
import java.util.Map;

class PaymentRepository {

    private Map<Long, Payment> persistence = new HashMap<>();
    private Long sequence = 0L;

    public void save(Payment payment) {
        payment.assignId(++sequence);
        persistence.put(payment.getId(), payment);
    }
}
```
### PaymentGateway
```java
interface PaymentGateway {
    void excute(int totalPrice, String cardNumber);
}
```

### ConsolePaymentGateway
```java
public class ConsolePaymentGateway implements PaymentGateway {

    @Override
    public void excute(int totalPrice, String cardNumber) {
        System.out.println("결제 완료");
    }
}
```
### Test
#### DiscountPolicyTest 추가 하기
- noneDiscountPolicy() : 할인이 없을때 테스트
- fix_1000_discounted_price(): 1000원 할인 테스트
- over_discounted_price(): 1000원 할인인데 금액이 1000원 보다 작을때 테스트
```java
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountPolicyTest {

    @Test
    void noneDiscountPolicy() {
        final int price = 1000;

        final int discountedPrice = DiscountPolicy.NONE.applyDiscount(price);

        assertThat(discountedPrice).isEqualTo(price);
    }

    @Test
    void fix_1000_discounted_price(){
        final int price = 2000;

        final int discountedPrice = DiscountPolicy.FIX_1000_AMOUNT.applyDiscount(price);

        assertThat(discountedPrice).isEqualTo(1000);
    }

    @Test
    void over_discounted_price() {
        final int price = 500;

        final int discountedPrice = DiscountPolicy.FIX_1000_AMOUNT.applyDiscount(price);

        assertThat(discountedPrice).isEqualTo(0);
    }
}
```
#### ProductTest에 할인 정책 추가
```java
    @Test
    void none_discounted_product() {
        final Product product = new Product("상품명", 1000, DiscountPolicy.NONE);

        final int discountedPrice = product.getDiscountedPrice();

        assertThat(discountedPrice).isEqualTo(1000);
    }

    @Test
    void fix_1000_discounted_price() {
        final Product product = new Product("상품명", 1000, DiscountPolicy.NONE);

        final int discountedPrice = product.getDiscountedPrice();

        assertThat(discountedPrice).isEqualTo(1000);
    }
```
#### OrderTest에 할인 후 금액 테스트
```java
import com.example.productorderservice.product.DiscountPolicy;
import com.example.productorderservice.product.Product;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Test
    void getTotalPrice() {
        final Order order =  new Order(new Product("상품명", 2000, DiscountPolicy.FIX_1000_AMOUNT), 2);

        final int totalPrice = order.getTotalPrice();

        assertThat(totalPrice).isEqualTo(2000);
    }
}
```
#### PaymentServiceTest 상품 주문 테스트
```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PaymentServiceTest {

    private PaymentService paymentService;

    private PaymentPort paymentPort;

    @BeforeEach
    void setUp(){
        final PaymentGateway paymentGateway = new ConsolePaymentGateway();
        final PaymentRepository paymentRepository = new PaymentRepository();
        paymentPort = new PaymentAdapter(paymentGateway,paymentRepository);
        paymentService = new PaymentService(paymentPort);

    }
    @Test
    void  상품주문(){
        final Long orderId = 1L;
        final String cardNumber = "1234-1234-1234-1234";
        final PaymentRequest request = 주문결제요청_생성();

        paymentService.payment(request);
    }

    private static PaymentRequest 주문결제요청_생성(){
        final Long orderId = 1L;
        final String cardNumber = "1234-1234-1234-1234";
        return new PaymentRequest(orderId, cardNumber);
    }
    
}
```
## 스프링부트 테스트로 전환하기

### Component 추가하기
- PaymentService
- ```java
  @Component
  public class PaymentService{
      
  }
  ```
- PaymentAdapter
- ```java
  @Component
  public class PaymentAdapter implements PaymentPort{
  }
  ```
- ConsolePaymentGateway
- ```java
  @Component
  public class ConsolePaymentGateway implements PaymentGateway {}
  ```

### PaymentSteps 주문결제요청_생성 분리 작업
```java
public class PaymentSteps {

    public static PaymentRequest 주문결제요청_생성(){
        final Long orderId = 1L;
        final String cardNumber = "1234-1234-1234-1234";
        return new PaymentRequest(orderId, cardNumber);
    }
}
```

### PaymentServiceTest
- `@SpringBootTest` 추가
- `@Autowired` 추가
```java
import com.example.productorderservice.order.OrderService;
import com.example.productorderservice.order.OrderSteps;
import com.example.productorderservice.product.ProductService;
import com.example.productorderservice.product.ProductSteps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PaymentServiceTest {

  @Autowired
  private PaymentService paymentService;

  @Autowired
  private OrderService orderService;

  @Autowired
  private ProductService productService;

  @Test
  void  상품주문(){
    productService.addProduct(ProductSteps.상품등록요청_생성());
    orderService.createOrder(OrderSteps.상품주문요청_생성());
    final PaymentRequest request = PaymentSteps.주문결제요청_생성();

    paymentService.payment(request);
  }

}
```

#### 결과 Log
```text
Hibernate: 
    insert 
    into
        products
        (id, discount_policy, name, price) 
    values
        (default, ?, ?, ?)
Hibernate: 
    select
        product0_.id as id1_1_0_,
        product0_.discount_policy as discount2_1_0_,
        product0_.name as name3_1_0_,
        product0_.price as price4_1_0_ 
    from
        products product0_ 
    where
        product0_.id=?
Hibernate: 
    insert 
    into
        orders
        (id, product_id, quantity) 
    values
        (default, ?, ?)
결제 완료
Execution finished ':test --tests "com.example.productorderservice.payment.PaymentServiceTest.상품주문"'.
```

## API 테스트로 전환하기

### OrderService에 Request Url 붙이기

```java
import com.example.productorderservice.product.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderService {

  private final OrderPort orderPort;

  OrderService(OrderPort orderPort) {
    this.orderPort = orderPort;
  }

  @PostMapping
  public ResponseEntity<Void> createOrder(@RequestBody final CreateOrderRequest request) {
    final Product product = orderPort.getProductById(request.productId());
    final Order order = new Order(product, request.quantity());

    orderPort.save(order);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
```

## API로테스트로 전환하기

### PaymentService Request Mapping URL에 추가
```java
@RestController
@RequestMapping("/payments")
public class PaymentService {

  private PaymentPort paymentPort;

  public PaymentService(PaymentPort paymentPort) {
    this.paymentPort = paymentPort;
  }

  @PostMapping
  public ResponseEntity<Void> payment(@RequestBody final PaymentRequest request) {
    Order order = paymentPort.getOrder(request.orderId());

    final Payment payment = new Payment(order, request.cardNumber());

    paymentPort.pay(payment.getPrice(), payment.getCardNumber());
    paymentPort.save(payment);
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
```

### PaymentSteps 주문결제요청 추가 하기
```java
    public static ExtractableResponse<Response> 주문결제요청(final PaymentRequest request){
        return RestAssured.given().log().all()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(request)
        .when()
        .post("/payments")
        .then().log().all()
        .extract();
        }
```
### PaymentApiTest에 상품 주문 테스트 하기
```java
import com.example.productorderservice.ApiTest;
import com.example.productorderservice.order.OrderSteps;
import com.example.productorderservice.product.ProductSteps;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentApiTest extends ApiTest {

    @Test
    void  상품주문(){
        ProductSteps.상품등록요청(ProductSteps.상품등록요청_생성());
        OrderSteps.상품주문요청(OrderSteps.상품주문요청_생성());

        final var response = PaymentSteps.주문결제요청(PaymentSteps.주문결제요청_생성());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}    
```
- 상품 등록 → 주문 결제 → 주문 결제

## JPA 적용하기
### OrderRepository
```java
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderRepository extends JpaRepository<Order, Long> {
}

```

### OrderSteps로 기능 분리 작업
```java
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.http.MediaType;

public class OrderSteps {

    public static CreateOrderRequest 상품주문요청_생성(){
        final Long productId = 1L;
        final int quantity = 2;
        return new CreateOrderRequest(productId, quantity);

    }

    public static ExtractableResponse<Response> 상품주문요청(final CreateOrderRequest request){
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/orders")
                .then()
                .log().all().extract();
    }
}
```

### OrderApiTest로 상품주문 테스트
- 상품 등록 → 상품 주문
```java
import com.example.productorderservice.ApiTest;
import com.example.productorderservice.product.ProductSteps;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderApiTest extends ApiTest {

    @Test
    void 상품주문(){
        ProductSteps.상품등록요청(ProductSteps.상품등록요청_생성());

        final var response = OrderSteps.상품주문요청(OrderSteps.상품주문요청_생성());
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

    }

}

```

##### 결과 Log
```text
Hibernate: 
    
    drop table if exists orders CASCADE 
Hibernate: 
    
    create table orders (
       id bigint generated by default as identity,
        quantity integer not null,
        product_id bigint,
        primary key (id)
    )
Hibernate: 
    
    alter table orders 
       add constraint FKkp5k52qtiygd8jkag4hayd0qg 
       foreign key (product_id) 
       references products

HTTP/1.1 201 
Content-Length: 0
Date: Mon, 03 Mar 2025 09:06:09 GMT
Keep-Alive: timeout=60
Connection: keep-alive
Request method:	POST
Request URI:	http://localhost:7334/orders
Proxy:			<none>
Request params:	<none>
Query params:	<none>
Form params:	<none>
Path params:	<none>
Headers:		Accept=*/*
				Content-Type=application/json
Cookies:		<none>
Multiparts:		<none>
Body:
{
    "productId": 1,
    "quantity": 2
}
Hibernate: 
    select
        product0_.id as id1_1_0_,
        product0_.discount_policy as discount2_1_0_,
        product0_.name as name3_1_0_,
        product0_.price as price4_1_0_ 
    from
        products product0_ 
    where
        product0_.id=?
Hibernate: 
    insert 
    into
        orders
        (id, product_id, quantity) 
    values
        (default, ?, ?)
```