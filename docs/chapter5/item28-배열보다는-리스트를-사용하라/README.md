## 배열과 제네릭의 차이점


### 차이점1. 배열은 공변(covariant)이며, 제네릭은 불공변(invariant)이다.

- 공변은 함께 변한다는 뜻을 가지고 있다.
- Sub과 Super의 하위타입이라면, 배열 Sub[]는 배열 Super[]의 하위 타입이 된다. 정리하면 Object 타입의 배열을 만들면 그 하위 타입을 해당 배열에 넣을 수 있다는 뜻이다.
- 제네릭은 불공변이며, 함께 변하지 않음을 뜻한다.
- List은 List의 하위 타입도, 상위타입도 아니다. (C#은 4.0부터 제네릭 공변을 지원함)

아래 코드는 문법상 허용되므로, 컴파일에선 실패하지 않고 런타임에서 실패한다.

```other
Object[] objectsArray = new Long[1]; // Long type array of size 1
objectArray[0] = "타입이 달라 넣을 수 없다"; // This thorw ArrayStoreException
```


아래 코드는 문법상 허용되지 않으므로, 컴파일 단계에서 잡아낼 수 있다.

```other
List<Object> ol = new ArrayList<Long>(); // not compatible type
ol.add("타입이 달라 넣을 수 없다");
```


위 두 코드 어느쪽이든 Long 타입의 저장소에 String을 넣을 수 없다. 다만 배열에서는 그 실수를 런타임이 되어서야 알 수 있다. 반면 리스트는 컴파일 단계에서 잡아낼 수 있다.

### 차이점2. 배열은 실체화(reify)된다.


배열은 런타임에도 자신이 담기로 한 원소의 타입을 인지하고 확인한다. 반면 제네릭은 타입 정보가 런타임에는 소거(erasure)된다. 즉, 제네릭은 원소 타입을 컴파일 타임에만 검사하며 런타임에는 알 수조차 없다는 뜻이다.

소거는 자바5가 제네릭으로 전환될 수 있도록 하기 위한 조치이다.

## 제네릭과 배열의 호환성


앞서 살펴본 차이들로 제네릭과 배열은 서로 호환이 되질 않는다. 다음과 같이 `new List<E>[]` , `new List<String>[]` , `new E[]` 형태는 오류를 일으킨다.

아래 코드에서 (1)이 허용되는것을 가정하고 쭉 읽어보자.

```other
List<String>[] stringLists = new List<String>[1] // (1) List<String>을 담을 수 있는 사이즈 1짜리 배열
List<Integer> initList = List.of(42); // (2) 사이즈 1짜리 Integer type List
Objects[] objects = stringLists; // (3) assign (1) stringLists to Objects[]
objects[0] = initList; // (4) assign (2) initList to objects[0]
String s = stringLists[0].get(0); // (5) assign 42 to String s 
```


배열은 공변이기 떄문에 (3)의 동작은 문제없다. List<String> 배열을 Object 배열에 할당하는 동작임.

제네릭은 소거 방식으로 구현 되므로 (4) 동작도 문제없다. 런타임시에 List<Integer>는 List가 되고 List<Integer>[]은 List[] (List를 원소로 가지는 배열)이 된다. 즉 타입이 소거된 List를 List타입 배열의 원소로 할당한다. 따라서 ArrayStoreException이 발생하지 않음.

문제는 (5)이다. stringLists에는 List<Integer>의 인스턴스가 저장되어 있다. 여기서 0번째 원소를 꺼내려고하는데 컴파일러는 좌변 s가 String이므로 String 자동 형변환을 시도한다. 그러나 0번째 원소는 42라는 Integer타입이다. 따라서 ClassCastException이 발생한다.

이런일을 방지하려면 애초에 제네릭 배열이 생성되지 않도록 (1)단계에서 오류가 발생되어야한다.

### 실체화 불가 타입


E, List<E>, List<String> 같은 타입을 실체화 불가 타입(non-reifiable type)이라고 한다. 실체화가 되지 않기때문에 런타임시에 배열보다 한정적인 타입정보를 가질수 밖에 없다.(런타임시에 타입이 소거되어 있는 상태임)

그마나 실체화 될 수 있는 타입은 List<?>와 Map<?,?> 같은 비 한정적 와일드카드 뿐이지만, 사실 쓰일 일이 거의 없다.

### 배열을 제네릭으로 만들지 못해서 귀찮은 순간


제네릭 컬렉션에서는 자신의 원소 타입을 담은 배열을 반환하는게 불가능하다. (아이템33에서 해결법 제시함)

제네릭 타입과 가변인수 메서드를 함께 쓰면 해석하기 어려운 경고 메시지가 뜬다. 가변인수 메서드를 호출할때마다 가벼인수 매개변수를 담을 배열을 만들게 되는데 이때 제네릭 타입이라면 실체화 불가 타입이기떄문에 경고를 띄운다.

가변인수 메서드 설명

[https://www.scaler.com/topics/varargs-in-java/](https://www.scaler.com/topics/varargs-in-java/)

### 배열로 형변환 하는것 보다 컬렉션을 사용하자.


배열로 형변환할 때 제네릭 배열 생성 오류나 비검사 형변환 경오가 뜨는 경우 대부분은 배열인 E[] 대신 List<E>를 사용하면 해결된다. (코드가 복잡해지고, 성능이 살짝 나빠질 수 있지만 대신 타입 안전성과 상호 운용성은 좋아진다.)

어떤 목록에서 랜덤하게 객체를 선택해야 하는 경우

아래 코드는 정상적으로 컴파일이 되겠지만, choose()를 사용하는 클라이언트가 choose()의 결과인 Object를 계속 형변환 해줘야한다!!

```java
public static class ChooserV1 {

    private final Object[] choiceArray;

    public ChooserV1(Collection choices) {
        this.choiceArray = choices.toArray();
    }

    public Object choose() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }

}
```


제네릭 배열을 사용하도록 코드를 변경했다. 제네릭은 런타임시점에 원소 타입을 소거하기때문에 무슨타입의 배열인지 알 수 없다. 따라서 컴파일 단계에서 비검사 형변환 경고를 띄운다. T에 Object가 들어온다면 배열은 공변이므로 런타임시에 ClassCastException에 노출됨.

```java
public static class ChooserV2<T> {

    private final T[] choiceArray;

    public ChooserV2(Collection<T> choices) {
        
        //this.choiceArray = choices.toArray(); // 컴파일 자체가 되지 않는다.
        this.choiceArray = (T[]) choices.toArray(); 
    }

    public Object choose() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }
}
```


비검사 형변환 경고를 제거하기 위해 배열 대신 리스트를 사용해서 코드를 개선한다.

```java
public static class ChooserV3<T> {

    private final List<T> choiceList;

    public ChooserV3(Collection<T> choices) {

        this.choiceList = new ArrayList<>(choices);
    }

    public Object choose() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        return choiceList.get(rnd.nextInt(choiceList.size()));
    }
}
```

