package chapter11.item78;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 이펙티브 자바 아이템 78의 예제 코드입니다. (공유 중인 가변 데이터는 동기화해 사용하라)
 */
public class StopThreadV4Main {

    //private static volatile int nextSerialNumber = 0;
    private static final AtomicLong nextSerialNumber = new AtomicLong();

    public static long generateSerialNumber() {
        return nextSerialNumber.getAndIncrement();
    }

    public static void main(String[] args) throws InterruptedException {

        generateSerialNumber();

    }

}
