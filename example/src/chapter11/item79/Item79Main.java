package chapter11.item79;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Item79Main {

    public static void main(String[] args) {

        /**
         * CASE 1 : 옵저버 1개를 추가한다. 옵저버는 출력만 수행한다.
         */
        ObservableSet<Integer> set = new ObservableSet<>(new HashSet<>());

        set.addObserver((s,e) -> System.out.println(e));

        for (int i = 0; i < 100; i++) {
            set.add(i); // i가 추가 되었다. 알려라!
        }


        /**
         * CASE 2 : 옵저버 1개를 추가한다. 옵저버는 출력과 추가된 값이 23이면 자신을(옵저버) 제거한다!
         */
        ObservableSet<Integer> set2 = new ObservableSet<>(new HashSet<>());

        set2.addObserver(new SetObserver<Integer>() {
            @Override
            public void added(ObservableSet<Integer> s, Integer e) {
                System.out.println(e);
                if (e == 23) {
                    s.removeObserver(this);
                }
            }
        });

        for (int i = 0; i < 100; i++) {
            set2.add(i); // i가 추가 되었다. 알려라!
        }

        /**
         * CASE 3 : 옵저버 1개를 추가한다. 옵저버는 출력과 추가된 값이 23인 경우 관찰자 잠그기 시도를 한다.
         */
        ObservableSet<Integer> set3 = new ObservableSet<>(new HashSet<>());

        set3.addObserver(new SetObserver<Integer>() {
            @Override
            public void added(ObservableSet<Integer> s, Integer e) {
                System.out.println(e);
                if (e == 23) {
                    ExecutorService exec = Executors.newSingleThreadExecutor();
                    try {
                        exec.submit(() -> s.removeObserver(this)).get();
                    } catch (ExecutionException | InterruptedException ex) {
                        throw new AssertionError(ex);
                    } finally {
                        exec.shutdown();
                    }
                }
            }
        });

        for (int i = 0; i < 100; i++) {
            set3.add(i); // i가 추가 되었다. 알려라!
        }


    }

}
