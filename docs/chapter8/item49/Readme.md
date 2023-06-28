49. 매개변수가 유효한지 검사하라
- 메서드와 생성자 대부분은 매개변수의 값이 특정 조건을 만족하기를 바란다. 
    => 메서드에 들어올 때 이미 특정 조건을 만족하는 값만 들어오기를 바란다.
    => 이러한 제약은 메서드의 몸체(핵심로직)가 수행되기 전에 검사해야 한다.

- 검사하지 않았을 때의 문제점
    - 메서드가 수행되는 중간에 모호한 예외를 던지며 실패할 수 있다.
    - 메서드가 잘 수행되지만 잘못된 결과를 반환할 수 있다.
    - 메서드는 문제없이 수행됐지만, 어떤 객체를 이상한 상태로 만들어놓아서 미래의 알 수 없는 시점에 이 메서드와는 관계없는 오류를 낼 수 있다.
    => 검사에 실패하면 실패 원자성(item 76) 을 어기는 결과를 낳을 수 있다.

- public과 protected 메서드는 매개변수 값이 잘못됐을 때 던지는 예외를 문서화 해야한다
    - @throws 태그 사용
    - 매개변수의 제약을 문서화한다면 그 제약을 어겼을 때 발생하는 예외도 기술해야 한다. 
    
- 클래스 수준 주석은 각 메서드에 일일이 기술하는 것보다 클래스에 작성하여 훨씬 깔끔한 방법이다.
    - 클래스에 공통으로 적용되는 제약을 클래스 수준 주석으로 기술한다.
    - BigInteger 예제

- @Nullable 과 같은 Annotation으로 표시할 수도 있으나 표준적인 방법은 아니다.
    - java7의 java.util.Objects.requireNonNull 과 같은 메서드를 사용하자.
    ```java
        this.strategy = Objects.requireNonNull(strategy, "전략");
        if (obj == null) 
            throw new NullPointerException(message);
        return obj;
    ```
    - java9 에서는 범위 검사 기능도 더해졌다. 
        requireNonNull만큼 유연하지는 않고, 예외메세지를 지정할 수 없으며 리스트와 배열 전용으로 설계됐다. 
        닫힌 범위를 다루지는 못한다.
        - checkFromIndexSize(fromIndex, size, length)
            fromIndex – the lower-bound (inclusive) of the sub-interval
            size – the size of the sub-range 
            length – the upper-bound (exclusive) of the range
            
            - 검사하는 것
                fromIndex < 0
                size < 0
                fromIndex + size > length, taking into account integer overflow
                length < 0, which is implied from the former inequalities

        - checkFromToIndex(fromIndex, toIndex, length)
            - 검사하는 것
                fromIndex < 0
                fromIndex > toIndex
                toIndex > length
                length < 0, which is implied from the former inequalities

        - checkIndex(index, length)
            - 검사하는 것
                index < 0
                index >= length
                length < 0, which is implied from the former inequalities

        IndexOutOfBoundsException

- private 메서드는 개발자가 메서드가 호출되는 상황을 통제할 수 있으므로 유효한 값만이 메서드에 넘겨지리라는 것을 보증할 수 있다.
    - assert 를 사용해 매개변수 유효성을 검증할 수 있다.
    ```java
        private methd(long a[], int offset, int length) {
            assert a != null;
            assert offset >= 0 && offset <= a.length;
            assert length >= 0 && length <= a.length - offset;

        }
    ```
    - assert 문은 일반적은 유효성 검사와 다른다.
        - 실패하면 AssertionError를 던진다
        - 런타임에 아무런 효과도, 성증 저하도 없다. (--ea, --enableassertions 플래그로 실행시에는 제외)


- 예외는 있다.
    - 유효성 검사 비용이 지나치게 높거나 실용적이지 않을 때
    - 계산 과정에서 암묵적으로 검사가 수행될 때
    Collections.sort(List) 는 정렬 과정에서 상호 비교가 되어야 하므로 상호비교 될 수 없는 객체가 들어있는지 먼저 확인할 필요는 없다.

- 이 item을 '매개변수에 제약을 두는 게 좋다'고 해석하면 안된다.
  메서드는 최대한 범용적으로 설계되어야 한다. 메서드가 건네 받은 값으로 무언가 제대로 된 일을 할 수 있다면 
  매개변수 제약은 적을수록 좋다. 