# 다 쓴 객체 참조를 해제하라

## 메모리 누수

자바는 가비지 컬렉터가 다 쓴 객체를 알아서 회수해가기 때문에 메모리 관리가 수월하다. 하지만, 그렇다고 메모리 관리에 더이상 신경 쓰지 않아된다는 이야기는 절대 아니다. 

```java
import java.util.Arrays;
import java.util.EmptyStackException;

public class Stack {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private Object[] elements;
    private int size = 0;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(final Object object) {
        ensureCapacity();
        elements[size++] = object;
    }

    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        return elements[--size];
    }

    /**
     * 원소를 위한 공간을 적어도 하나 이상 확보한다.
     * 배열 크기를 늘려야 할 때마다 대략 두 배씩 늘린다.
     */
    private void ensureCapacity() {
        if (elements.length < size) {
            return;
        }
        elements = Arrays.copyOf(elements, 2 * size + 1);
    }
}
```

위의 코드의 보았을 때 별도로 문제가 없어보인다. 하지만 해당 코드는 메모리 누수가 발생하는 코드이다. <br/>
메모리가 누수되면서 성능이 저하되고, OOM 까지 발생할 수 있다.

메모리가 누수되는 곳은 어디일까?

이 코드에서는 스택이 커졌다가 줄어들었을 때 스택에서 꺼내진 객체들을 가비지 컬렉터가 회수하지 않는다. 프로그램에서 더이상 그 객체들을 사용하지 않더라도 말이다. <br/>
이 스택이 그 객체들의 `다 쓴 참조(obsolete reference)`를 여전히 가지고 있기 때문이다.

> 다 쓴 참조란 문자 그대로 앞으로 다시 쓰지 않을 참조를 뜻한다.

위의 코드는 elements 배열의 '활성 영역' 밖의 참조들이 모두 `다 쓴 참조`를 의마한다.

> 활성 영역은 인덱스가 size 보다 작은 원소들로 구성된다.

## 그렇다면 어떻게 메모리 누수를 방지할까?

### 해당 참조를 해제하라

위의 코드의 해법은 해당 참조를 해제(null 처리)하면 된다.

```java
public Object pop() {
    if (size == 0) {
        throw new EmptyStackException();
    }
    final Object result = elements[--size];
    elements[size] = null; // 다 쓴 참조 해제
    return result;
}
```

다 쓴 참조를 null 처리하면 다른 이점도 따라온다. 만약, null 처리한 참조를 실수로 사용하려 하면 프로그램에서 NPE 를 던지며 종료한다. <br/>

하지만, 모든 객체를 null 처리하는 것은 바람직하지 않다. `객체 참조를 null 처리하는 일은 예외적인 경우여야 한다.` <br/>
그렇다면, 가장 좋은 방법은 무엇일까?

### 참조를 담은 변수를 유효 범위 (scope) 밖으로 밀어내라

개발자가 변수의 범위를 최소가 되게 정의했다면 (아이템 57) 자연스럽게 다 쓴 참조를 해제하게 된다.

### 그렇다면, null 처리는 언제할까?

Stack 클래스와 같이 자기 메모리를 직접 관리하는 클래스의 경우 null 처리를 통해 참조 해제해야한다. <br/>
스택은 (객체 자체가 아니라 객체 참조를 담는) elements 배열로 저장소 풀을 만들어 원소들을 관리한다. 배열의 `활성 영역`에 속한 원소들이 사용되고 `비활성 영역`은 쓰이지 않는다. <br/>
문제는 가비지 컬렉터는 비활성 영역에서 참조하는 객체도 똑같이 유효한 객체이고, 더 이상 쓸모없다는 건 프로그래머만 아는 사실이다.

그렇기 때문에, `비활성 영역`이 되는 순간 null 처리해서 해당 객체는 더이상 쓰지 않는다는 것을 가비지 컬렉터에 알려야한다.

즉, **자기 메모리를 직접 관리하는 클래스라면 프로그래머는 항시 메모리 누수에 주의해야 한다.** <br/> 
원소를 다 사용한 즉시 그 원소가 참조한 객체들을 다 null 처리해야한다.

### 또다른 메모리 누수 주범은 누구일까?

캐시 역시 메모리 누수를 일으키는 주범이다. 그렇기 때문에 캐시 외부에서 키(key)를 참조하는 동안만(값이 아니다) 엔트리가 살아있는 캐시가 필요한 상황이라면 `WeakHashMap` 사용해 캐시를 만들자.

> WeakHashMap은 다 쓴 엔트리는 즉시 자동으로 제거한다.

단, `WeakHashMap` 은 위의 상황에서만 유용한다. 다른 방법은 `TTL 을 활용`하는 것이다.

TTL 을 활용하는 방법으로는 보통 캐시 엔트리의 유효기간을 정확히 정의하기 어렵기 때문에 다음과 같은 방법이 있다.

- 시간이 지날수록 엔트리의 차치를 떨어뜨리는 방식으로써 (ScheduledThreadPoolExcutor 같은) 백그라운드 스레드를 활용해 주기적으로 정리
- 캐시에 새 엔트리를 추가할 때 부수 작업으로 수행하는 `LinkedHashMap` 의 removeEldestEntry() 메서드도 있다.

> 스프링의 경우 캐시 관련해서 어노테이션이 있으며, 로컬메모리 캐시(카페인) 또한 TTL 설정도 가능하다. 

### 메모리 누수 주범은 끝인가?

세번째 주범은 리스터 혹은 콜백이다. 클라이언트가 콜백을 등록만 하고 명확히 해지하지 않는다면, 뭔가 조치해주지 않는 한 콜백은 계속 쌓여갈 것이다. <br/>
이럴 때 콜백을 약한 참조(weak reference)로 저장하면 가비지 컬렉터가 즉시 수거해간다. <br/>
예를 들어 `WeakHashMap` 에 키로 저장하면 된다.
