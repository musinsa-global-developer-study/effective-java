package chapter11.item79;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ObservableSet<E> extends ForwardingSet<E> {

    public ObservableSet(Set<E> s) {
        super(s);
    }

    private final List<SetObserver<E>> observers = new ArrayList<>();

    public List<SetObserver<E>> getObservers() {
        return observers;
    }

    public void addObserver(SetObserver<E> observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public boolean removeObserver(SetObserver<E> observer) {
        synchronized (observers) {
            return observers.remove(observer); // observers가 다른 동기화에 사용중이라면?!
        }
    }

    private void notifyElementAdded(E element) {
        synchronized (observers) {
            for (SetObserver<E> observer : observers) {
                observer.added(this, element); // added에서 무엇을 하는지 살펴봐라!
            }
        }
    }

    @Override
    public boolean add(E element) {
        boolean added = super.add(element); // 상위 Set에 추가
        if (added) {
            notifyElementAdded(element); // s에 값이 추가되었다는 것을 다른 옵저버들에게 알린다!!
        }
        return added;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean result = false;
        for (E element : c) {
            result |= add(element);
        }
        return result;
    }


}
