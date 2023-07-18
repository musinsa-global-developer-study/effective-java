package chapter5.item28;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Item28Main {

    public static void main(String[] args) {

    }

    public static class ChooserV1 {

        private final Object[] choiceArray;

        public ChooserV1(Collection choices) {
            this.choiceArray = choices.toArray();
        }

        public Object choose() {
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            return choiceArray[rnd.nextInt(choiceArray.length)];
        }
    }

    public static class ChooserV2<T> {

        private final T[] choiceArray;

        public ChooserV2(Collection<T> choices) {

            //this.choiceArray = choices.toArray(); // 컴파일 자체가 되지 않는다.
            this.choiceArray = (T[]) choices.toArray();
        }

        public Object choose() {
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            return choiceArray[rnd.nextInt(choiceArray.length)];
        }
    }

    public static class ChooserV3<T> {

        private final List<T> choiceList;

        public ChooserV3(Collection<T> choices) {

            this.choiceList = new ArrayList<>(choices);
        }

        public Object choose() {
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            return choiceList.get(rnd.nextInt(choiceList.size()));
        }
    }

}
