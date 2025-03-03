# 색션 5. 상품 주문 API 개발

## POJO 상품 주문 기능 구현하기

### 상품 주문 관련 Class
- Order → 주문 객체
- OrderService → 주문 구현체
- CreateOrderRequest → 주문 요청 Class 
- OrderPort
- OrderAdapter 
- OrderRepository → 주문 DB
```java
import com.example.productorderservice.product.DiscountPolicy;
import com.example.productorderservice.product.Product;
import com.example.productorderservice.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;


public class OrderServiceTest {

  private OrderService orderService;

  private OrderPort orderPort;

  private OrderRepository orderRepository;


  @BeforeEach
  void setUP(){
    orderRepository = new OrderRepository();
    orderPort = new OrderPort() {

      @Override
      public Product getProductById(Long productId) {
        return new Product("상품명", 1000, DiscountPolicy.NONE);
      }

      @Override
      public void save(Order order) {
        orderRepository.save(order);
      }
    };
    orderService = new OrderService(orderPort);
  }
  @Test
  void 상품주문(){
    final Long productId = 1L;
    final int quantity = 2;
    final CreateOrderRequest request = new CreateOrderRequest(productId, quantity);

    orderService.createOrder(request);
  }

  private record CreateOrderRequest (Long productId, int quantity){
    private  CreateOrderRequest{
      Assert.notNull(productId, "상품 ID는 필수입니다.");
      Assert.isTrue(quantity > 0, "수량은 0보다 커야 합니다.");
    }


  }

  private class OrderService {

    private final OrderPort orderPort;

    private OrderService(OrderPort orderPort) {
      this.orderPort = orderPort;
    }

    public void createOrder(CreateOrderRequest request) {
      final Product product = orderPort.getProductById(request.productId);
      final Order order = new Order(product, request.quantity());

      orderPort.save(order);
    }
  }

  private class OrderAdapter implements OrderPort {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private OrderAdapter(final ProductRepository productRepository, final OrderRepository orderRepository){
      this.productRepository = productRepository;
      this.orderRepository = orderRepository;
    }

    public Product getProductById(final Long productId){
      return  productRepository.findById(productId)
              .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
    }

    public void save(final Order order) {
      orderRepository.save(order);
    }
  }

  private class Order {

    private Long id;
    private final Product product;
    private final int quantity;
    public Order(Product product, int quantity) {
      Assert.notNull(product, "상품은 필수입니다.");
      Assert.isTrue(quantity > 0, "수량은 0보다 커야 합니다.");
      this.product = product;
      this.quantity = quantity;
    }

    public void assignID(final Long id){
      this.id = id;
    }
    public Long getId() {
      return id;
    }
  }

  private class OrderRepository {

    private final Map<Long, Order> persistence = new HashMap<>();
    private Long sequence = 0L;
    public void save(final Order order) {
      order.assignID(++sequence);
      persistence.put(order.getId(), order);
    }
  }

  private interface OrderPort {

    Product getProductById(final Long productId);

    void save(final Order order);
  }
}
```

## 스프링부트 테스트로 전환하기
### Order
```java
import com.example.productorderservice.product.Product;
import org.springframework.util.Assert;

class Order {

  private Long id;
  private final Product product;
  private final int quantity;

  public Order(Product product, int quantity) {
    Assert.notNull(product, "상품은 필수입니다.");
    Assert.isTrue(quantity > 0, "수량은 0보다 커야 합니다.");
    this.product = product;
    this.quantity = quantity;
  }

  public void assignID(final Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }
}
```

### OrderService
```java
import com.example.productorderservice.product.Product;
import org.springframework.stereotype.Component;

@Component
class OrderService {

  private final OrderPort orderPort;

  OrderService(OrderPort orderPort) {
    this.orderPort = orderPort;
  }

  public void createOrder(CreateOrderRequest request) {
    final Product product = orderPort.getProductById(request.productId());
    final Order order = new Order(product, request.quantity());

    orderPort.save(order);
  }
}
```

### CreateOrderRequest 주문 요청 만들기
```java
import org.springframework.util.Assert;

record CreateOrderRequest(Long productId, int quantity) {
  CreateOrderRequest {
    Assert.notNull(productId, "상품 ID는 필수입니다.");
    Assert.isTrue(quantity > 0, "수량은 0보다 커야 합니다.");
  }

}
```

### OrderPort
```java
import com.example.productorderservice.product.Product;

interface OrderPort {

  Product getProductById(final Long productId);

  void save(final Order order);
}

```

### OrderAdapter
```java
import com.example.productorderservice.product.Product;
import com.example.productorderservice.product.ProductRepository;
import org.springframework.stereotype.Component;

@Component
class OrderAdapter implements OrderPort {

  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;

  private OrderAdapter(final ProductRepository productRepository, final OrderRepository orderRepository) {
    this.productRepository = productRepository;
    this.orderRepository = orderRepository;
  }

  public Product getProductById(final Long productId) {
    return productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
  }

  public void save(final Order order) {
    orderRepository.save(order);
  }
}
```

### OrderRepository 
```java
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class OrderRepository {

  private final Map<Long, Order> persistence = new HashMap<>();
  private Long sequence = 0L;

  public void save(final Order order) {
    order.assignID(++sequence);
    persistence.put(order.getId(), order);
  }
}
```
- JPA가 아닌 일단 임시로 Map에다가 저장한다. 

### OrderServiceTest 수정
```java
import com.example.productorderservice.product.ProductService;
import com.example.productorderservice.product.ProductSteps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;

    @Test
    void 상품주문(){

        productService.addProduct(ProductSteps.상품등록요청_생성());
        final CreateOrderRequest request = 상품주문요청_생성();

        orderService.createOrder(request);
    }

    private static CreateOrderRequest 상품주문요청_생성(){
        final Long productId = 1L;
        final int quantity = 2;
        return new CreateOrderRequest(productId, quantity);

    }

}
```
- 그냥 주문하면 오류가 난다 → 기존 상품이 없어서
- 상품 등록 → 상품 주문

#### 결과 Log
```text
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes UP-TO-DATE
> Task :compileTestJava
> Task :processTestResources NO-SOURCE
> Task :testClasses
> Task :test
BUILD SUCCESSFUL in 2s
4 actionable tasks: 2 executed, 2 up-to-date
오후 3:33:53: Execution finished ':test --tests "com.example.productorderservice.product.ProductServiceTest.상품수정"'.
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

### OrderApiTest에  상품주문 API 연동 테스트
```java
import com.example.productorderservice.ApiTest;
import com.example.productorderservice.product.ProductSteps;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderApiTest extends ApiTest {

    @Autowired
    private OrderService orderService;

    @Test
    void 상품주문(){
        ProductSteps.상품등록요청(ProductSteps.상품등록요청_생성());
        final CreateOrderRequest request = 상품주문요청_생성();

        final ExtractableResponse<Response> response = RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post("/orders")
            .then()
            .log().all().extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        orderService.createOrder(request);
    }

    private static CreateOrderRequest 상품주문요청_생성(){
        final Long productId = 1L;
        final int quantity = 2;
        return new CreateOrderRequest(productId, quantity);

    }

}
```
#### Log
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
        product0_.id as id1_0_0_,
        product0_.discount_policy as discount2_0_0_,
        product0_.name as name3_0_0_,
        product0_.price as price4_0_0_ 
    from
        products product0_ 
    where
        product0_.id=?
Hibernate: 
    select
        product0_.id as id1_0_0_,
        product0_.discount_policy as discount2_0_0_,
        product0_.name as name3_0_0_,
        product0_.price as price4_0_0_ 
    from
        products product0_ 
    where
        product0_.id=?
Hibernate: 
    update
        products 
    set
        discount_policy=?,
        name=?,
        price=? 
    where
        id=?
Hibernate: 
    select
        product0_.id as id1_0_0_,
        product0_.discount_policy as discount2_0_0_,
        product0_.name as name3_0_0_,
        product0_.price as price4_0_0_ 
    from
        products product0_ 
    where
        product0_.id=?
```

## API로테스트로 전환하기

### ProductService Request Mapping URL에 추가
```java
    @PatchMapping("{productId}")
    @Transactional
    public ResponseEntity<Void> updateProduct(
    @PathVariable final Long productId,
    @RequestBody final UpdateProductRequest request) {
    final Product product = productPort.getProduct(productId);
        product.update(request.name(), request.price(), request.discountPolicy());

        productPort.save(product);
        return ResponseEntity.ok().build();
      }
```

### ProductApiTest 상품 수정 테스트 하기
```java
    @Test
    void 상품수정(){
        ProductSteps.상품등록요청(ProductSteps.상품등록요청_생성());
        final long productId = 1l;

        final ExtractableResponse<Response> response = 상품수정요청(1L);


        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(productRepository.findById(1L).get().getName()).isEqualTo("상품 수정");
    }

    private static ExtractableResponse<Response> 상품수정요청(final long productId){
        return RestAssured.given().log().all()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(ProductSteps.상품수정요청_생성())
        .when()
        .patch("/products/{priductId}", productId)
        .then()
        .log().all().extract();
    }

```
- 상품 먼저 등록후 등록한 데이터 기점으로 테스트 한다.

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