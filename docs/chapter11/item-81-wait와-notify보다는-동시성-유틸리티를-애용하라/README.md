# wait와 notify보다는 동시성 유틸리티를 애용하라

## wait과 notify

`wait 메소드`는 현재 publishTicket를 사용중인 쓰레드가 클래스의 lock을 반납하고, 해당 위치에서 대기하는 상태가 되도록 한다.

`notify 메소드`를 사용하면 대기상태에 있는 쓰레드중 하나만 깨운다.

자바 5부터 등장한 고수준의 동시성 유틸리티가 wait, notify를 통해 하드코딩했던 전형적인 일들을 대신하게 되었다. 

wait과 notify는 올바르게 사용하기가 아주 까다로우니 고수준 동시성 유틸리티를 사용하자!!

## java.util.concurrent 패키지

`java.util.concurrent`의 고수준 유틸리티는 세 범주로 나눌 수 있다.

바로 실행자 프레임워크(아이템 80), 동시성 컬렉션(concurrent collection), 동기화 장치(synchronizer)이다.

## 동시성 컬렉션

동시성 컬렉션은 List, Queue, Map 같은 표준 컬렉션 인터페이스에 동시성을 가미해 구현한 고성능 컬렉션이다.

높은 동시성에 도달하기 위해 동기화를 각자의 내부에서 수행한다.(아이팀 79)

따라서 동시성 컬렉션에서 동시성을 무력화 하는 건 불가능하며, 외부에서 락을 추가로 걸어버릴 경우 오히려 속도가 더 느려진다.
(이미 최적화 잘 되어있으니 그냥 쓰라는 뜻?)

## 상태 의존적 수정

앞서 말한대로 동시성 컬렉션은 2개 이상의 메소드를 원자적으로(하나의 트랜잭션)호출 할 수 없기 때문에 `상태 의존적 수정` 메소드들이 추가되었다.

예를 들면 Map의 `putIfAbsent(key, value)`같은 것들이다. 기존에 값이 있다면 그 값을 반환하고 없다면 null을 반환한다.

아래 코드는 String.intern의 동작을 흉내내어 구현한 샘플 코드이며, 최적화가 필요한 코드이다.

```java
public static final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

public static String intern(String s) {
    String previousValue = map.putIfAbsent(s, s);
    return previousValue == null ? s : previousValue; // 값이 없다면 현재 값, 값이 있다면 이전값 반환
}
```

좀더 성능 향상을 한 코드이다. 값이 없는 경우를 검사할태 `상태 의존적 수정`를 사용하지 않도록 했다.
(동기화를 최대한 하지 않는 방향으로 코드가 작성됨!!)

```java
public static String internV2(String s) {
    String result = map.get(s); // 먼저 하나 꺼내본다. 없으면 null
    if (result == null) {
        result = map.putIfAbsent(s, s); // s를 넣어보자. 기존에 값이 없다면 null return
        if(result == null) {
            result = s;
        }
    }
    return result;
}
```

위 코드는 (저자 PC 기준으로) String.intern 보다 6배나 빠르다. 단 String.intern은 메모리 누수를 방지하는 기술도 들어가 있음을 감안하자.

## 동기화한 컬렉션은 이제 버리자

ConcurrentHashMap의 등장으로 Collections.synchronizedMap을 사용할 필요가 없게 되었다.

동기화된 맵보다 동시성 맵이 훨씬더 성능이 좋다.

## BlockingQueue

컬렉션 인터페이스 중 일부는 작업이 성공적으로 완료될 때까지 기다리도록 확장되었다.

그 중에 Queue를 확장한 BlockingQueue에 추가된 take 메소드가 있다. (take는 큐의 첫 원소를 꺼내는데, 만약 큐가 비어었다면 새로운 원소가 추가될때까지 기다린다.)

이런 특성 덕분에 작업 큐(생산자-소비자 큐)로 쓰이기에 적합하다. ThreadPoolExecutor가 BlockingQueue를 사용한다.

## 동기화 장치

동기화 장치는 스레드가 다른 스레드를 기다릴 수 있게 하여, 서로 작업을 조율할 수 있게 해준다.

가장 자주 쓰이는 동기화 장치는 `CountDownLatch`와 `Semaphore`다. (CyclicBarrier, Exchange도 있는데 자주 쓰이진 않는다)

가장 강력한 동기화 장치로는 `Phaser`가 있다.

### CountDownLatch

`CountDownLatch`(latch; 걸쇠)는 일회성 장벽으로, 하나 이상의 스레드가 또 다른 하나 이상의 스레드 작업이 끝날 때까지 기다리게 한다.

생성자로 int 1개를 받으며, 이 값은 countDown 메서드를 몇번 호출해야 대기 중인 스레드들을 깨우는지를 결정한다.

멀티 쓰레드로 구성된 작업자들이 모두 준비가 완료되면 시작 방아쇠를 당겨 동시에 작업을 시작하고 그 시간을 측정하는 프로그램을 작성한 예시다.

여기선 오직 3개의 카운트다운 래치를 통해 구현했다. (wait과 notify로 짰다면...) 

단, 주의할 점이 있는데 executor가 생성할 수 있는 쓰레드의 개수가 concurrency만큼의 쓰레드를 생성할 수 있도록 설정되어야 한다.
그렇지 않으면 이 메서드는 영원히 끝나지 않는다. 이러한 상태를 `쓰레드 기아 교착상태(thread starvation deadlock)`이라 한다.

```java
public static long time(Executor executor, int concurrency, Runnable action) throws InterruptedException {

    CountDownLatch ready = new CountDownLatch(concurrency); // concurrency번 조회될때까지 멈춘다
    CountDownLatch start = new CountDownLatch(1); // 한번 호출되면 대기중인 쓰레드를 꺠운다
    CountDownLatch done = new CountDownLatch(concurrency); // concurrency번 조회될때까지 멈춘다

    for (int i = 0; i< concurrency; i++) {
        // executor에 총 concurrency개의 작업을 수행하도록 한다.
        executor.execute(() -> {
            
            ready.countDown(); // ready 래치를 1 증가시킨다
            try {
                start.await(); // start 래치가 실행될때까지 대기한다.
                action.run(); // 작업을 실행한다.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown(); // done 래치를 1 증가시킨다.
            }

        });

    }

    ready.await(); // ready 래치가 실행될때까지 대기한다.
    long startNanos = System.nanoTime();
    start.countDown(); // start 래치 카운트를 1 증가시킨다.
    done.await(); // done 래치가 끝날때까지 기다린다
    return System.nanoTime() - startNanos;

}
```

가능하면 시간을 잴 때는 `System.currentTimeMillis`가 아닌 `System.nanoTime`을 사용하자.
`System.nanoTime`은 더 정확하고 정밀하며 시스템의 실시간 시계의 시간 보정에 영향받지 않는다.

## wait과 notify를 다뤄야 할때

레거시 코드를 다뤄야 할때 어쩔 수 없이 wait과 notify를 만나게 될때도 있다.

이때는 가능하면 아래와 같이 표준 방식을 사용하자.

wait 메서드를 사용할 때는 반드시 대기 반복문(wait loop) 관용구를 사용해라. 반복문 밖에서는 절대로 호출하지 말자!
```java
synchronized (obj) {
    while(<조건이 충족되지 않았다>) { 
        obj.wait(); // 조건 충족이 안되면 계속해서 대기한다!
    }
    ... // 조건이 충족됐을 때의 동작을 수행한다.
    
}
```

대기 전에 조건을 검사하여 조건이 이미 충족되었다면 wait를 건너뛰게 한 것은 응답 불가 상태(Thread가 연산을 마무리 하지 못하는 현상)를 방지하기 위함이다.
만약에 조건이 충족되었는데 스레드가 notify(또는 notifyAll) 메서드를 먼저 호출한 후에 대기 상태로 빠지면 그 쓰레드를 다시 꺠울것이란 보장이 없다.
> wait보다 notify가 먼저 호출 되는 경우를 방어한다!

대기 후에 조건을 검사하여 조건이 충족되지 않았다면 다시 대기하게 하는 것은 안전 실패(잘못된 연산결과)를 막는 조치다. 
만약 조건이 충족되지 않았는데 스레드가 동작을 이어가면 락이 보호하는 불변식을 깨뜨릴 위험이 있다. 
> 첫번째 스레드가 계산이 끝나지 않았는데 두번째 쓰레드가 결과를 얻어 연산을 수행하는 경우를 방지한다


### 조건을 만족하지 않았더라도 쓰레드가 깨어나는 경우
1. 스레드가 notify를 호출한 다음 대기 중이던 스레드가 꺠어나는 사이에 다른 스레드가 락을 얻어 그 락이 보호하는 상태를 변경하는 경우 (새치기..)
2. 다른 스레드가 악의적으로 notify를 호출하는 경우. 공개된 객체를 락으로 사용할때 주로 발생한다.
3. 꺠우는 쓰레드가 너무 관대해서 notifyAll을 호출해 모든 스레드를 꺠우는 경우.
4. 대기 중인 스레드가 (드물게) notify 없이도 깨어나는 경우가 있다. 허위 각성(spurious wakeup)이라는 현상이다.

### notify와 notifyAll

일반적으로는 notifyAll을 사용하는게 안전하고 합리적이다. 깨어나야하는 쓰레드가 모두 꺠어남을 보정하니 항상 정확한 결과를 얻을 것이다.

깨어난 쓰레드들은 기다리던 조건이 충족되었는지 확인하여, 충독되지 않았다면 다시 대기하면 그만이다.

정말 최적화를 하고 싶다면 notify를 사용할 수도 있겠지만 notifyAll을 사용해야하는 이유가 하나더 있다.

외부로 공개된 객체인 경우라면 악의적으로 wait을 호출해서 스레드를 대기시킬 수 있다. 따라서 notifyAll을 통해 이런 공격을 방어할 수 있다.



