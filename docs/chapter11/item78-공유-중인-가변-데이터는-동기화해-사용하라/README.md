# 공유 중인 가변 데이터는 동기화해 사용하라

synchronized 키워드는 해당 메서드나 블록을 한번에 한 스레드씩 수행하도록 보장한다.

## 동기화의 2가지 기능
일반적으로 동기화는 한 스레드가 변경하는 중이라서 상태가 일관되지 않은 순간의 객체를 다른 스레드가 보지 못하게 막는 용도로만 생각한다.

사실 우리가 잘 인지 하지 못하는 기능이 하나더 있다. 

동기화를 사용하면 한 스레드가 만든 변화를 확인할 수 있도록 해준다. 
A라는 쓰레드가 동기화 적용된 메소드에 들어가서 공유된 값을 Read 한다면 우리는 이 값이 이전의 어떤 쓰레드에 의해 변경된 최종 값임을 확신할 수 있다.

## Atomic한 변수
자바의 언어 명세상 long과 double 외의 변수를 읽고 쓰는 동작은 모두 원자적(Atomic)이다.
- long과 double은 64-bit 데이터 타입이라 32-bit씩 데이터를 처리하는 JVM에서 원자적이지 못하다.(JLS 17.7 참고)

이 뜻은 공유되고 있는 변수의 어떤 값은 이전 쓰레드의 최종 결과값임을 보장한다. 

이 말을 듣고 성능을 위해서 원자적 변수를 읽고 쓸때 동기화 하지 않는 것은 위험한 발상이다.

앞서 말했듯이 자바 언어 명세는 스레드가 변수를 읽을 때 항상 `수정이 완전히 반영된 값`을 보장한다.
문제는 한 스레드가 변경한 값이 다른 스레드에게 `보이는가`는 보장하지 않는다.

따라서 우리는 동기화를 배타적인 실행을 위해서 뿐만아니라 스레드 사이의 안정적인 통신(`A 쓰레드가 변경한 값이 B 쓰레드에 보이는가`)에 꼭 필요하다

이는 한 스레드가 만든 변화가 다른 스레드에게 언제 어떻게 보이는지를 규정한 자바의 메모리 모델 떄문이다.
- 자세한 내용은 https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html 를 참고하자.

## Thread.stop은 사용하지 말자!

앞서 우리는 공유되고 있는 변수에 대해서 동기화가 필요함을 확인했다. 동기화 이전에는 다른 쓰레드를 멈추기 위해서 Thread.stop()을 사용했다.

그러나 Thread.stop()은 안전히자 않아 올바른 방법이 아니므로 사용하지 말자.

## 쓰레드간 통신 실패 예제

메인 쓰레드가 1초 sleep후 stopRequest의 값을 변경한다. 이 코드의 결과는 어떨까??

메인 쓰레드와 백그라운드 쓰레드가 stopRequested 변수에 대한 통신이 올바르게 되지 않아 프로그램이 loop에 빠진다. 
```java
public class StopThreadMain {

    private static boolean stopRequested;

    public static void main(String[] args) throws InterruptedException {

        Thread backgroundThread = new Thread(() -> {
           int i = 0;
           while (!stopRequested) { // 메인쓰레드가 수정한 값을 백그래안두 쓰레드가 언제 보게될지 알수 없음.
               i++;
           }

        });
        backgroundThread.start(); // 백그라운드 쓰레드 시작

        TimeUnit.SECONDS.sleep(1); // 메인 쓰레드 1초 sleep
        stopRequested = true; // 메인 쓰레드가 변경
    }
}
```

## synchronized를 통한 동기화

쓰기 메소드와 읽기 메소드를 모두 동기화 했음에 주목하자. 
쓰기와 읽기 모두를 동기화 하지 않으면 동기화 동작을 보장하지 않는다.

사실 아래 코드는 단순해서 동기화를 하지 않아도 원자적으로 동작한다.

다만 쓰레드간 통신 실패에 대한 개선을 위해 synchronized를 사용한 것이다.

```java
public class StopThreadV2Main {

    private static boolean stopRequested;

    private static synchronized void requestStop() {
        stopRequested = true;
    }

    private static synchronized boolean stopRequested() {
        return stopRequested;
    }


    public static void main(String[] args) throws InterruptedException {

        Thread backgroundThread = new Thread(() -> {
           int i = 0;
           while (!stopRequested()) {
               i++;
           }
           
        });
        backgroundThread.start(); // 백그라운드 쓰레드 시작

        TimeUnit.SECONDS.sleep(1); // 메인 쓰레드 1초 sleep
        requestStop();

    }

}
```

## volatile를 사용한 동기화?!
더 빠른 속도를 위한 대안으로 volatile을 사용할 수 있다.

volatile은 배타적 수행과는 상관 없고, 항상 가장 최근에 기록된 값을 읽게 됨을 보장한다.

```java
public class StopThreadV3Main {

    private static volatile boolean stopRequested;

    public static void main(String[] args) throws InterruptedException {

        Thread backgroundThread = new Thread(() -> {
            int i = 0;
            while (!stopRequested) { // 메인쓰레드가 수정한 값을 백그래안두 쓰레드가 언제 보게될지 알수 없음.
                i++;
            }

        });
        backgroundThread.start(); // 백그라운드 쓰레드 시작

        TimeUnit.SECONDS.sleep(1); // 메인 쓰레드 1초 sleep
        stopRequested = true; // 메인 쓰레드가 변경
    }

}
```

volatile은 주의해서 사용해야할 필요가 있다. 아래 일련번호 생성 코드를 보자.

아래 메서드는 호출될때 마다 매번 고유한 값을 반환할 목적으로 만들어 졌다.

얼핏보면 원자적으로 접근할 수 있는것 처럼 보이는데 문제는 `++` 연산이다.

이를 풀어서 쓰면 `nextSerialNumber = nextSerialNumber + 1`이다. 즉, nextSerialNumber 변수에 2번 접근한다.

만약 A 쓰레드가 nextSerialNumber의 첫번째 접근을 하고 있는 와중에 
B 쓰레드가 비집고 들어와서 연산을 수행해버리면 A쓰레드와 같은 값을 읽수 있는 케이스가 생긴다. 이런 현상을 `safety failure`라고 부른다.

```java
private static volatile int nextSerialNumber = 0;
    
public static int generateSerialNumber() {
    return nextSerialNumber++;
}
```

문제 해결을 위해선 `generateSerialNumber()` 메소드에 `synchronized`를 붙이면 된다.

## AtomicLong 사용해서 개선하기

`java.util.concurrent.atomic` 패키지의 AtomicLong을 사용한 코드다.

이 패키지에는 락 없이도(lock-free) 스레드 안전한 프로그래밍을 지원하는 클래스들이 있다.

volatile은 동기화의 두 기능중에 쓰레드간 통신에 대한 기능만 지원하지만, 이 패키지는 원자성(배타적 실행)까지 보장한다.

````java
private static final AtomicLong nextSerialNumber = new AtomicLong();

public static long generateSerialNumber() {
    return nextSerialNumber.getAndIncrement();
}
````

사실 앞에 언급한 문제를 해결하는 가장 쉬운 방법은 애초에 가변 데이터를 쓰레드간 공유하지 않는 것이다.

가능하면 불변 데이터만 공유하거나 아무것도 공유하지 말자. 즉, 가변 데이터는 단일 스레드에서만 쓰도록 하자.