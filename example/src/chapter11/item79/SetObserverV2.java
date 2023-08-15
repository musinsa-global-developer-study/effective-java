package chapter11.item79;

@FunctionalInterface
public interface SetObserverV2<E> {

    // ObservableSet에 원소가 더해지면 호출된다
    void added(ObservableSetV2<E> set, E element);
}
