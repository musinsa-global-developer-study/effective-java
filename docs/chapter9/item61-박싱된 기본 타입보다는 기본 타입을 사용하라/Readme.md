# Item 61. 박싱된 기본 타입보다는 기본 타입을 사용하라

## java 의 데이터 타입
1. 기본 타입
    ```java 
    int, double, boolean, ...
    ```
2. 참조 타입
    ```java 
    String, List, ...
    ```

## 기본 타입은 각각의 기본 타입에 대응하는 참조 타입이 하나씩 있다.
```java
int -> Integer
double -> Double
boolean -> Boolean
```

오토박싱과 오토언박싱 덕분에 두 타입을 크게 구분하지 않고 사용할 수는 있지만, 차이가 사라지는 것은 아니다.

## 차이
1. 기본 타입은 값만 가지고 있으나, 박싱된 기본 타입은 값 + 식별성(identity) 란 속성을 갖는다. (= 박싱된 기본 타입의 두 인스턴스는 값이 같아도 서로 다르다고 식별될 수 있다.)
2. 기본 타입의 값은 항상 유효하지만 박시오딘 기본타입은 null을 가질 수 있다.
3. 기본 타입이 박싱된 기본 타입보다 시간과 메모리 사용면에서 더 효율적이다.

## 문제점
```java
Comparator<Integer> natualOrder = (i, j) -> (i < j) ? -1 : (i == j ? 0 : 1));

---

naturalOrder.compare(new Integer(42), new Integer(42)) => 1

```

## 원인

1. (i < j) 는 잘 작동한다. i, j 가 참조하는 Integer 인스턴스는 기본 타입 값으로 변환된다.
2. (i == j) 에서 두 객체 참조의 식별성을 검사하게 된다.

i와 j는 각각 다른 객체를 참조하므로 결과는 false가 되고 1 이 반환된다.

이처럼 박싱된 기본 타입에 ``==`` 을 사용하면 오류가 일어난다.

---

```java
public class Unbelievable {
    static Integer i;

    public static void main(String[] args) {
        if (i == 42) 
            System.out.println("unbelievable");
    }
}

=> Integer 의 초기값은 null 이므로  NullPointerException 이 발생한다.
```
기본 타입과 박싱된 기본 타입을 혼용한 연산에서는 박싱된 기본 타입의 박싱이 자동으로 풀리는데, 이 때 null 참조를 언박싱하면 NPE 가 발생한다.

i 를 int 로 선언하면 해결된다.



```java
public static void main(String[] args) {
    Long sum = 0L;
    for (long i = 0; i <= Integer.MAX_VALUE; i++) {
        sum += i;
    }
    System.out.println(sum);
}

```
위 코드는 ㅂ오류나 경고 없이 컴파일되지만, 박싱과 언박싱이 반복해서 일어나 체감될 정도로 성능이 느려진다.

## 박싱된 기본 타입을 써야 하는 경우
1. 컬렉션의 원소, 키, 값으로 쓴다. 컬렉션은 기본 타입을 담을 수 없으므로 어쩔 수 없이 박싱된 기본 타입을 써야한다.
2. 리플렉션아ㅡㄹ 통해 메서드를 호출할 때도 박싱된 기본 타입을 사용해야 한다. (item65)