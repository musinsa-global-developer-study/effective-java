# public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라

## 패키지 바깥에서 접근할 수 있는 클래스라면 접근자를 제공하라

public 클래스의 경우 패키지 바깥에서 접근할 수 있기 때문에 접근자 제공하여, 속성을 바로 접근하지 않고 메서드(getter)를 통해 제공해야한다.<br/>
속성을 제공하지 않고 메서드로 제공함에 따라 클래스 내부 표현 방식을 언제든 바꿀 수 있는 유연성을 얻을 수 있다.

```java
public class Point {
    
    private double x;
    private double y;

    public Point(final double x, final double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
}
```

## 그렇다면, 모든 클래스의 속성은 다 private일까?

패키지 바깥에서 접근할 수 없는 클래스의 경우 속성을 노출해도 괜찮다. <br/>
즉, package-private 클래스 혹은 private 중첩 클래스라면 속성을 public 하게 노출해도 문제가 없다. <br/>
대신, 클래스가 표현하려는 추상 개념만 올바르게 표현해주면 된다.

## 불변 클래스면 속성을 노출해도 될까?

결론은, No 다. 불변 클래스라서 의도치않게 속성값이 변경되지는 않아 불변식은 보장하지만 아래와 같은 단점이 있다.
- 속성명을 수정하는데 외부에 노출된 속성명이기 때문에 변경에 어려움이 있어 유연성이 떨어진다.
- 속성을 읽을 때 부수 작업을 수행할 수 없다.

```java
public final class Time {
    
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;
    
    public final int hour;
    public final int minute;

    public Time(final int hour, final int minute) {
        ...
        this.hour = hour;
        this.minute = minute;
    }
}
```