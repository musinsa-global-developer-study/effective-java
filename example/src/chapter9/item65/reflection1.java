package chapter9.item65;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;

public class reflection1 {

    public static void main(String[] args) {
        Class<? extends Set<String>> cl = null;
        try {
            cl = (Class<? extends Set<String>>) Class.forName(args[0]); // 첫번째 인자로 클래스를 확정한다.
        } catch (ClassNotFoundException e) {
            // 클래스를 찾을 수 없습니다.
        }

        Constructor<? extends Set<String>> cons = null;
        try {
            cons = cl.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            // 매개변수 없는 생성자를 찾을 수 없습니다.
        }

        Set<String> s = null;
        try {
            s = cons.newInstance();
        } catch (IllegalAccessException e) {
            // 생성자에 접근할 수 없습니다.
        } catch (InstantiationException e) {
            // 클래스를 인스턴스화할 수 없습니다.
        } catch (InvocationTargetException e) {
            // 생성자가 예외를 던졌습니다.
        } catch (ClassCastException e) {
            // Set을 구현하지 않은 클래스입니다.
        }

        s.addAll(Arrays.asList(args).subList(1, args.length)); // 나머지 인자를 모두 입력한다.
        System.out.println(s);

        // 첫 번째 인수와 상관없이 두 번째 이후의 인수들은 중복을 제거한 후 출력한다 (Set)
        // 반면, 이 인수들이 출력되는 순서는 첫 번째 인수로 지정한 클래스가 무엇이냐에 따라 달라진다.
        // java.util.HashSet : 무작위
        // java.util.TreeSet : 알파벳 순

    }

}
