# Item43 람다보다는 메서드 참조를 사용하라


- 람다가 익명 클래스보다 나은 점 중 가장 큰 특징은 간결함이다. 그런데 자바에는 함수 객체를 람다보다도 더 간결하게 만드는 방법이 있다. 
- = **메서드 참조**

```java
map.merge(key, 1, (count, incr) -> count + incr);
// key 가 존재하면 count + 1 을 하고 
// 존재하지 않으면 key: 1 을 push 한다.
```
위 코드는 깔끔해 보이지만 거추장스러운 부분이 남아있다.
매개변서 count와 incr은 크게 하는 일 없이 공간을 꽤 차지한다. 
자바 8이 되면서 Integer 클래스(와 모든 기본타입의 박싱 타입)는 이 람다와 기능이 같은 정적 메서드 sum을 제공하기 시작했다.

```java
map.merge(key, 1 Integer::sum);
```
매개 변수 수가 늘어날수록 메서드 참조로 제거할 수 있는 코드양도 늘어난다.

하지만 어떤 람다에서는 매개변수의 이름 자체가 프로그래머에게 좋은 가이드가 되기도한다.
이런 람다는 길이는 길지만 메서드 참조보다 읽기 쉽고 유지보수도 쉬울 수 있다.

람다로 할 수 없는 일이라면 메서드 참조로도 할 수 없다.
```java
// 되는 케이스
// 제네릭 함수 타입 구현

interface G1 {
    <E extends Exception> String m() throws E;
}

interface G2 {
    <F extends Exception> String m() throws Exception;
}

interface G extends G1, G2 {}

// 이 때 함수형 인터페이스 G를 함수 타입으로 표현하면 다음과 같다.
<F extends Exception> ()->String throws F

이처럼 함수형 인터페이스를 위한 제네릭 함수 타입은 메서드 참조 표현식으로는 구현할 수 있지만, 람다식으로는 불가능하다.
제네릭 람다식이라는 문법이 존재하지 않기 때문이다.

```

그렇더라도 메서드 참조를 사용하는 편이 보통은 더 짧고 간결하므로, 람다로 구현했을 때 너무 길거나 복잡하다면 메서드 참조가 좋은 대안이 되어준다.

메서드와 람다가 같은 클래스 안에 있을 때는 람다가 메서드 참조보다 간결할 때가 있다.
```java
service.execute(GoshThisClassNameIsHumongous::action);

service.exeute(() -> action());

Function.identity() 를 사용하기보다는 (x -> x) 를 직접 사용하는 편이 코드도 짧고 명확하다.

```

# 메서드 참조의 유형
1. 정적 메서드를 가리키는 메서드 참조
```java 
Integer::parseInt
```
2. 수신 객체를 특정하는 한정적 인스턴스 메서드 참조 (bound)

   근본적으로 정적 참조와 비슷하다. 함수 객체가 받는 인수와 참조되는 메서드가 받는 인수가 똑같다.
```java 
Instant.now()::isAfter
```
3. 수신 객체를 특정하지 않은 비한정적 인스턴스 메서드 참조 (unbound)

   함수 객체를 적용하는 시점에 수신 객체를 알려준다. 이를 위해 수신 객체 전달용 매개변수가 매개변수 목록의 첫 번째로 추가되며, 그 뒤로는 참조되는 메서드 선언에 정의된 매개변수들이 뒤따른다. 주로 스트림 파이프라인에서의 매핑과 필터 함수에 쓰인다. (item 45)
```java 
String::toLowerCase
```
4. 클래스 생성자를 가리키는 메서드 참조
```java
   TreeMap<K,V>::new
```
5. 배열 생성자를 가리키는 메서드 참조
```java 
    int[]::new
```

