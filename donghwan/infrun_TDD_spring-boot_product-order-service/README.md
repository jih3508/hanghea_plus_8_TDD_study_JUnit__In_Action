
## 흐름

1. POJO로 구현해야할 기능을 단위별 Test 코드로 작성한다.
2. 의존성을 먼저 만들지 않는다. 이 기능이 만들어졌다고 생각하고 테스트 코드를 짜는 느낌?
   ![img.png](img.png)
```java
    @Test
    void 상품주문() {
    
        final CreateOrderRequest request = new CreateOrderRequest(productId,quantity);
        
        orderService.createOrder(request);
    }
```
1. CreateOrderRequest, orderService를 만들지 않은 상태에서 위에 코드를 작성한다.
2. 테스트에 필요한 모든 클래스를 테스트 클래스에 구현한다.


```java
public class OrderServiceTest {

    private OrderService orderService;
    private OrderPort orderPort;
    private OrderRepository orderRepository;
    
    @BeforeEach
    void setUp() {
      orderRepository = new OrderRepository();
      orderPort = new OrderPort() {
        
      @Override
      public Product getProductById(Long productId) {
        return new Product("상품명",1000, DiscountPolicy.NONE);
      }
            
      @Override
      public void save(Order order) {
        
      }
    };
      orderService = new OrderService(orderPort);
    }
    
    @Test
    void 상품주문() {
    
        final Long productId = 1L;
        final int quantity = 2;
        
        final CreateOrderRequest request = new CreateOrderRequest(productId,quantity);
        
        orderService.createOrder(request);
    }
    
    private class OrderService {
    
        private final OrderPort orderPort;
        
        public OrderService(OrderPort orderPort) {
          this.orderPort = orderPort;
        }
    
        public void createOrder(CreateOrderRequest request) {
        
            final Product product = orderPort.getProductById(request.productId());
            
            final Order order = new Order(product, request.quantity());
            
            orderPort.save(order);
        }
    }
    
    private class Order {
        private Long id;
        private final Product product;
        private final int quantity;
        
        public Order(Product product, int quantity) {
        Assert.notNull(product, "상품 ID는 필수입니다.");
        Assert.isTrue(quantity > 0,"수량은 0보다 커야합니다.");
        this.product = product;
        this.quantity = quantity;
    }
    
        public void assignId(Long id) {
          this.id = id;
        }
    
        public Long getId() {
          return id;
        }
    }
    private class OrderAdapter implements OrderPort{
    
        private final ProductRepository productRepository;
        
        private final OrderRepository orderRepository;
        
        
        public OrderAdapter(final ProductRepository productRepository, final OrderRepository orderRepository) {
            this.productRepository = productRepository;
            this.orderRepository = orderRepository;
        }
				
        @Override
        public Product getProductById(final Long productId) {
          return productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
        }
				
        @Override
        public void save(Order order) {
          orderRepository.save(order);
        }
    }
    private record CreateOrderRequest(Long productId, int quantity) {
        private CreateOrderRequest {
            Assert.notNull(productId, "상품 ID는 필수입니다.");
            Assert.isTrue(quantity > 0,"수량은 0보다 커야합니다.");
        }
    }
    
    
    private class OrderRepository {
    
        private Map<Long, Order> persistence = new HashMap<>();
        private Long sequence = 0L;
    
        public void save(Order order) {
            order.assignId(++sequence);
            persistence.put(order.getId(), order);
        }
    }
    
    private interface OrderPort {
        Product getProductById(final Long productId);
        void save(Order order);
    }
}


``` 

**테스트가 정상적으로 수행되면 테스트 코드(@Test 어노테이션이 붙은 코드)를 제외한 내부 클래스를 메인 디렉토리로 분리한다.**

클래스 분리를 마쳤다면 테스트 코드가 정상적으로 동작하는지 확인한다.
정상동작한다면 스프링부트로 전환한다.

## 스프링부트로 전환하기
1. 분리된 클래스 의존성 등록하기
2. 테스트 코드에서 의존성 직접주입 코드(setUp) 제거
```java
@BeforeEach
void setUp() {
    orderRepository = new OrderRepository();
    orderPort = new OrderPort() {
    
    @Override
    public Product getProductById(Long productId) {
      return new Product("상품명",1000, DiscountPolicy.NONE);
    }
    
    @Override
    public void save(Order order) {
    }
    };
		
    orderService = new OrderService(orderPort);
}
```
3. @SpringBootTest 추가 및 의존성 주입
```java
@SpringBootTest
public class OrderServiceTest {

	private OrderService orderService;
	private OrderPort orderPort;
	private OrderRepository orderRepository;
```

```java
@Component
class OrderAdapter implements OrderPort {...
```
```java
@Component
interface OrderPort {...
```
```java
@Repository
class OrderRepository {...
```
```java
@Component
class OrderService {...
```
### 스프링부트로 전환이 완료된 테스트코드
```java
@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;
    
    @Test
    void 상품주문() {
        productService.addProduct(ProductSteps.상품등록요청_생성());
        final CreateOrderRequest request = 상품주문요청_생성();
        
        orderService.createOrder(request);
    }
    
    private static CreateOrderRequest 상품주문요청_생성() {
        final Long productId = 1L;
        final int quantity = 2;
        
        final CreateOrderRequest request = new CreateOrderRequest(productId,quantity);
        return request;
    }


}
```

## API 테스트로 전환하기
OrderServiceTest -> OrderApiTest로 변경
실제 애플리케이션 서버에 API요청을 보내 상품주문을 테스트한다.

**변경 후**
```java
public class OrderApiTest extends ApiTest {

	@Test
	void 상품주문() {
		ProductSteps.상품등록요청(ProductSteps.상품등록요청_생성());
		final CreateOrderRequest request = 상품주문요청_생성();

		ExtractableResponse<Response> response = RestAssured.given().log().all()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(request)
			.when()
			.post("/orders")
			.then()
			.log().all().extract();

		assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
	}

	private static CreateOrderRequest 상품주문요청_생성() {
		final Long productId = 1L;
		final int quantity = 2;

		final CreateOrderRequest request = new CreateOrderRequest(productId, quantity);
		return request;
	}
}
```
- 부모클래스인 ApiTest 에서 @SpringBootTest를 사용
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
public class ApiTest {

    @Autowired
    private DatabaseCleanup databaseCleanup;
    
    @LocalServerPort 
    int port;
    
    @BeforeEach
    public void setUp() {
        if(RestAssured.port == RestAssured.UNDEFINED_PORT) {
            RestAssured.port = port;
            databaseCleanup.afterPropertiesSet();;
        }
        databaseCleanup.execute();
    }
}
```
컨트롤러 역할을 하는 OrderService  
**변경전**
```java
@Component
private class OrderService {

  private final OrderPort orderPort;

    public OrderService(OrderPort orderPort) {
      this.orderPort = orderPort;
    }

    public void createOrder(CreateOrderRequest request) {
    
        final Product product = orderPort.getProductById(request.productId());
        
        final Order order = new Order(product, request.quantity());
        
        orderPort.save(order);
    }
}
```

**변경후**
```java
@RestController
@RequestMapping("/orders")
class OrderService {

    private final OrderPort orderPort;
    
    public OrderService(OrderPort orderPort) {
      this.orderPort = orderPort;
    }
    
    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody CreateOrderRequest request) {
    
        final Product product = orderPort.getProductById(request.productId());
        
        final Order order = new Order(product, request.quantity());
        
        orderPort.save(order);
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
```

## JPA 적용하기
Order 엔터티 클래스로 변환
```java
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
class Order {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Setter
	@OneToOne
	private Product product;
	private  int quantity;
  ...
```

JPA Repository 구현
```java
@Repository
interface OrderRepositoryJpa extends JpaRepository<Order,Long> {
}
```

OrderApiTest 실제 DB에 값 저장
```java
public class OrderApiTest extends ApiTest {

@Test
void 상품주문() {
    ProductSteps.상품등록요청(ProductSteps.상품등록요청_생성());
    final var  request = OrderSteps.상품주문요청_생성();
    
    final var response = OrderSteps.상품주문요청(request);
    
    assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }
}
```

리팩토링
OrderSteps.java
```java
public class OrderSteps {
    public static ExtractableResponse<Response> 상품주문요청(CreateOrderRequest request) {
        return RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post("/orders")
            .then()
            .log().all().extract();
    }

    public static CreateOrderRequest 상품주문요청_생성() {
        final Long productId = 1L;
        final int quantity = 2;
    
        final CreateOrderRequest request = new CreateOrderRequest(productId, quantity);
        return request;
    }
}
```
## 왜 POJO -> Stringboot -> API 로 진행할까
> POJO로 개발한 후 스프링으로 전환하고 마지막에 JPA로 전환하는 이유는 **주로 설계 및 개발 과정에서의 효율성과 유연성** 때문입니다.
>
> 처음부터 JPA로 개발하는 것도 가능하지만, 이렇게 접근할 경우 다음과 같은 단점이 있습니다.
>
> 데이터 중심의 설계: JPA를 처음부터 사용하게 되면, 데이터 중심의 설계가 나오기 쉽습니다. 이로 인해 객체지향적인 설계 원칙이 무시되거나 희생될 수 있습니다.
>
>반면, POJO를 먼저 사용하면 객체지향적인 설계 원칙에 충실한 코드를 작성할 수 있으며, 이후에 JPA로 전환하면서 객체와 데이터베이스 사이의 연동을 수월하게 할 수 있습니다.<br/><br/>
개발 시간: JPA를 처음부터 사용하면, 초기 개발 시간이 상대적으로 더 많이 소요됩니다. 반면에 POJO로 먼저 개발하면, 기능 구현에 집중하여 빠르게 개발할 수 있으며, 이후 스프링 및 JPA로 전환하면서 필요한 부분만 점진적으로 수정해 나갈 수 있습니다.<br/><br/>
따라서, POJO로 개발한 후 스프링과 JPA로 전환하는 접근 방식은 객체지향적인 설계 원칙을 준수하면서도 개발 시간을 줄이고 유연한 코드 작성이 가능한 방법입니다.

---
## MockMvc VS RestAssured
구분| MocvMvc| RestAssured 
--|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------
사용목적| - 웹 애플리케이션을 서버에 배포하지 않고 스프링 MVC 동작을 재현할수 있는 라이브러리이며 대부분 컨트롤러 단위테스트에서 사용한다.<br/><br/> - 실제 서버 환경과 동일한 @SpringBootTest를 사용할 필요가 없어 @WebMvcTest를 통해 Presentation Layer Bean들만 불러온다. <br/><br/>- Mock 객체를 설정하여 순수한 Controller 로직을 테스트할 수 있다. | - REST 웹 서비스를 검증하기 위한 라이브러리<br/><br/>-@SpringBootTest로 실제 요청을 보내 전체적인 로직을 테스트한다. 
의존성| Spring Framework test 클래스 중 하나 Spring test 의존성만 있으면 별도의 의존성 추가필요없음.| 별도의 의존성을 직접 추가해야함
속도| presentation layer의 bean들만 로드하기 때문에 시간이 상대적으로 빠르다.|Spring Bean을 전부 로드하기 때문에 시간이 오래걸린다.

가독성에서는 BDD스타일로 작성한 RestAssured가 명확하고 쉽게 읽을 수 있는 것같다.

**RestAssured**
```java
@Test
public void getMember() {
    given().
            accept(MediaType.APPLICATION_JSON_VALUE).
    when().
            get("/members/1").
    then().
            log().all().
            statusCode(HttpStatus.OK).
            assertThat().body("id", equalTo(1)); 
}
```
**MockMvc**
```java
@Test
public void getMember() throws Exception {
  mockMvc.perform(get("/members/1")
    .accept(MediaType.APPLICATION_JSON))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.id", Matchers.is(1)));
}
```
https://tecoble.techcourse.co.kr/post/2020-08-19-rest-assured-vs-mock-mvc/

---

## TDD란 무엇인가
- 개발전 테스트 코드를 먼저 작성하여 개발하는 방식
- write the test -> run test ->(fail) -> write only enough code
- 실패한 테스트 케이스를 먼저 만들고 동작하도록 실제 코드를 작성하는 방법
- 모든 기능이 구현되면 리팩토링

### 장점
- 작성하고자 하는 모든 요구사항에 대한 분석과 이해를 기반으로함.
- 사용자 입장에서 코드를 작성할 수 있다.
- 구현보다는 인터페이스에 집중해서 코드를 작성할 수 있다.
- 코드의 퀄리티 향상
- 시스템 전반적인 설계 향상
- 내가 집중하고 있는 기능외에 사이드 이펙트가 발생할 수 있는 부분을 알려준다.
- 새로운 기능을 추가할때 좀 더 자신감있게 작성할 수 있다.

### 단점
- 시간이 오래걸린다.

--- 
### 의문
- 실무에서도 이렇게 개발을 할 수 있을까? 