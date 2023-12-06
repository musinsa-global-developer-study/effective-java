

자바가 람다를 지원하면서 API를 작성하는 모범 사례도 크게 바뀌었다.

상위 클래스의 기본 메서드를 재정의해 원하는 동작을 구현하는 `템플릿 메서드 패턴` 의 매력이 크게 줄었다.

이를 대체하는 현대적인 해법은 같은 효과의 함수 객체를 받는 적적 팩터리나 생성자를 제공하는 것이다.              
이 내용을 일반화해서 말하면 함수 객체를 매개변수로 받는 생성자와 메서드를 더 많이 만들어야 한다. 이때 함수형 매개변수 타입을 올바르게 선택해야 한다.


```java

class LinkedHashMap {
    
    
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > 100;
    }
    
}


```
LinkedHashMap의 removeEldestEntry 를 위와같이 재정의하면 캐시로 사용할 수 있다.     
put 메서드는 이 메서드를 호출하여 true를 반환하면 맵에서 가장 오래된 원소를 제거한다. 따라서 가장 최근 100개 원소만 유지된다.

위 방법도 잘 동작하지만 람다를 사용하면 훨씬 잘 해낼 수 있다.           
removeEldestEntry 는 Map.Entry<K, V> 를 인수로 받아 boolean을 반환해야 할 것 같지만, 꼭 그렇지는 않다. 
size() 를 사용해 맵 안의 원소 수를 알아내는데, 이는 removeEldestEntry가 인스턴스 메서드라서 가능한 방식이다.   
하지만 생성자에 넘기는 함수 객체는 이 맵의 인스턴스 메서드가 아니다. (팩터리나 생성자를 호출할 때는 맵의 인스턴스가 존재하지 않기 때문)
따라서 맵은 자기 자신도 함수 객체에 건네줘야 한다.

```java

@FunctionalInterface interface EldestEntryRemovalFunction<K, V> {
    boolean remove(Map<K, V> map, Map.Entry<K, V> eldest);
}

```
이 인터페이스도 잘 동작하지만 굳이 사용할 필요는 없다.             
자바 표준 라이브러리에 이미 같은 모양의 인터페이스가 준비되어 있기 때문이다.

java.util.function 패키지를 보면 다양한 용도의 표준 함수형 인터페이스가 담겨있다.   
`필요한 용도에 맞는 게 있다면, 직접 구현하지 말고 표준 함수형 인터페이스를 활용하라`

그러면 API가 다루는 개념의 수가 줄어들어 익히기 더 쉬워진다. 또한 표준 함수형 인터페이스들은 유용한 디폴트 메서드를 많이 제공하므로 다른 코드와의 상호 운용성도 크게 좋아질 것이다.

예를들어 Predicate 인터페이스는 Predicate 들을 조합하는 메서드를 제공한다. 

앞의 EldestEntryRemovalFunction 대신 표준 인터페이스인 `BiPredicate<Map<K, V>, Map.Entry<K, V>>`를 사용할 수 있다.

java.util.function 패키지에는 총 43개의 인터페이스가 담겨 있다. 전부 기억하긴 어렵지만 기본 인터페이스 6개만 기억하면 나머지를 충분히 유추해 낼 수 있다.

## Operator 인터페이스
반환값과 인수의 타입이 같은 함수

인수가 1개: UnaryOperator 
`T apply(T a)` > `String::toLowerCase`

인수가 2개: BinaryOperator 
`T apply(T a, T b)` > `BigInteger::add`

## Predicate 인터페이스
인수 하나를 받아 boolean 을 반환하는 함수
`boolean test(T t)` > `Collection::isEmpty` 

## Function 인터페이스
인수와 반환 타입이 다른 함수
`R apply(T t)` > `Arrays::asList`

## Supplier 인터페이스
인수를 받지 않고 값을 반환 하는 함수
`T get()` > `Instant::now`

## Consumer 인터페이스
인수를 하나 받고 반환값은 없는 함수
`void accept(T t)` > `System.out::println`


기본 인터페이스는 기본 타입인 int, long, double 용으로 각 3개씩 변형이 생겨난다. 그 이름도 기본 인터페이스의 이름 앞에 해당 기본 타입 이름을 붙여 지었다.  
`IntPredicate`, `LongBinaryPredicate`, ...

유일하게 Function의 변형만 매개변수화됐다. 정확히는 반환타입만 매개변수화 됐는데,  
`LongFunction<int[]>`는 long 인수를 받아 int[] 을 반환한다.

Function 인터페이스에는 기본 타입을 반환하는 변형이 총 9개가 더 있다.
인수와 같은 타입을 반환하는 함수는 UnaryOperator 이므로, Function 인터페이스의 변형은 입력과 결과의 타입이 항상 다르다.

입력과 결과 타입이 모두 기본 타입이면 접두어로 `SrcToResult`를 사용한다.  => `LongToIntFunction` (총 6개)   
나머지는 입력이 객체이고 결과가 int, long, double 인 변형들로 앞서와 달리 입력을 매개변수화 하고 접두어로 `ToResult`를 사용한다. => `ToLongFunction<int[]>` int 매개변수를 받아 long을 반환한다.


기본 함수형 인터페이스 중 3개에는 인수를 2개씩 받는 변형이 있다.
`BiPredicate<T, U>`, `BiFunction<T, R>`, `BiConsumer<T, U>` 다.

BiFunction 에는 다시 기본 타입을 반환하는 세 변형 
`ToIntBiFunctino<T, U>`, `ToLongBiFunction<T, U>`, `ToDoubleBiFunction<T, U>` 가 존재한다. 

Consumer에도 객체 참조와 기본 타입 하나, 즉 인수 두개를 받는 변형인 `ObjDoubleConsumer<T>`, `ObjIntConsumer<T>`, `ObjLongConsumer<T>` 가 존재한다.

마지막으로 BooleanSupplier 인터페이스는 boolean을 반환하도록 한 Supplier의 변형이다.


----
표준 함수형 인터페이스 대부분은 기본 타입만 지원한다. 그렇다고 `기본 함수형 인터페이스에 박싱된 기본 타입을 넣어 사용하지는 말자.` 동작은 하지만 계산량이 많을 때는 성능이 처참히 느려질 수 있다. (item61)

----

@FunctionalInterface 애너테이션을 사용하는 이유는 @Override 를 사용하는 이유와 비슷하다. 프로그래머의 의도를 명시하는 것으로, 크게 세 가지 목적이 있다.
1. 해당 클래스의 코드나 설명 문서를 읽을 이에게 그 인터페이스가 람다용으로 설계된 것임을 알려준다.
2. 해당 인터페이스가 추상 메서드를 오직 하나만 가지고 있어야 컴파일되게 해준다.
3. 그 결과 유지보수 과정에서 누군가 실수로 메서드를 추가하지 못하게 막아준다.

그러니 직접 만든 함수형 인터페이스에는 항상 @FunctionalInterface 애너테이션을 사용하라.

함수형 인터페이스를 API에서 사용할 때의 주의점은, 서로 다른 함수형 인터페이스를 같은 위치의 인수로 받는 메서드들을 다중 정의해서는 안 된다. 클라이언트에게 불필요한 모호함만 안겨줄 뿐이며, 이 모호함으로 인해 실제로 문제가 일어나기도 한다.
ExecutorService의 submit 메서드는 Callable<T>를 받는 것과 Runnable을 받는 것을 다중정의했다.  
그래서 올바른 메서드를 알려주기 위해 형변환해야 할 때가 생긴다. (item55)

이런 문제를 피하는 가장 쉬운 방법은 서로 다른 함수형 인터페이스를 같은 위치의 인수로 사용하는 다중정의를 피하는 것이다.


