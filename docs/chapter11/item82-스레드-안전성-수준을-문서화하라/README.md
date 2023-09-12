# 스레드 안전성 수준을 문서화하라

한 메서드를 여러 스레드가 동시에 호출할 때 그 메서드가 어떻게 동작하느냐는 해당 클래스와 이를 사용하는 클라이언트 사이의 중요한 계약과 같다.

**API 문서에서 아무런 언급도 없으면 그 클래스 사용자는 나름의 가정을 해야만 한다.**

## synchronized 한정자가 보이는 메서드는 안전할까?

이 질문은 몇가지 면에서 틀렸다.

자바독이 기본 옵션에서 생성한 API 문서에는 synchronized 한정자가 포함되지 않는다!

메서드 선언에 synchronized 한정자를 선언할지는 구현의 이슈일 뿐 API에 속하지 않기 때문이다.

## 스레드 안전성 수준

멀티스레드 환경에서도 API를 안전하게 사용하게 하려면 클래스가 지원하는 스레드 안전성 수준을 정확히 명시해야 한다.

### 불변(immutable)
이 클래스의 인스턴스는 마치 상수와 같은 수준이다. 즉, 외부 동기화 없이 사용하면 된다. String, Long, BigInteger가 대표적이다.

### 무조건적 스레드 안전(unconditionally thread-safe)
이 클래스의 인스턴스는 수정될 수 있으나, 내부에서 충실히 동기화 했기때문에 외부에서 동기화 없이 동시에 사용해도 안전하다.
AtomicLong, ConcurrentHashMap이 여기에 속한다.

### 조건부 스레드 안전(conditionally thread-safe)
무조건적 스레드 안전과 같으나, 일부 메서드는 동시에 사용하려면 외부 동기화가 필요하다.
`Collections.synchronized` 래퍼 메서드가 반환한 컬렉션들이 여기에 속한다.

### 스레드 적대적(thread-hostile)
이 클래스는 모든 메서드 호출을 외부 동기화로 감싸더라도 멀티스레드에서 안전하지 않다. 이 수준의 클래스는 일반적으로 정적 데이터를 아무 동기화 없이 수정 한다.

이런 클래스를 고의로 만들진 않겠지만 동시성을 고려하지 않고 작성하다 보면 우연히 만들어 질 수 있다.

스레드 적재적으로 밝혀진 클래스나 메서드는 **문제를 고쳐 재배포하거나, 사용 자제(deprecated) API로 지정하자**

(아이템 78의 generateSerialNumber 메소드에서 내부 동기화를 생략하면 스레드 적대적이게 된다)

## 어노테이션으로 안전성 수준 나타내기

위에서 설명한 분류는 스레드 적대적만 빼면 스레드 안정성 어노테이션과 대략 일치한다.

어노테이션에는 `@Immutable`,`@ThreadSafe`, `@NotThreadSafe`가 있다. 

무조건적 스레드 안전과 조건부 스레드 안전은 `@ThreadSafe`에 속한다. 

## 조건부 스레드 안전 클래스는 주의해서 문서화 하자

어떤 조건이나 순서로 호출할 때 외부 동기화가 필요한지, 그 순서로 호출하려면 어떤 락 혹은 (드물게) 락들을 얻어야 하는지 알려줘야한다.

`Collections.synchronizedMap`의 API문서이다.

```java
/**
 * Returns a synchronized (thread-safe) map backed by the specified
 * map.  In order to guarantee serial access, it is critical that
 * <strong>all</strong> access to the backing map is accomplished
 * through the returned map.<p>
 *
 * It is imperative that the user manually synchronize on the returned
 * map when traversing any of its collection views via {@link Iterator},
 * {@link Spliterator} or {@link Stream}:
 * <pre>
 *  Map m = Collections.synchronizedMap(new HashMap());
 *      ...
 *  Set s = m.keySet();  // Needn't be in synchronized block
 *      ...
 *  synchronized (m) {  // Synchronizing on m, not s!
 *      Iterator i = s.iterator(); // Must be in synchronized block
 *      while (i.hasNext())
 *          foo(i.next());
 *  }
 * </pre>
 * Failure to follow this advice may result in non-deterministic behavior.
 *
 * <p>The returned map will be serializable if the specified map is
 * serializable.
 *
 * @param <K> the class of the map keys
 * @param <V> the class of the map values
 * @param  m the map to be "wrapped" in a synchronized map.
 * @return a synchronized view of the specified map.
 */
```

클래스의 스레드 안전성은 보통 클래스의 문서화 주석에 기재하지만 독특한 특성의 메서드라면 해당 메서드의 주석에 기재하자

열거 타입(enum)은 굳이 불변이라고 쓰지 않아도 된다.

반환 타입만으로는 명확히 알 수 없는 정적 팩터리라면 자신이 반환하는 객체의 스레드 안전성을 반드시 문서화해야한다. 
`Collections.synchronizedMap`가 좋은 예이다.

## 외부에서 사용할 수 있는 락을 제공한다면?

클래스가 외부에서 사용할 수 있는 락을 제공하면, 클라이언트에서 일련의 메서드 호출을 원자적으로 수행할 수 있게 된다. (클래스의 소유자가 누군지 확인가능)

이 유연성에는 대가가 따르는데... 
- 내부에서 처리하는 고성능 동시성 제어 메커니즘(ex.`ConcurrentHashMap`)과 혼용할 수 없게 된다.
- 클라이언트가 공개된 락을 오래 쥐고 놓지 않는 서비스 거부 공격을 수행할 수도 있다.

이를 방지하기 위해서 synchronized 메서드(사실상 공개된 락과 같음) 대신 비공개 락 객체를 사용해야한다.

```java
private final Object lock = new Object();

public void foo() {
    synchronized(lock) {
        ...
    }    
}

```

비공개 락 객체는 클래스 바깥에서 볼 수 없으니 클라이언트가 그 객체의 동기화에 관여할 수 없다.

비공개 락 객체 관용구는 `무조건적 스레드 안전` 클래스에서만 사용하자. 
특정 호출 순서에 필요한 락이 무엇인지를 클라이언트에게 알려줘야 하는 `조건부 스레드 안전`에서는 사용할 수 없다.



