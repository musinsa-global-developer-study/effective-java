# 필요 없는 검사 예외 사용은 피하라

## 검사예외(Checked Exception)을 남용하면 고통스럽다.

검사예외는 발생한 exception 에 대해 프로그래머가 처리해야하기 때문에 안정성을 높여준다. <br/>
하지만, 2가지 단점이 존재한다.

- Checked Exception 을 던지는 메서드를 호출하는 코드에서 catch 블록을 두어 그 예외를 붙잡아 처리하거나 더 밖으로 던져 exception을 전파해야하는 번거로움이 추가된다.
- Checked Exception 을 던지는 메서드는 Stream 안에서 직접 사용할 수 없기 때문에 자바 8부터는 부담이 더욱 커진다.

그렇기 때문에, 프로그래머가 예외상황에서 복구할 방법이 없다면 Unchecked Exception 을 사용하는 것이 좋다.

## 검사예외를 회피하는 방법

### 1. Optional을 사용하라

Checked Exception 을 던지는 대신 빈 옵셔널 `Optional.empty()`를 사용하는 것이다.

- 단점: 예외가 발생한 이유를 알려주는 부가정보를 담을 수 없다. 예외를 사용하면 구체적인 예외 타입과 그 타입에 제공하는 메서드들을 활용해 부가정보를 제공할 수 있다. (Item 70)

### 2. 메서드를 2개로 쪼개 비검사 예외로 바꿔라

첫번째 메서드는 예외가 던져질지 여부를 boolean 값으로 반환하는 것이다.

``` java
// 리팩터링 전
try {
    obj.action(args);
} catch (TheCheckedException e) {
    ... // 예외 상황에 대처한다.
}


// 리팩터링 후
if (obj.actionPermitted(args)) {
    obj.action(args);
} else {
    ... // 예외 상황에 대처한다.
}
```
