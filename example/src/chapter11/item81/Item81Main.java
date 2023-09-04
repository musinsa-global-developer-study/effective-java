package chapter11.item81;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Item81Main {

    public static void main(String[] args) {

    }

    public static final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

    public static String intern(String s) {
        String previousValue = map.putIfAbsent(s, s);
        return previousValue == null ? s : previousValue; // 값이 없다면 현재 값, 값이 있다면 이전값 반환
    }


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
                    // 타이머에게 작업을 마쳤음을 알린다
                    done.countDown();
                }

            });

        }

        ready.await(); // ready 래치가 실행될때까지 대기한다.
        long startNanos = System.nanoTime();
        start.countDown(); // start 래치 카운트를 1 증가시킨다.
        done.await(); // done 래치가 끝날때까지 기다린다
        return System.nanoTime() - startNanos;

    }



}
