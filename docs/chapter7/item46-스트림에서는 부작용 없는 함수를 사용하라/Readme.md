# Item46 스트림에서는 부작용 없는 함수를 사용하라

스트림은 그저 또 다른 API 가 아닌, 함수형 프로그래밍에 기초한 패러다임이기 때문에 이해하기 어렵거나 장점이 무엇인지 쉽게 와 닿지 않을 수 있다.

스트림 패러다임의 핵심은 계산을 일련의 변환으로 재구성 하는 부분이다.  
이때 각 변환 단계는 가능한 한 이전 단계의 결과를 받아 처리하는 **순수 함수**여야 한다.   
**순수 함수**란 오직 입력만이 결과에 영향을 주는 함수를 말한다.    
다른 가변 상태를 참조하지 않고, 함수 스스로도 다른 상태를 변경하지 않는다. 

이렇게 하려면 스트림 연산에 건네는 함수 객체는 모두 부작용(side effect)이 없어야 한다.

```java
Map<String, Long> freq = new HashMap<>();
try (Stream<String> words = new Scanner(file).tokens()) {
    words.forEach(word -> {
        freq.merge(word.toLowerCase(), 1L, Long::sum);
    });    
}
```
스트림, 람다, 메서드 참조를 사용했고, 결과도 올바르다. 하지만 절대 스트림 코드라 할 수 없다.  
스트림 코드를 가장한 반복적 코드다.  스트림 API의 이점을 살리지 못하여 같은 기능의 반복적 코드보다 조금 더 길고, 읽기 어렵고, 유지보수에도 좋지 못하다.

forEach 에서 모든 작업이 일어나는데, 이때 외부 상태를 수정하는 람다를 실행하면서 문제가 생긴다.
이를 올바르게 작성하면 아래와 같이 된다.
```java
Map<String, Long> freq;
try (Stream<String> words = new Scanner(file).tokens()) {
    freq = words.collect(groupingBy(String::toLowerCase, counting()));    
}

```

forEach 연산은 종단 연산 중 기능이 가장 적고 가장 '덜'스트림답다.   
대놓고 반복적이라서 병렬화할 수도 없다.  
**forEach 연산은 스트림 계산 결과를 보고할 때만 사용하고, 계산하는 데는 쓰지 말자.**    
물론 가끔은 스트림 계산 결과를 기존 컬렉션에 추가하는 등의 다른 용도로도 쓸 수 있다.

----
java.util.stream.Collectors 클래스는 메서드를 39개 가지고 있고, 그중에는 타입 매개변수가 5개나 되는 것도 있다. (자바 10에서 4개가 늘어 43개)   
익숙해 지기 전까지는 Collector 인터페이스는 잠시 잊고, 그저 축소(reduction) 전략을 캡슐화한 블랙박스 객체라고 생각하기 바란다.   
**- 축소**: 스트림의 원소들을 객체 하나에 취합한다는 뜻이다. 

collector를 사용하면 스트림의 원소를 손쉽게 컬렉션으로 모을 수 있다.    
- toList()
- toSet()
- toCollection(collectionFactory)

```java
List<String> topTen = freq.keySet().stream()
    .sorted(comparing(freq::get).reversed())
    .limit(10)
    .collect(toList()); // Collectors의 멤버를 정적 임포트 하여 사용하면 스트림 파이프라인 가독성이 좋아진다.
```

나머지 39개의 메서드  
대부분은 스트림을 맵으로 취합하는 긴으으로, 진짜 컬렉션에 취합하는 것보다 훨씬 복잡하다.

## toMap
- toMap(keyMapper, valueMapper)
  - 스트림 원소를 키에 매핑하는 함수
  - 스트림 원소를 값에 매핑하는 함수
  - ```java
    private static final Map<String, Operation> stringToEnum = Stream.of(values())
        .collect(toMap(Object::toString, e -> e));
    ``` 
  - 이 형태는 스트림의 각 원소가 고유한 키에 매핑되어 있을 때 적합하다. 스트림 원소 다수가 같은 키를 사용한다면 파이프라인이 IllegalStateException을 던지며 종료될 것이다.
  - 더 복잡한 형태의 toMap이나 groupingBy 는 이런 충돌을 다루는 다양한 전략을 제공한다.
    - ```java
        Map<Artist, Album> topHits = albums
            .collect(toMap(Album::artist, a -> a, maxBy(comparing(Album::sales))));
      ```
- toMap(keyMapper, valueMapper, (oldVal, newVal) -> newVal)
  - 인수가 3개인 toMap은 충돌이 나면 마지막 값을 취하는 collector를 만들 때도 유용하다.
- toMap(keyMapper, valueMapper, (oldVal, newVal) -> newVal, mapFactory)
  - 원하는 특정 맵 구현체를 직접 지정할 수 있다.
- 위 세가지 toMap을 기반으로 변종이 있다. 그종 toConcurrentMap 은 병렬 실행된 후 결과로 ConcurrentHashMap 인스턴스를 생성한다.


## groupingBy
이 메서드는 입력으로 분류 함수(classifier)를 받고 출력으로는 원소들을 카테고리별로 모아 놓은 맵을 담은 Collector를 반환한다.    

```java
words.collect(groupingBy(word -> alphabetize(word)));
``` 

groupingBy가 반환하는 Collector 가 리스트 외의 값을 갖는 맵을 생성하게 하려면, 분류 함수와 함께 다운스트림 Collector도 명시해야한다.
다운스트림 collector 의 역할은 해당 카테고리의 모든 원소를 담은 스트림으로부터 값을 생성하는 일이다.

이 매개변수를 사용하는 가장 간단한 방법은 toSet() 을 넘기는 것이다. 그러면 groupingBy 는 원소들의 리스트가 아닌 Set을 값으로 갖는 Map을 만들어낸다.

toSet() 대신 toCollection(collectionFactory) 를 건네는 방법도 있다.    
이렇게 하면 리스트나 Set 대신 컬렉션을 값으로 갖는 맵을 생성한다. 

counting() 을 건네는 방법도 있다. 이렇게 하면 각 카테고리(key)를 해당 카테고리에 속하는 원소의 개수(value) 와 매핑한 맵을 얻는다.

```java
Map<String, Long> freq = words.collect(groupingBy(String::toLowerCase, counting()));
```

groupingBy의 세 번째 버전은 다운스트림 Collector에 더해 맵 팩터리(TreeSet, TreeMap)도 지정할 수 있게 해준다.   
참고로 이 메서드는 점층적 인수 목록 패턴(telescoping argument list pattern)에 어긋난다.    
즉, mapFactory 매개변수가 downStream 매개변수보다 앞에 놓인다.

또한 위 세가지 groupingBy 각각에 대응하는 groupingByConcurrent 메서드들도 있다. ConcurrenyHashMap 인스턴스를 만들어준다.

## partitionBy
분류 함수 자리에 Predicate 을 받고 Key 가 boolean 인 맵을 반환한다.
Predicate 에 더해 Downstream Collector 까지 입력받는 버전도 다중정의 되어있다.

## counting
downstream collector 전용 collector를 반환한다.
stream의 count 메서드를 직접 사용하여 같은 기능을 수행할 수 있으니 `collect(counting()) 형태로 사용할 일은 전혀 없다.`

Collections 에는 이런 속성의 메서드가 16개나 더 있다.    
그 중 9개는 summing, averaging, summarizing 으로시작하며 각각 int, long, double 용으로 하나씩 존재한다.

다중정의된 reducing 메서드들, filtering, mapping, flatMapping, collectingAndThen 메서드가 있는다, 대부분 프로그래머는 이들의 존재를 모르고 있어도 상관없다.

## maxBy, minBy, joining
이들은 Collectors에 정의되어 있지만 '수집'과는 관련이 없다.
- maxBy, minBy: 인수로 받은 비교자를 이용해 스트림에서 가장 큰/작은 원소를 찾아 반환한다.
- joining: 문자열 등의 CharSequance 인스턴스의 스트림에만 적용할 수 있다. 
  - 매개변수가 없는 joining: 단순히 원소들을 연결하는 collector 반환
  - 인수 1개 짜리 joining: CharSequence 타입의 구분문자(delimiter)를 매개변수로 받는다.
  - 인수 3개 짜리 joining: 구분문자에 더해 prefix, suffix 도 받는다.  
    - ex: [came, saw, conquered]'
  


