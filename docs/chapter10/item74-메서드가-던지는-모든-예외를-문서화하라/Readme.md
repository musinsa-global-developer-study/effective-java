# 메서드가 던지는 모든 예외를 문서화하라

## 검사 예외는 `@throws` 어노테이션을 사용해 문서화하라

검사 예외는 항상 따로따로 선언하고, 각 예외가 발생하는 상황을 자바독의 `@throws` 어노테이션을 사용하여 문서화해야한다. <br/>
공통 상위 클래스 하나로 뭉뚱그려 선언하는 일은 삼가야한다. 극단적인 예로 메서드가 Exception 이나 Throwable 을 던진다고 선언하면 안된다.<br/>
Exception 을 던지는 것은 오직 JVM만 호츨하는 main 메서드만 가능하다.

```java
/**
 * @throws CheckedException
 *          유효하지 않는 상황이면 CheckedException 을 반환한다.
 */
public void process() throws CheckedException {
    if(this.isInvalid()) {
        throw new CheckedException();
    }
    ...
}
```

## 비검사 예외도 `@throws` 어노테이션을 사용해 문서화하라

메서드가 던질 수 있는 예외를 각각 `@throws` 어노테이션으로 문서화하되, 비검사 예외는 메서드 선언의 throws 목록에 넣지 말자.<br/>
검사냐 비검사냐에 따라 API 사용자가 해야 할 일이 달라지므로 이 둘을 확실히 구분해주는 것이 좋다. <br/>
JavaDoc 유틸리티는 메서드 선언의 `throws` 절과 메서드 주석의 `@throws` 어노테이션 둘다 명시된 예외와 메서드 주석의 `@throws` 어노테이션에만 명시된 예외를 시각적으로 구분해주어,
사용자가 바로 어떤것이 비검사 예외인지 알 수 있다.

```java
/**
 * @throws UncheckedException
 *          유효하지 않는 상황이면 UncheckedException 을 반환한다.
 */
public void process() {
    if(this.isInvalid()) {
        throw new UncheckedException();
    }
    ...
}
```

## 같은 예외가 같은 이유로 너무 많이 발생하면 클래스 주석에 문서화하는 방법을 사용하라

한 클래스에 정의된 많은 메서드가 같은 이유로 같은 예외를 던진다면 그 예외를 각각의 메서드가 아닌 클래스 주석에 추가하는 방법도 있다.

```java
/**
 * @throws UncheckedException
 *          유효하지 않는 상황이면 UncheckedException 을 반환한다.
 */
public class Calculator {
    
    ...

    public void process1() {
        if (this.isInvalid()) {
            throw new UncheckedException();
        }
    ...
    }

    public void process2() {
        if (this.isInvalid()) {
            throw new UncheckedException();
        }
    ...
    }
}
```

## 이모저모

이펙티브 자바에서는 자바독을 이용해 문서화를 이야기해주고 있지만, 문서화를 자바독이 아닌 테스트 코드를 사용하는 것은 어떨까 생각이 들었습니다. <br/>
테스트 코드의 역할이 심리적 안정감, 리팩터링 용이함, 회귀테스트 등 다양한 역할이 있지만, 요구사항 혹은 정책들에 대한 명세서 역할도 해주기 때문에 테스트 코드 작성 하나로 문서화 역할까지 제공해주며,
프로덕션 코드에 주석을 추가하는 추가 리소스 대신 테스트코드로 커버하면 더 좋지 않을까 생각이 들었습니다.
