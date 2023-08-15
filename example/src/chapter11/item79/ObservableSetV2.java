package chapter11.item79;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObservableSetV2<E> extends ForwardingSet<E> {

    public ObservableSetV2(Set<E> s) {
        super(s);
    }

    private final List<SetObserverV2<E>> observers = new CopyOnWriteArrayList<>();

    public List<SetObserverV2<E>> getObservers() {
        return observers;
    }

    public void addObserver(SetObserverV2<E> observer) {
        observers.add(observer);
    }

    public boolean removeObserver(SetObserverV2<E> observer) {
        return observers.remove(observer);
    }

    private void notifyElementAdded(E element) {

        for (SetObserverV2<E> observer : observers) {
            observer.added(this, element); // added에서 무엇을 하는지 살펴봐라!
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
