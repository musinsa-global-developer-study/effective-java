package chapter5.item26;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Item26Main {

    public static void main(String[] args) {

        List<String> strings = new ArrayList<>();
        unsafeAdd(strings, Integer.valueOf(42));
        // Runtime시 에러 발생.
        String s = strings.get(0); // Integer to String ClassCaseException

        // 아래 코드는 compile 단계에서 찾아낼 수 있다!

        //safeAdd(strings, Integer.valueOf(42));
        //String s = strings.get(0);


    }

    // List의 제네릭 타입을 명시하지 않는다.(=Raw Type을 사용한다)
    private static void unsafeAdd(List list, Object o) {
        list.add(o);
    }

    // List의 제네릭 타입을 명시한다
    private static void safeAdd(List<Object> list, Object o) {
        list.add(o);
    }

    // 잘못된 예제.
    static int numElementsInCommon(Set s1, Set s2) {
        int result = 0;
        for (Object o1 : s1) {
            if (s2.contains(o1)) {
                result ++;
            }
        }
        return result;
    }

    static int numElementsInCommonWithGeneric(Set<?> s1, Set<?> s2) {
        int result = 0;
        for (Object o1 : s1) {
            if (s2.contains(o1)) {
                result ++;
            }
        }
        return result;
    }

}
