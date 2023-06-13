package chapter2.item1;

import java.util.List;

/**
 * 생성자 대신 정적 팩터리 메서드를 고려하라
 */
public class Item1Main {

    public static void main(String[] args) {

        // 두번째 장점 - 호출될 때마다 인스턴스를 새로 생성하지는 않아도 된다.
        // from 메소드는 static이므로 메소드 호출시 별도의 Person 인스턴스를 만들 필요가 없다.
        // 불변 클래스에 대해서 미리 인스턴스를 만들어 두는 작업을 하지 않아도 된다. 필요할떄 만들면 됨!! - Boolean.valueOf를 참고 - 플라이웨이트 패턴 참고
        // Compile 시점에 만들어진 불변 인스턴스를 반환하는식?! - 계산기 예제 코드...
        Person person = Person.from("Joey", 30, "Seoul");

        // 세번째 - 반환 타입의 하위 타입 객체를 반환할 수 있는 능력이 있다.
        // List.of 메소드는 파라미터의 타임에 따라 반환되는 타입이 달라진다. List.of 메소드가 타입 제어권을 가진다.
        // 생성자만 사용했다면 현재 클래스의 인스턴스만 반환할것이다.
        List<String> stringList = List.of("Joey");
        List<Integer> integerList = List.of(1);

        // 네번째 - 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.
        // EnumSet.noneOf() 메소드를 보면 Set의 사이즈가 64개 이하일때와 초과일때 반환되는 인스턴스가 다르다

        // 다섯번째 - 정적 팩터리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.
        // 참고 - https://stackoverflow.com/questions/53240626/what-does-static-factories-returned-object-need-not-exist-mean
        // 인터페이스만 존재하고 아직 구현체가 작성되지 않았더라도 클라이언트는 인터페이스에 의존하고 있기떄문에 구현체가 아직 작성되어 있지 않아도 된다.

    }

}

