# 추상화 수준에 맞는 예외를 던지라

## 예외 번역을 활용하라

메서드가 저수준 예외를 처리하고 않고 바깥으로 전파해버릴 때 종종 당황스러운 케이스가 생긴다. 이렇게 되면 대표적인 단점들을 나열하면 아래와 같다.
- 내부 구현 방식을 드러내어 윗 레벨 API 를 오염시킬 수 있다.
- 내부 구현 방식을 바꾸게 되면서 갑자기 다른 예외가 튀어나와 기존에 사용하던 위 레벨의 API가 깨질 수 있다.

이를 해결하기 위해 추상화 수준에 맞는 예외를 사용해야하고, 이를 예외 번역(exception translation)이라고 한다.
```java
try {
    ...  // 저수준 추상화를 이용한다.
} catch (final LowerLevelException e) {
    // 추상화 수준에 맞게 번역한다.
    throw new HigherLevelException(...);
}
```

자바 API로 예를 들면 `AbstractSequentialList`이다.

```java
/**
 * 이 리스트 안의 지정한 위치의 원소를 반환한다.
 * @throws IndexOutOfBoundsException index가 범위 밖이라면, 즉 ({@code index < 0 || index >= size()})이면 발생한다.
 */
public E get(int index) {
    ListIterator<E> i = listIterator(index);
    try {
        return i.next();
    } catch (NoSuchElementException e) {
        throw new IndexOutOfBoundsException("index: " + index);
    }
}
```

이렇게 내부 구현 로직중 발생하는 `NoSuchElementException`을 상위레벨로 전파하지 않고, 추상화 수준에 맞는 `IndexOutOfBoundsException`을 전파함으로써 내부 구현방식이 바뀌어도 전파되는 예외는 `IndexOutOfBoundsException` 로 동일하다.

## 저수준 예외가 디버깅에 도움이 된다면 예외 번역을 사용할 때 예외 연쇄를 사용하라

예외를 번역할 때 저수준 예외가 디버깅이 도움이 된다면 예외 연쇄(exception chaining)를 사용하는 것이 좋다.<br/>
예외 연쇄란 `문제의 근본 원인(cause)`인 저수준 예외를 고수준 예외에 실어 보내는 방식이며, 별도의 접근자 메서드(Throwable의 getCause 메서드)를 통해 언제든 저수준 예외를 꺼내볼 수 있다.
```java
try {
    ...  // 저수준 추상화를 이용한다.
} catch (LowerLevelException cause) {
    throw new HigherLevelException(cause);
}
```

예외 연쇄의 예시를 들면 아래와 같다.
```java
class HigherLevelException extends Exception {
    HigherLevelException(Throwable cause) {
        super(cause);
    }
}
```

위와 같이 생성자를 통해 저수준 예외를 제공하고, `Exception` 의 부모인 `Throwable` 의 `getCause()` 메서드를 통해 저수준 예외 또한 꺼내서 확인할 수 있다. <br/>
예외 연쇄를 통해 원인과 고수준 예외의 스택 추적 정보를 잘 통합해주는 장점이 있다.

## 예외번역을 사용할 수 없을 때는 차선책은 무엇일까?

개발을 하다보면, 아래 계층의 예외를 피할 수 없는 케이스도 존재하는데 이때는 상위 계층에서 그 예외를 조용히 처리하고 문제를 API 호출자까지 전파하지 않는 방법이 있다. <br/>
다만, 이경우 적절한 로깅 기능을 활용하여 기록하도록 하자.
