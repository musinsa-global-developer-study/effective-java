package chapter11.item80;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Item80Main {

    public static void main(String[] args) {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);

        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    }

}
