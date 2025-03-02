# 세션 2 상품 등록 API 개발

## POJO 상품 등록 기능 구현하기
### Class
- Product : 상품 객체
- DiscountPolicy(ENUM) -> NONE
```java
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import javax.persistence.*;

class Product {

    private String name;

    private int price;

    private DiscountPolicy discountPolicy;

    Product(final String name, final int price, final DiscountPolicy discountPolicy) {
        Assert.hasText(name, "상품명은 필수입니다.");
        Assert.isTrue(price > 0, "상품 가격은 0보다 커야 합니다.");
        Assert.notNull(discountPolicy, "할인 정책은 필수 입니다.");
        this.name = name;
        this.price = price;
        this.discountPolicy = discountPolicy;


    }

}

```
- ProductService
- ProductPort
- ProductRepository
- AddProductRequest: 상품 등록 Class 구조
- `@BeforeEach`
    - 각 테스트 실행 전에 반드시 실행되는 메서드를 정의

```java
import org.springframework.util.Assert;

record AddProductRequest(String name, int price, DiscountPolicy discountPolicy) {
    public AddProductRequest {
        Assert.hasText(name, "상품명은 필수입니다.");
        Assert.isTrue(price > 0, "상품 가격은 0보다 커야 합니다.");
        Assert.notNull(discountPolicy, "할인 정책은 필수 입니다.");
    }
}
```

## API 테스트로 전환하기
### ProductService에서 `/products` RestAPI 추가
```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
class ProductService {
    private final ProductPort productPort;

    ProductService(final ProductPort productPort) {
        this.productPort = productPort;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Void> addProduct(@RequestBody final AddProductRequest request) {
        final Product product = new Product(request.name(), request.price(), request.discountPolicy());

        productPort.save(product);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

```
### REST Assured Dependency 추가
```text
testImplementation 'io.rest-assured:rest-assured:4.4.0'
```
### APITest 추가
```java
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void serUp(){
        RestAssured.port = port;
    }
}

```
- 테스트할때 SpringBoot 환경에서 랜덤포트로 할당 하여 실행 시킨다.
- `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
  - Spring Boot 테스트 환경을 설정하는 애너테이션
  - `webEnvironment = RANDOM_PORT` 설정은 테스트용으로 임의의 포트를 할당하여 애플리케이션을 실행하도록 지정

- `@LocalServerPort`
  - 할당된 포트 번호를 자동으로 주입받아 port 변수에 저장

#### 상품등록 요청 생성
```java


private static AddProductRequest 상품등록요청_생성(){
        final String name = "상품명";
        final int price = 1000;
        final DiscountPolicy discountPolicy = DiscountPolicy.NONE;
        return new AddProductRequest(name, price, discountPolicy);
    }
```

### 상품등록 요청
```java

private static  ExtractableResponse<Response> 상품등록요청(final AddProductRequest request){
    return RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post("/products")
            .then()
            .log().all().extract();
}
```
- `AddProductRequest` 보낼데이터를 Json으로 변경후 `/products` Post 전송 테스트

### 전체 테스트 코드
```java
import com.example.productorderservice.ApiTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductApiTest extends ApiTest {


    @Autowired
    private ProductService productService;

    @Test
    void 상품등록(){
        final  AddProductRequest request = 상품등록요청_생성();

        // API 요청
        final ExtractableResponse<Response> response = 상품등록요청(request);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());

    }

    private static  ExtractableResponse<Response> 상품등록요청(final AddProductRequest request){
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/products")
                .then()
                .log().all().extract();
    }

    private static AddProductRequest 상품등록요청_생성(){
        final String name = "상품명";
        final int price = 1000;
        final DiscountPolicy discountPolicy = DiscountPolicy.NONE;
        return new AddProductRequest(name, price, discountPolicy);
    }

}
```

## JPA 적용하기
### Product JPA Entity로 변경
```java
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import javax.persistence.*;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    private int price;

    private DiscountPolicy discountPolicy;


}
```
- `@Table(name = "products")`:  `products` 테이블과 매팽됨
- `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`: PK 값으로 지정과 기본 키 값을 자동으로 생성(Auto Increment) 하도록 설정
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 
  - 기본 생성자의 접근 제어자를 protected로 지정
  - 동일 패키지 내 클래스나 서브클래스에서만 이 생성자를 사용할 수 있음
  - 외부에서는 객체 생성을 막을 수 있어, 직접 인스턴스화하지 않도록 제한하는 용도로 사용
  - 외부에서 함부로 new Product() 객체 생성 못하도록 막음
### ProductRepository JPA 형식으로 변경
```java
import org.springframework.data.jpa.repository.JpaRepository;

interface ProductRepository extends JpaRepository<Product, Long> {
}
```

### DatabaseCleanup class 추가
```java
import com.google.common.base.CaseFormat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DatabaseCleanup implements InitializingBean {

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> tableNames;

    @Override
    public void afterPropertiesSet() {
        final Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
        // TABLE 목록 리스트 추출
        tableNames = entities.stream()
                .filter(e -> isEntity(e) && hasTableAnnotation(e))
                .map(e -> {
                    String tableName = e.getJavaType().getAnnotation(Table.class).name();
                    return tableName.isBlank() ? CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, e.getName()) : tableName;
                })
                .collect(Collectors.toList());

        final List<String> entityNames = entities.stream()
                .filter(e -> isEntity(e) && !hasTableAnnotation(e))
                .map(e -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, e.getName()))
                .toList();

        tableNames.addAll(entityNames);
    }

    private boolean isEntity(final EntityType<?> e) {
        return null != e.getJavaType().getAnnotation(Entity.class);
    }

    private boolean hasTableAnnotation(final EntityType<?> e) {
        return null != e.getJavaType().getAnnotation(Table.class);
    }

    @Transactional
    public void execute() {
        entityManager.flush();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        /*
         * 각테이블 마다 아래 SQL 실행
         */
        for (final String tableName : tableNames) {
            entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate(); // 테이블 데이터 비우시
            entityManager.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1").executeUpdate(); // pk 값 1로 시작하기
        }

        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }
}
```
- `afterPropertiesSet()`: DB 테이블 추출
- `execute()`: DB 초기화 작업

### ApiTest 수정
- 테스트할때 추가, 조회할때 꼬일수 있어서 초기 작업할때 초기화 작업이 필요
```java
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiTest {

    @Autowired
    private DatabaseCleanup databaseCleanup;

    @LocalServerPort
    private int port;

    @BeforeEach
    void serUp(){
        if(RestAssured.port == RestAssured.UNDEFINED_PORT){
            RestAssured.port = port;
            databaseCleanup.afterPropertiesSet();
        }
        databaseCleanup.execute();

    }
}
```
#### Log
```text
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
```