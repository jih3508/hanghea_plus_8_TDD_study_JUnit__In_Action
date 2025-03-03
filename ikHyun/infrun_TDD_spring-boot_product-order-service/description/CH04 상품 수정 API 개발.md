# 색션 4. 상품 수정 API 개발

## POJO 상품 수정 기능 구현하기

### ProductSteps 상품조회요청 method 추가
```java
public static ExtractableResponse<Response> 상품조회요청(final Long productId){
return RestAssured.given().log().all()
        .when()
        .get("/products/{productId}", productId)
        .then().log().all()
        .extract();
}
```

### UpdateProductRequest 수정 요청값 Class
```java
import org.springframework.util.Assert;

public record UpdateProductRequest(
        String name,
        int price,
        DiscountPolicy discountPolicy
) {
    public UpdateProductRequest{
        Assert.hasText(name, "상품명은 필수입니다.");
        Assert.isTrue(price > 0, "상품 가격은 0보다 커야 합니다.");
        Assert.notNull(discountPolicy, "할인 정책은 필수입니다.");
    }
}
```

### Product에 update method 추가
```java
public void update(String name, int price, DiscountPolicy discountPolicy) {
    Assert.hasText(name, "상품며은 필수인니다.");
    Assert.isTrue(price > 0, "상품 가격은 0보다 커야 합니다.");
    Assert.notNull(discountPolicy, "할인 정책은 필수 입니다.");
    this.name = name;
    this.price = price;
    this.discountPolicy = discountPolicy;
}

```

### 상품 조회 기능 구현
#### ProductService
```java
public GetProductResponse getProduct(final Long productId){
        final Product product = productPort.getProduct(productId);

        return new GetProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDiscountPolicy()
        );
    }
```

#### ProductTest 추가
```java
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Test
    void update() {
        final Product product = new Product("상품명", 1000, DiscountPolicy.NONE);

        product.update("상품 수정", 2000, DiscountPolicy.NONE);

        assertThat(product.getName()).isEqualTo("상품 수정");
        assertThat(product.getPrice()).isEqualTo(2000);

    }
}
```

#### ProductAdapter
```java
    @Override
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
    }
```

#### ProductServiceTest 상품 수정 테스트
```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    private ProductService productService;

    private ProductPort productPort;

    @BeforeEach
    void setUp(){
        productPort = Mockito.mock(ProductPort.class);
        productService = new ProductService(productPort);
    }

    @Test
    void 상품수정(){
        final Long productId = 1L;
        final UpdateProductRequest request = new UpdateProductRequest("상품 수정", 2000, DiscountPolicy.NONE);
        final Product product = new Product("상품명", 1000, DiscountPolicy.NONE);
        Mockito.when(productPort.getProduct(productId)).thenReturn(product);

        productService.updateProduct(productId, request);

        assertThat(product.getName()).isEqualTo("상품 수정");
        assertThat(product.getPrice()).isEqualTo(2000);

    }


}
```
- `@ExtendWith(MockitoExtension.class)`
  - MockitoExtension.class를 적용
  - @Mock, @InjectMocks 같은 어노테이션을 사용할 수 있음
- `Mockito.mock(ProductPort.class)`
  - ProductPort 인터페이스의 Mock 객체를 생
  - 실제 구현체 대신 **가짜 객체(Mock Object)**를 사용하여 테스트를 수행 함
- `Mockito.when(productPort.getProduct(productId)).thenReturn(product);`
  - productPort가 실제로 데이터베이스를 조회하는 것이 아니라, Mockito가 제공한 가짜 응답을 반환

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

## 스프링부트 테스트로 전환하기

### ProductServiceTest Mock에서 SpringBootTest로 변환

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProductServiceTest {

  @Autowired
  private ProductService productService;

  @Test
  void 상품수정(){
    productService.addProduct(ProductSteps.상품등록요청_생성());
    final Long productId = 1L;
    final UpdateProductRequest request = new UpdateProductRequest("상품 수정", 2000, DiscountPolicy.NONE);

    productService.updateProduct(productId, request);

    final ResponseEntity<GetProductResponse> response = productService.getProduct(productId);
    final GetProductResponse productResponse = response.getBody();

    assertThat(productResponse.name()).isEqualTo("상품 수정");
    assertThat(productResponse.price()).isEqualTo(2000);


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
##### 결과 Log
```text
Hibernate: 
    
    drop table if exists products CASCADE 
Hibernate: 
    
    create table products (
       id bigint generated by default as identity,
        discount_policy integer,
        name varchar(255),
        price integer not null,
        primary key (id)
    )
Hibernate: 
    
SET
    REFERENTIAL_INTEGRITY FALSE
Hibernate: 
    TRUNCATE TABLE products
Hibernate: 
    ALTER TABLE products ALTER COLUMN ID RESTART WITH 1
Hibernate: 
SET
    REFERENTIAL_INTEGRITY TRUE
Request method:	POST
Request URI:	http://localhost:14920/products
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
    "name": "상품명",
    "price": 1000,
    "discountPolicy": "NONE"
}

Hibernate: 
    insert 
    into
        products
        (id, discount_policy, name, price) 
    values
        (default, ?, ?, ?)
HTTP/1.1 201 
Content-Length: 0
Date: Mon, 03 Mar 2025 07:12:39 GMT
Keep-Alive: timeout=60
Connection: keep-alive
Request method:	PATCH
Request URI:	http://localhost:14920/products/1
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
    "name": "상품 수정",
    "price": 2000,
    "discountPolicy": "NONE"
}
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
HTTP/1.1 200 
Content-Length: 0
Date: Mon, 03 Mar 2025 07:12:39 GMT
Keep-Alive: timeout=60
Connection: keep-alive
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
    
    drop table if exists products CASCADE 
```