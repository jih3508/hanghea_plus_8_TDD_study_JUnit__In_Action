# 2장

## 2.1 핵심 애노테이션
- 테스트 메서드: @Test, @RepeatedTest, @ParameterizedTest @TestFactory, @TestTemplate
- 생애 주기 메서드: @BeforeAll, @AfterAll, @BeforeEach, @Each

### 2.2.1 @DisplayName
### 2.2.2 @Disabled
- @Disabled 붙인 테스트 클래스나 테스트 메서드는 비활성화되므로 테스트가 실행되지 않는다.

## 2.2 중첩 테스트

## 2.3 테그를 사용한 테스트

## 2.4 단언무
- assertAll: 오버로딩이 적용되어 있다. 안에 있는 executable 객체 중 어느 것도 예외를 던지지 않는다고 단언한다.
- asserArrayEquals: 오버로딩이 적용되어 있다. 예상 배열과 실제 배열이 동등하다고 단언한다.
- assertEquals: 예상 값과 실제 값이 동등하다고 단언한다.
- assertX(..., String message): 실패 했을 경우 message를 테스트 프레임워크에 전달하는 단언문이다.
- assertX(..., Supplier<String> messageSupplier): 실패했을 경우 messageSupplier를 테스트 프레임워크에 전달하는 단언무이다. 실패 메세지는 messageSupplier에서 지연(lazily) 전달한다.

## 2.6 JUnit5의 의존성 주입
### 2.6.1 TestInfoParameterResolver
### 2.6.2 TestReporterParameterResolver
### 2.6.3 RepetitionInfoParameterResolver

## 2.7 반복 테스트
- @RepeatedTest