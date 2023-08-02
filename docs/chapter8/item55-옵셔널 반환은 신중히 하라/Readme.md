# 옵셔널 반환은 신중히 하라

### 자바 8 이전
자바 8 이전에는 메서드가 특정조건에서 반환값이 없을 때 취할 수 있는 선택지가 두 가지 있었다.
1. 예외를 던진다.
2. null을 반환한다.

위 두 방법에는 허점이 있는데, 
예외는 진짜 예외적인 상황에서만 사용해야 하며(item69), 예외를 생성할 때 스택 추적 전체를 캡처하므로 비용이 크다.
null을 반환하면 null을 반환하는 메서드를 호출할 때는 별도의 null 처리 코드를 추가해야 한다. 그렇지 않으면 null을 반환한 메서드와는 전혀 관계없는 코드에서 NullPointerException이 발생할 수 있다.

### Optional\<T\>
Optional\<T\>는 null이 아닌 T 타입 참조를 하나 담거나, 혹은 아무것도 담지 않을 수 있다. 이를 비었다(empty)고 한다.
Optional은 원소를 최대 1개 가질 수 있는 '불변' 컬렉션이다.

### Optional 반환
```java
return Optional.empty();
return Optional.of(result); // null 을 넣으면 NPE 발생
return Optional.ofNullable(result);
```
옵셔널을 반환하는 메서드에서는 절대 null을 반환하지 말자. 옵셔널의 도입 취지를 완전히 무시하는 행위이다.


### Optional은 검사 예외와 취지가 비슷하다 (item71)
반환 값이 없을 수도 있음을 API 사용자에게 명확하게 알려준다. 비검사 예외를 던지거나 null을 반환한다면 API 사용자가 그 사실을 인지하지 못할 수 있다. 하지만 검사예외를 던지면 클라이언트에서 반드시 이에 대처하는 코드를 작성해야 한다.
비슷하게, 메서드가 옵셔널을 반환한다면 클라이언트는 값을 받지 못했을 때 취할 행동을 선택해야 한다.
1. 기본값을 설정한다.
   ```java
   String lastWordInLexicon = max(words).orElse("단어 없음");
   ```
2. 상황에 맞는 예외를 던진다.
   ```java
   Toy myToy = max(toys).orElseThrow(TemperTantrumException::new);
   // 실제 예외가 아니라 팩터리를 전달했기 때문에, 예외가 실제로 발생하지 않는 한 예외 생성 비용은 들지 않는다.
   ```
3. 값이 채워져있다고 판단된다면 바로 꺼내 사용한다.
   ```java
   Element e = max(Element.NOBLE_GASES).get(); // NoSuchElementException 발생 가능
   ```
4. 이따금 기본값을 설정하는 비용이 아주 커서 부담이 될 때는 Supplier\<T\>를 인수로 받는 orElseGet을 사용하면, 값이 처음 필요할 때 Suuplier\<T\>를 이용하여 생성하므로 초기 설정 비용을 낮출 수 있다.
5. 추가적으로 filter, map, flatMap, ifPresent, isPresent 를 사용할 수 있다.
  isPresent 는 상당수 위의 메서드들로 대체할 수 있다.
  ```java
  Optional<ProcessHandle> parentProcess = ph.parent();
  log.debug("부모 PID: {}", parentProcess.isPresent() ? parentProcess.get().pid() : "N/A");

  ----------------------------------

  log.debug("부모 PID: {}", ph.parent().map(h -> h.pid()).orElse("N/A"));
  
  ```

### Stream과 함께 사용
스트림을 사용한다면 옵셔널들을 Stream<Optional\<T>>로 받아서, 그 중 채워진 값을 뽑아 Stream\<T>에 건네 담아 처리하는 경우가 많다.
```java
streamOfOptionals
    .filter(Optional::isPresent)
    .map(Optional::get)
```

java 9 에서는 Optional에 stream() 메서드가 추가되었다. 이 메서드는 Optional을 stream 으로 변환해주는 어댑터이다. 옵셔널에 값이 있으면 그 값을 원소로 담은 스트림으로, 값이 없다면 빈 스트림으로 변환한다. 이를 Stream 의 flatMap 메서드와 조합하면 앞의 코드가 아래처럼 된다.
```java
streamOfOptionals
    .flatMap(Optional::stream)
```

### 무조건 득이 되진 않는다.
반환값으로 옵셔널을 사용하는 것이 무조건 득이 되는 것은 아니다. ``컬렉션, 스트림, 배열, 옵셔널`` 같은 컨테이너 타입은 옵셔널로 감싸면 안된다.
Optional<List\<T>>를 반환하는 것 보다는 빈 List\<T> 를 반환하는 게 좋다. (item54) 

빈 컨테이너를 그대로 반환하면 클라이언트에 옵셔널 처리 코드를 넣지 않아도 된다. 
(ProcessHandle.Info 인터페이스의 arguments 메서드는 Optional<String[]> 을 반환하는데 예외적인 경우다.)

### Optional\<T> 를 반환해야 하는 경우
결과가 없을 수 있으며, 클라이언트가 이 상황을 특별하게 처리해야 한다면 Optional\<T> 를 반환하자.

### Optional 의 비용
Optional도 새로 할당하고 초기화 해야하는 객체이고, 그 안에 값을 꺼내려면 메서드를 호출해야 하니 비용이 들 수 밖에 없다. 그래서 성능이 중요한 상황에서는 적절하지 않을 수 있다.
어떤 메서드가 이 사왛ㅇ에 적합한지 알아내려면 세심히 측정해볼 수 밖에 없다. (item67)

### OptionalInt, OptionalLong, OptionalDouble
박싱된 기본 타입을 담는 옵셔널은 기본 타입 자체보다 무거울 수밖에 없다. 
그래서 자바 API 설계자는 int, long, double 전용 옵셔널 클래스들을 준비해놨다.
위 세 옵셔널들은 Optional\<T>가 제공하는 메서드를 거의 다 제공한다.

### 사용하면 안되는 경우
옵셔널을 맵의 값으로 사용하면 안된다.
맵 안에 키가 없다는 사실을 나타내는 방법이 두 가지가 된다. (키 자체가 없는 경우, 키는 있지만 키가 속이 빈 옵셔널인 경우) - 복잡성이 높아진다.

-> 옵셔널을 컬렉션의 키, 값, 원소나 배열로 사용하는 게 적절한 상황은 거의 없다.