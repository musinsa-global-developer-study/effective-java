# 적시에 방어적 복사본을 만들라

## 자바는 안전한 언어다.
자바로 작성한 클래스는 시스템의 다른 부분에서 무슨 짓을 하든 그 불변식이 지켜진다.

## 그러나
아무런 노력 없이 다른 클래스로부터의 침범을 막을 수는 없다.

따라서, 우리는 클라이언트가 우리의 클래스에 침범하려고 혈안이 되어있다고 생각하고 방어적으로 프로그래밍 해야한다.

어떤 객체든 그 객체의 허락 없이는 외부에서 내부를 수정하는 일은 불가능 하지만, 

주의를 기울이지 않으면 자기도 모르게 수정이 가능하도록 허락하는 일이 생긴다
```java
public final class Period {
    private final Date start;
    private final Date end;

    public Period(Date start, Date end) {
        if (start.compareTo(end) > 0) 
            throw new IllegalArgumentException(start + "가 " + end + "보다 늦다.");
        this.start = start;
        this.end = end;
    }

    public Date start() {
        return start;
    }

    public Date end() {
        return end;
    }

    ...
}
```
언뜻 이 클래스는 불변처럼 보인다. (시작 시각이 종료 시각보다 늦을 수 없는 것 처럼 보인다.)

그러나 아래와 같이 하면 불변식을 깨뜨릴 수 있다.

```java
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
end.setYear(78); // p 내부 수정
```

## 해결
자바8 이후에서는 Date 대신 불변인 Instance 를 사용하면 된다. (혹은 LocalDateTime, ZonedDatedTime).

Date 는 더이상 사용하지 말도록 하자.

외부 공격으로부터 Period 인스턴스의 내부를 보호하려면 생성자에서 받은 가변 매개변수 각가을 방어적으로 복사(defensive copy)해야 한다.
그런 다음 인스턴스 안에서는 원본이 아닌 복사본을 사용한다.

```java
public Period(Date start, Date end) {
    this.start = new Date(start.getTime());
    this.end = new Date(end.getTime());
    
    if (this.start.compareTo(this.end) > 0) 
        throw new Illethrow new IllegalArgumentException(start + "가 " + end + "보다 늦다.");
}
```

이와 같이하면 앞의 공격은 더 이상 Period 에 위협이 되지 않는다.

매개변수의 유효성을 검사하기 전에 방어적 복사본을 만들고, 이 복사본으로 유효성을 검사한 점에 주목하자.

멀티 스레딩 환경이라면 유효성 검사한 후 복사본을 만드는 그 찰나에 다른 스레드가 원본 객체를 수정할 위험이 있기 때문이다. 
(검사시점/사용시점 공격, time-to-check/time-of-use TOCTOU)

방어적 복사에 clone 메서드를 사용하지 않은 점에도 주목하자. Date는 final 이 아니므로(상속이 가능하므로) clone이 Date가 정의한 게 아닐 수 있다. 즉, clone이 악의를 가진 하위 클래스의 인스턴스를 반환할 수도 있다. 

매개변수가 제3자에 의해 확장될 수 있는 타입이라면 방어적 복사본을 만들 때 clone을 사용해서는 안된다.


생성자를 수정하여 앞의 공격은 막았지만, Period 인스턴스는 아직 변경 가능하다.
```java 
Date start = new Date();
Date end = new Date();
Period p = new Period(start, end);
p.end().setYear(78); // p 내부 수정
```

두 번째 공격을 막아내려면 단순히 접근자가 가변 필드의 방어적 복사본을 반환하면 된다.

```java
public Date start() {
    return new Date(start.getTime());
}

public Date end() {
    return new Date(end.getTime());
}
```

위와 같이 하면 아무리 악의적인 프로그래머라도 시작 시각이 종료 시각보다 나중일 수 없다는 불변식을 위배할 방법이 없다.

생성자와 달리 접근자 메서드에서는 방어적 복사에 clone() 을 사용해도 된다.
Period가 가지고 있는 객체가 Date 임이 확실하기 때문이다.

그렇더라도 인스턴스를 복사하는 데는 일반적으로 생성자나 정적 팩터리를 쓰는게 좋다.
(item13. clone 재정의는 주의해서 진행해라)

## 또 다른 이유
매개변수를 방어적으로 복사하는 목적이 불변 객체를 만들기 위해서만은 아니다. 

메서드든 생성자든 클라이언트가 제공한 객체의 참조를 내부의 자료구조에 보관해야 할 때면 항시 그 객체가 잠재적으로 변경될 수 있는지를 생각해야 한다.
변경될 수 있다면, 그 객체가 클래스에 넘겨진 뒤 임의로 변경되어도 그 클래스가 문제없이 동작할지를 따져보라. 확신할 수 없다면 복사본을 만들어 저장해야 한다.

반환할 때도 내부 객체가 가변이고 안심할 수 없다면 방어적 복사본을 반환해야 한다.

길이가 1 이상인 배열은 무조건 가변임을 잊지말자. 내부에서 사용하는 배열을 클라이언트에 반환할 때는 항상 방어적 복사를 수행해야 한다. 혹은 배열의 불변 뷰를 반환하는 대안도 있다. (item15)

위의 모든 작업에서 우리는 ``되도록 불변 객체들을 조합해 객체를 구성해야 방어적 복사를 할 일이 줄어든다.``는 교훈을 얻을 수 있다. (item17)

Date.getTime() 이 반환하는 long 정수를 사용한다던지..


방어적 복사에는 성능 저하가 따르고, 또 항상 쓸 수 있는 것도 아니다. 호출자가 컴포넌트 내부를 수정하지 않으리라 확신한다면 방어적 복사를 생략할 수 있다. 이러한 상황이라도 호출자에서 해당 매개변수나 반환값을 수정하지 말아야 함을 명확히 문서화하는 게 좋다.

클라이언트가 넘긴 객체의 통제권을 완전히 넘겨받기로 한다면 방어적 복사본이 필요없을 수 있다. 그러나 클라이언트가 더이상 해당 객체를 직접 수정하는 일이 없다고 약속해야 한다.
(ex. item18 래퍼 클래스 패턴)


