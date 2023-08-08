package chapter11.item78;

import java.util.concurrent.TimeUnit;

/**
 * 이펙티브 자바 아이템 78의 예제 코드입니다. (공유 중인 가변 데이터는 동기화해 사용하라)
 */
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
