package chapter11.item78;

import java.util.concurrent.TimeUnit;

/**
 * 이펙티브 자바 아이템 78의 예제 코드입니다. (공유 중인 가변 데이터는 동기화해 사용하라)
 */
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
