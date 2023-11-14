# 지연 초기화는 신중히 사용하라.

## 지연 초기화란?

필드를 사용할 때까지 초기화를 늦추는 기법이다.

주로 성능을 최적화 하거나 초기화시에 발생하는 위험한 순환 문제를 해결하는 효과도 있다.

자주 호출되지는 않는데 초기화 비용은 큰 필드들을 처리할때 유용하다.

그러나... `지연 초기화는 가능하면 필요한 순간까지는 사용하지 말라`라는 격언이 있으니 신중히 써라.

## 왜 신중히 쓰라는 거야?

클래스 혹은 인스턴스 생성 시 초기화 비용은 줄겠지만, 대신에 지연 초기화하는 필드에 접근하는 비용이 커진다. (초기화 되었는지 상태검사를 계속해서 해줘야하기 떄문)

초기화된 필드를 얼마나 빈번히 호출하는지, 실제 초기화에 드는 비용에 따라 성능이 오히려 줄어들 수도 있다. 

## 그럼에도 불구하고 지연 초기화가 필요한 상황은?

필드를 사용하는 인스턴스의 비율이 낮은 반면, 그 필드를 초기화하는 비용이 크다면 지연 초기화가 제 역할을 해줄 수 있다.

다만, 멀티 쓰레드 환경에서 지연 초기화는 다소 까다로운 작업이다.

> 지연 초기화하는 필드를 둘 이상의 쓰레드가 공유한다면 어떤 형태로든 반드시 동기화를 해줘야 한다!

그렇다보니... `대부분의 상황에서 일반적인 초기화가 지연 초기화보다 낫다.`

## 지연 초기화 기법 1 - synchronized 접근자 방식

아래는 보통의 초기화 방법이다.

```java
private final FieldType field = computerFieldValue();
```


`초기화 순환성`이 걱정된다면 `synchronized` 접근자를 사용하면 된다.

```java
private FieldType field;

private Synchronized FieldType getField() {
    if (field == null) {
        field = computeFieldValue();
        return field;
    }
}

```

## 지연 초기화 기법 2 - 홀더 클래스 관용구

성능 떄문에 *정적 필드*를 지연 초기화해야 한다면 `지연 초기화 홀더 클래스 기법`을 활용 해보자.

getField() 메소드가 호출되는 순간에 처음으로 field가 읽히면서 초기화 된다.

아래 코드의 멋진점은 동기화를 전혀 하지 않기 떄문에 성능이 느려질 거리가 없다.

VM 내부적으로 클래스 초기화시 동기화를 수행한다. 이후에는 VM이 동기화 코드를 제거하여, 그다음부터는 아무런 검사나 동기화 없이 필드에 접근하게 된다.


```java
private static class FieldHolder {
    static final FieldType field = computeFieldValue();
}

private static FieldType getField() {
    return FieldHolder.field;
}

```

## 지연 초기화 기법 3 - 이중 검사(double-check)

성능 때문에 인스턴스 필드를 지연 초기화 해야한다면 `이중검사(double-check)` 관용구를 사용하자.

result 변수는 CPU 캐싱을 위해 존재하는 변수다. 없어도 동작에 문제는 없으나 속도 측면에서 보면 이득이다. (field 접근 횟수를 최소화)

```java
private volatile FieldType field; // 메인 메모리 적재

private FieldType getField() {
    FieldType result = field;
    if (result != null) // 첫 번째 검사 (lock 사용안함)
        return result;
    
    synchronized(this) {
        if (field == null) // 두번째 검사 (lock 사용)
            field = computeFieldValue();
        return field;
    }
    
}

```

### 지연 초기화 기법 4 - 단일 검사(single-check)

반복해서 초기화 해도 상관없는 인스턴스 필드를 지연 초기화 해야한다면 `단일 검사(single-check)` 관용구를 사용하자.

이중 검사 관용구에서 중복 초기화를 방지하는 로직을 제거한 모습일뿐이다.

당연하지만... 모든 쓰레드가 필드값을 다시 계산해도 상관이 없는 경우에 사용해야한다.

참고로, 단일 검사는 아주 이례적인 기법으로 보통은 거의 쓰지 않는다.

```java
private volatile FieldType field; // 메인 메모리 적재

private FieldType getField() {
    FieldType result = field;
    if (result == null)
        field = result = computeFieldValue(); // 
    return result;
    
    
}
```