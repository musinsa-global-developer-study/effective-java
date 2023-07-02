# 비검사 경고를 제거하라.
제네릭을 사용하게 되면 수많은 컴파일 경고를 보게 될것이다.

## 비검사 경고 예시
```java
Set<Lark> exaltation = new HashSet();
```
위 코드를 컴파일 하면 컴파일러가 경고 문구를 출력한다. (명령줄 인수에 `-Xlint:unchecked`추가 필요!)

HashSet의 타입이 확정되지 않았기 때문에 발생한 경고.
```
Item27Main.java:10: warning: [unchecked] unchecked conversion
        Set<Lark> exaltation = new HashSet();
                               ^
  required: Set<Lark>
  found:    HashSet
1 warning
```

컴파일러가 알려준대로 수정해보자.
Java 7부터는 다이아몬드 연산자만 써줘도 컴파일러가 타입을 추론해서 처리한다.
```java
Set<Lark> exaltation1 = new HashSet<>(); // 자바 7부터
Set<Lark> exaltation2 = new HashSet<Lark>(); // 자바 7 이전
```

위 예시처럼 가능한 모든 비검사 경고를 제거하자!

## @SuppressWarnings("unchecked")
코드로 경고를 제거할 수는 없지만 타입이 안전하다고 확신할 수 있다면 `@SuppressWarnings("unchecked")`어노테이션을 사용해서 경고를 숨기자.

경고를 숨기지 않는다면 자칫 중요한 메시지를 놓칠수가 있다. 여기서 조심해야할 점은 `검증되지 않은 경고마저 숨기는 것`이다.

검증되지 않은 경고를 숨긴다면 런타임에서 여전히 ClassCastException을 던질 수 있게된다.

## @SuppressWarnings("unchecked") 사용
`@SuppressWarnings("unchecked")` 애너테이션은 항상 가능한 좁은 범위에 적용하자. 

심각한 경고를 놓칠 수 있으니 절대로 클래스 전체에 적용해서는 안된다.

한 줄이 넘는 메서드나 생성자에 달린 `@SuppressWarnings("unchecked")` 어노테이션을 발견하면 지역변수 선언 쪽으로 옮기자.
이를 위해 지역변수를 새로 선언해야하는 경우도 있지만 그만한 값어치가 있을 것이다.

아래는 ArrayList의 toArray 메소드 코드이다.
```java
public <T> T[] toArray(T[] a) {

    if (a.length < size) {
        return (T[]) Arrays.copyOf(elements, size, a.getClass());
    }

    if (a.length > size) {
        a[size] = null;
    }
    return a;

}
```
이 코드를 compile하면 다음과 같은 문구가 출력된다.
```
Item27Main.java:28: warning: [unchecked] unchecked cast
            return (T[]) Arrays.copyOf(elements, size, a.getClass());
                                      ^
  required: T[]
  found:    Object[]
  where T is a type-variable:
    T extends Object declared in method <T>toArray(T[])
1 warning
```
이 경고 제거를 위해선 `@SuppressWarnings("unchecked")`을 return문에 달아야하지만, 이 기능은 지원하지 않는다.

따라서 아래와 같이 코드를 변경한다.

```java
public <T> T[] toArray(T[] a) {

    if (a.length < size) {
        // 생성한 배열과 매개변수로 받은 배열의 타입이 모두 T[]로 같으므로 올바른 형변환이다
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Arrays.copyOf(elements, size, a.getClass());
        return result;
    }

    if (a.length > size) {
        a[size] = null;
    }
    return a;

}
```
`@SuppressWarnings("unchecked")` 어노테이션을 사용할 때면 그 경고를 무시해도 안전한 이유를 항상 주석으로 남겨야한다!