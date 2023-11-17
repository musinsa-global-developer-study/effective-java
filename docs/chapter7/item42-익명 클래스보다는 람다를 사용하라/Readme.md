# Item42 익명 클래스보다는 람다를 사용하라

예전에는 자바에서 함수 타입을 표현할 때 추상 메서드를 하나만 담은 인터페이스를 사용했다. 이런 인터페이스의 인스턴스를 함수객체라고 하여, 특정 함수나 동작을 나타내는 데 썼다.
1997년에 JDK 1.1 이 등장하면서 함수 객체를 만드는 주요 수단은 익명 클래스가 되었다.
```java
Collections.sort(words, new Comparator<String>() {
    public int compare(String s1, String s2) {
        return Integer.compare(s1.length(), s2.length());
    }
})
```

## 람다
java8 에 와서 추상 메서드 하나짜리 인터페이스는 특별한 의미를 인정받아 특별한 대우를 받게 되었다.
지금은 함수형 인터페이스라고 부르는 이 인터페이스들의 인스턴스를 `람다식(lambda expression, lambda)`을 통해 만들 수 있게 된 것이다. 

람다는 함수나 익명 클래스와 개념은 비슷하지만 코드는 훨씬 간결하다. 

```java
Colletions.sort(words, (s1, s2) -> Integer.compare(s1.length(), s2.length()));
```

여기서 s1, s2 의 타입은 문맥을 고려하여 컴파일러가 추론해준다.
상황에 따라 컴파일러가 타입을 결정하지 못할 수도 있는데, 그럴 떄는 프로그래머가 직접 명시해야 한다. `타입을 명시해야 코드가 더 명확할 때만 제외하고는, 람다의 모든 매개변수 타입은 생략하자.` 컴파일러가 타입을 알수 없다는 에러를 낼 때만 해당 타입을 명시하면 된다. 

람다 자리에 비교자 생성 메서드를 사용하면 이 코드를 더 간결하게 만들 수 있다.
```java
Collections.sort(words, comparingInt(String::length));
```

더 나아가 java 8에 List 인터페이스에 추가된 sort 메서드를 이용하면 더욱 짧아진다.
```java
words.sort(comparing(String::length));
```

```java
public enum Operation {
    PLUS("+") {
        public double apply(double x, double y) { return x + y; }
    },
    MINUS("-") {
        public double apply(double x, double y) { return x - y; }
    },
    TIMES("*") {
        public double apply(double x, double y) { return x * y; }
    },
    DIVIDE("/") {
        public double apply(double x, double y) { return x / y; }
    };

    private final String symbol;
    
    Operation(String symbol) { 
        this.symbol = symbol 
    }
    
    @Overrdie 
    public String toString() { return symbol; }

    public abstract double apply(double x, double y);

}

```

item34 에서는 상수별 클래스 몸체를 구현하는 방식보다 열거 타입에 인스턴스 필드를 두는 편이 낫다고 했다. 람다를 이용하면 후자의 방식, 즉 열거 타입의 인스턴스 필드를 이용하는 방식으로 상수별로 다르게 동작하는 코드를 쉽게 구현할 수 있다. 

```java
public enum Operation {
    PLUS ("+", (x, y) -> x + y),
    MINUS ("-", (x, y) -> x - y),
    TIMES ("*", (x, y) -> x * y),
    DIVIDE ("/", (x, y) -> x / y);

    private final String symbol;
    private final DoubleBinaryOperator op;

    Operation(String symbol, DoubleBinaryOperator op) {
        this.symbol = symbol;
        this.op = op;
    }

    @Overrdie 
    public String toString() { return symbol; }

    public double apply(double x, double y) {
        return op.applyAsDouble(x, y);
    }
}

```

DoubleBinaryOperator 는 java.util.function 패키지가 제공하는 다양한 함수 인터페이스 중 하나로, double 타입 인수 2개를 받아 double 타입 결과를 돌려준다.

람다 기반 열거 타입을 보면 상수별 클래스 몸체는 더 이상 사용할 이유가 없다고 느낄지 모르지만, 메서드나 클래스와 달리 람다는 이름이 없고 문서화도 못한다. 따라서 `코드 자체로 동작이 명확히 설명되지 않거나 코드 줄 수가 많아지면 람다를 쓰지 말아야 한다.`

람다는 한 줄일 떄 가장 좋고 길어야 세 줄 안에 끝내는 게 좋다. 세 줄이 넘어가면 가독성이 심하게 나빠진다. 람다가 길거나 읽기 어렵다면 더 간단히 줄여보거나 람다를 쓰지 않는 쪽으로 리팩터링 하길 바란다.

Enum 생성자에 넘겨지는 인수들의 타입도 컴파일 타임에 추론된다. 따라서 Enum 생성자 안의 람다는 Enum 인스턴스의 멤버에 접근할 수 없다. 따라서 인스턴스 필드나 메서드를 사용해야만 하는 상황이라면 상수별 클래스 몸체를 사용해야 한다.

람다는 함수형 인터페이스에서만 쓰인다. 추상 클래스의 인스턴스를 만들 때 람다를 쓸 수 없으니 익명 클래스를 써야한다. 비슷하게 추상 메서드가 여러 개인 인터페이스의 인스턴스를 만들 때도 익명 클래스를 쓸 수 있다. 

람다는 자기 자신을 참조할 수 없다. 람다에서 this 는 바깥 인스턴스를 가리킨다. 반면에 익명 클래스의 this 는 익명 클래스의 인스턴스 자신을 가리킨다. 그래서 함수 객체가 자신을 참조해야 한다면 반드시 익명 클래스를 써야 한다.

람다도 익명 클래스처럼 직렬화 형태가 구현별로 다를 수 있다. 따라서 람다를 직렬화 하는 일은 극히 삼가야 한다.(익명 클래스의 인스턴스도 마찬가지) 