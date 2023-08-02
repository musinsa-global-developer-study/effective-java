package chapter8.item55;

import java.util.List;
import java.util.OptionalInt;

public class Item55Main {

    public static void main(String[] args) {

        // OptionalInt가 사용되는 예시
        System.out.println(test(2).orElse(0));
    }

    public static OptionalInt test(Integer a) {

        if (a > 3) { // 3보다 크면 empty
            return OptionalInt.empty();
        }

        return OptionalInt.of(a);
    }

}
