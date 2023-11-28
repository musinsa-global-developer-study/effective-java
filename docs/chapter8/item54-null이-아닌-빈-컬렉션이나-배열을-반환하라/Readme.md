# null이 아닌, 빈 컬렉션이나 배열을 반환하라


## 빈 컬렉션을 null로 반환

컬렉션이 비어있는 경우 null을 반환하는 메서드는 없어야 한다. 

```java
// 컬렉션이 비었으면 null을 반환 - 따라 하지 말 것!
private final List<Cheese> cheesesInStock = ...;

public List<Cheese> getCheeses() {
    return cheesesInStock.isEmpty() ? null : new ArrayList<>(cheesesInStock);
}
```

null을 반환하면 클라이언트에서 추가로 코드를 작성해야 하기 때문이다. null을 반환하면 클라이언트에서 null 처리를 해야 하기 때문에 코드가 복잡해진다.

```java
List<Cheese> cheeses = shop.getCheeses();
if (cheeses != null && cheeses.contains(Cheese.STILTON)) {
    System.out.println("Jolly good, just the thing.");
}
```

사실 재고가 없다고 해서 특별이 취급할 이유도 없다!!

## null과 빈 컬렉션 할당 성능차이

빈 컨테이너를 할당하는 데도 비용이 드니 null을 반환하는 쪽이 낫다는 주장도 있다. 하지만 이는 두 가지 면에서 틀렸다.

첫번째, 이 정도 성능차이는 신경 쓸 수준이 되지 않는다. 빈 컬렉션을 새로 할당하는 데는 아주 작은 비용이 든다.

두번째는 빈 컬렉션과 배열은 굳이 새로 할당하지 않고도 반환할 수 있다.

```java
// 빈 컬렉션을 반환하는 올바른 예
public List<Cheese> getCheeses() {
    return new ArrayList<>(cheesesInStock);
}
```

물론, 사용패턴에 따라 빈 컬렉션 할당이 성능을 눈에 띄게 떨어뜨릴 수도 있다. 
해법은 매번 똑같은 빈 '불변' 컬렉션을 반환하는 것이다. `Collections.emptyList()`가 이런 역할을 한다. 

```java
// 최적화 - 빈 컬렉션을 매번 새로 할당하지 않도록 했다.
public List<Cheese> getCheeses() {
    return cheesesInStock.isEmpty() ? Collections.emptyList() : new ArrayList<>(cheesesInStock);
}
```

## 배열의 경우

배열을 쓸 때도 마찬가지다. 절대 null을 반환하지 말고 길이가 0인 배열을 반환하라.

```java
// 배열을 사용하는 올바른 예
public Cheese[] getCheeses() {
    return cheesesInStock.toArray(new Cheese[0]);
}
```

위 방식이 성능을 떨어뜨릴 것 같다면 길이 0짜리 배열을 미리 선언해두고 매번 그 배열을 반환하면 된다.

toArray 메서드를 사용해서 cheesesInStock List를 배열로 만들때 List가 비어있다면 새로 할당하지 않고 EMPTY_CHEESE_ARRAY를 그대로 사용한다.

```java
// 최적화 - 빈 배열을 매번 새로 할당하지 않도록 했다.
private static final Cheese[] EMPTY_CHEESE_ARRAY = new Cheese[0];

public Cheese[] getCheeses() {
    return cheesesInStock.toArray(EMPTY_CHEESE_ARRAY);
}
```

단순 성능 개선이 목적이라면 toArray에 넘기는 배열을 미리 할당하는건 추천하지 않는다.

```java
// 빈 배열을 매번 새로 할당하지 않도록 한다.
public Cheese[] getCheeses() {
    return cheesesInStock.toArray(new Cheese[cheesesInStock.size()]);
}
```