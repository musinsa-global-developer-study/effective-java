package chapter5.item27;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Item27Main {

    public static void main(String[] args) {

        // 잘못된 코드
        //Set<Lark> exaltation = new HashSet();

        Set<Lark> exaltation1 = new HashSet<>();
        Set<Lark> exaltation2 = new HashSet<Lark>();

    }

    public static class Lark {}

    public <T> T[] toArray(T[] a) {

        int size = 1;
        String[] elements = {};

        if (a.length < size) {
            @SuppressWarnings("unchecked")
            T[] result = (T[]) Arrays.copyOf(elements, size, a.getClass());
            return result;
        }

        if (a.length > size) {
            a[size] = null;
        }
        return a;

    }



}
