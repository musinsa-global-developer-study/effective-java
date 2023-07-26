# 가변인수는 신중히 사용하라

가변인수 메서드는 명시한 타입의 인수를 0개 이상 받을 수 있다.
가변인수 메서드를 호출하면, 먼저 인수의 개수와 길이가 같은 배열을 생성하고 인수들을 이 배열에 저장하여 가변인수 메서드에 건네준다.

```java
static int sum(int... args) {
    int sum = 0;
    for (int arg: args) {
        sum += arg;
    }
    return sum;
}

-------------------
sum(1,2,3) = 6
sum() = 0
```

인수 개수는 런타임에 자동 생성된 배열의 길이로 알 수 있다.

```java
if (args.length == 0) {
    throw new IllegalArgumentException("인수가 1개 이상 필요합니다.");
}
```
이 방식에는 문제가 있는데, 인수 0개를 넣어 호출하면 컴파일타임이 아닌 런타임에 실패한다는 점이다.

```java
static int min(int firstArg, int... remainingArgs) {
    int min = firstArg;
    for (int arg: remainingArgs) {
        if (arg < min) {
            min = arg;
        }
    }
    return min;
}
위와 같이 첫 인수를 명시적으로 받고 나머지만 가변인수로 받으면 해결된다.

java 에 printf 가 도입될 때 가변인수와 함께 도입되었고 리플렉션도 재정비되어 가변인수의 덕을 보고있다.

하지만 성능에 민감한 상황이라면 가변인수가 걸림돌이 될 수 있다.
가변인수 메서드는 호출될 때마다 배열을 새로 하나 할당하고 초기화하기 때문이다.

비용을 줄이고 가변인수의 유연성을 늘리고 싶을 때는 다음과 같이 할 수 있다.
(해당 메서드 사용의 95%가 인수를 3개 이하로 사용한다고 가정하면)
```java
public void foo() {}
public void foo(int a1) {}
public void foo(int a1, int a2) {}
public void foo(int a1, int a2, int a3) {}
public void foo(int a1, int a2, int a3, int... rest) {}
```
EnumSet의 정적 팩터리도 이 기법을 사용해 열거 타입 집합 생성 비용을 최소화한다.

```java

public static <E extends Enum<E>> EnumSet<E> of(E e) {
    EnumSet<E> result = noneOf(e.getDeclaringClass());
    result.add(e);
    return result;
}

public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2) {
    EnumSet<E> result = noneOf(e1.getDeclaringClass());
    result.add(e1);
    result.add(e2);
    return result;
}

public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3) {
    EnumSet<E> result = noneOf(e1.getDeclaringClass());
    result.add(e1);
    result.add(e2);
    result.add(e3);
    return result;
}

public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4) {
    EnumSet<E> result = noneOf(e1.getDeclaringClass());
    result.add(e1);
    result.add(e2);
    result.add(e3);
    result.add(e4);
    return result;
}

public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4,
                                                E e5)
{
    EnumSet<E> result = noneOf(e1.getDeclaringClass());
    result.add(e1);
    result.add(e2);
    result.add(e3);
    result.add(e4);
    result.add(e5);
    return result;
}

@SafeVarargs
public static <E extends Enum<E>> EnumSet<E> of(E first, E... rest) {
    EnumSet<E> result = noneOf(first.getDeclaringClass());
    result.add(first);
    for (E e : rest)
        result.add(e);
    return result;
}
```



## @SafeVarargs
```java
public static <T> T[] unsafe(T... elements) {
    return elements; // unsafe! don't ever return a parameterized varargs array
}

public static <T> T[] broken(T seed) {
    T[] plant = unsafe(seed, seed, seed); // broken! This will be an Object[] no matter what T is
    return plant;
}

public static void plant() {
   String[] plants = broken("seed"); // ClassCastException
}

warning: [unchecked] Possible heap pollution from parameterized vararg type T
  public static <T> T[] unsafe(T... elements) {

---------------

public class Machine<T> {
    private List<T> versions = new ArrayList<>();

    @SafeVarargs
    public final void safe(T... toAdd) {
        for (T version : toAdd) {
            versions.add(version);
        }
    }
}


```