# 예외는 진짜 예외 상황에만 사용하라

## 예외는 오직 예외 상황에서만 써야한다.

예외를 예외 상황이 아닌 정상적인 제어 흐름용으로 쓰여서는 절대로 안된다.
코드를 통해 설명하면,

```java
// 잘못된 추론으로 예외를 정상적인 제어 흐름용으로 사용한 코드
public static void main(String[] args){
    final Mountain[] mountains = new Mountain[]{
        new Mountain("한라산"),
        new Mountain("백두산")
    };
    
    try {
        int i = 0;
        while(true) {
            mountains[i++].climb();
        }
    } catch (ArrayIndexOutOfBoundsException e) {
    }
}
```

```java
// 표준적인 관용구대로 작성한 코드
public static void main(String[] args){
    final Mountain[] mountains = new Mountain[]{
        new Mountain("한라산"),
        new Mountain("백두산")
    };
    
    for(Mountain mountain : mountains) {
        mountain.climb();
    }
}
```

우선, 위의 첫번째 코드처럼 예외를 정상적인 제어 흐름용으로 사용한 이유는 잘못된 추론을 근거로 성능을 높여보려 한 것이다.<br/>
JVM은 배열에 접근할 때마다 경계를 넘지 않는지 검사하는데, 일반적인 반복문도 배열 경계에 도달하면 종료한다. 따라서, 이 검사를 반복문에도 명시하면 같은 일이 중복되므로 하나를 생략한 것이다.

하지만, 위에서 이야기한 것처럼 잘못된 추론이다. 
두번째 코드처럼 배열을 순회하는 표준 관융구로 작성해도 앞서 걱정한 중복검사를 수행하지 않는다. JVM 이 알아서 최적화해 없애준다.<br/>
첫번째 코드로 작성할 경우 오히려 가독성이 떨어지며, 의도치 않게 사이드 이펙트가 발생될 수 있다. 한가지 예시로, climb() 로직 중에 ArrayIndexOutOfBoundsException 를 던지는 경우도 정상적인 반복문 종료로 오해하는 경우가 생길 수 있다.

이처럼, `예외는 오직 예외 상황에서만 써야한다. 절대로 정상적인 제어 흐름용으로 쓰여서는 안된다.`

## 잘 설계된 API 라면 클라이언트가 정상적인 제어 흐름에서 예외를 사용할 일이 없게 해야한다.

만약, 특정 상태에서만 호출할 수 있는 '상태 의존적' 메서드를 제공하는 클래스의 경우 '상태 검사' 메서드도 함께 제공해야한다.
코드로 예를 들면,

```java
public interface Vehicle {

    void move();    // 상태에 의존적 메서드
}

public class Car implements Vehicle {
    
    private final String status;
    private final Position position;
    
    ...
    
    public void move() {
        if (status.equals("POWER_OFF")) {
            throw new IllegalStateException();
        }
        position.increase();
    }
    
    // override eqauls() and hashcode()...
}
```

위의 코드처럼 status 라는 값에 의존적인 메서드를 제공하는 클래스의 경우 '상태 검사' 메서드도 함께 제공해야 해당 잘 설계된 API 이다. <br/>
위의 코드의 경우 상태검사 메서드가 없기 때문에 사용하는 클라이언트가 정상흐름에서 어쩔 수 없이 예외에 대한 후처리를 해야하기 때문에 아래 코드와 같이 '상태 검사' 메서드를 제공해주어야 잘 설계된 API 이다.

```java
public interface Vehicle {
    
    void move();    // 상태에 의존적 메서드
    boolean movable();  // 상태 검사 메서드
}

public class Car implements Vehicle {
    
    private final String status;
    private final Position position;
    
    ...
    
    public void move() {
        if (status.equals("POWER_OFF")) {
            throw new IllegalStateException();
        }
        position.increase();
    }
    
    public boolean movable() {
        return !status.equals("POWER_OFF");
    }
    
    // override eqauls() and hashcode()...
}
```
