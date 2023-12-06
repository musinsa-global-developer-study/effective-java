# Item45 스트림은 주의해서 사용하라

스트림API 는 다량의 데이터 처리작업을 돕고자 추가되었다.

이 API가 제공하는 추상 개념 중 핵심은 두 가지다. 
1. 스트림은 데이터 원소의 유한 혹은 무한 `시퀀스`를 뜻한다.
2. 스트림 파이프라인은 이 원소들로 수행하는 `연산 단계`를 표현하는 개념이다.

---

스트림의 원소들은 어디로부터든 올 수 있다. 대표적으로 컬렉션, 배열, 파일, 정규표현식 패턴 매처, 난수 생성기, 다른 스트림이 있다.  
스트림 안의 데이터 원소들은 객체 참조나 기본 타입이다. (int, long, double)

---

스트림 파이프라인은 소스 스트림에서 시작해 종단 연산(terminal operation)으로 끝나며, 그 사이에 하나 이상의 중간 연산(intermediate operation)이 있을 수 있다.  
(각 원소에 함수를 적용하거나 특정 조건을 만족 못하는 원소를 걸러낼 수 있다.)

중간 연산들은 모두 한 스트림을 다른 스트림으로 변환하는데, 변환된 스트림의 원소 타입은 변환 전 스트림의 원소 타입과 같을 수도 있고 다를 수도 있다.

종단 연산은 마지막 중간 연산이 내놓은 스트림에 최후의 연산을 가한다. 원소를 정렬해 컬렉션에 담거나, 특정 원소 하나를 선택하거나, 모든 원소를 출력하는 식이다.

스트림 파이프라인은 `지연 평가(lazy evaluation)`된다. 평가는 종단 연산이 호출될 때 이뤄지며, 종단 연산에 쓰이지 않는 데이터 원소는 계산에 쓰이지 않는다. 이러한 지연 평가가 무한 스트림을 다룰 수 있게 해주는 열쇠이다.   
종단 연산이 없는 스트림은 아무 일도 하지 않는 명령어인 no-op와 같다.

스트림 API는 메서드 연쇄를 지원하는 `플루언트 API(fluent API)` 이다.

---
- Fluent API   
파이프라인 하나를 구성하는 모든 호출을 연결하여 단 하나의 표현식으로 완성할 수 있다.   
[구글 참고] 함수들을 작성하고 나면, 마치 그 문장이 영어 문장처럼 읽히는 API
---

기본적으로 스트림 파이프라인은 순차적으로 수행된다.    
파이프라인을 병렬로 실행하려면 파이프라인을 구성하는 스트림 중 하나에서 parallel 메서드를 호출해주기만 하면 되나, 효과를 볼 수 있는 상황은 많지 않다. (item 48)

스트림 API는 사실상 어떠한 계산이라도 해낼 수 있다.   
하지만 해낼 수 있다는 뜻이지, 해야 한다는 뜻은 아니다.

---
스트림을 제대로 사용하면 프로그램이 짧고 깔끔해지지만, 잘못 사용하면 읽기 어렵고 유지보수도 힘들어진다. 스트림을 언제 사용해야 하는지에 대한 규칙은 없지만, 참고할 만한 노하우는 있다.

아래 프로그램은 사전 파일에서 단어를 읽어 사용자가 지정한 문턱값보다 원소 수가 많은 anagram 그룹을 출력한다.
(* anagram: 철자를 구성하는 알파벳이 같고 순서만 다른 단어를 말한다)
```java
import java.io.IOException;
import java.util.HashMap;

public class Anagram {

    public static void main(String[] args) throws IOException {
        File directory = new File(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        Map<String, Set<String>> groups = new HashMap<>();
        try (Scanner s = new Scanner(dictionary)) {
            while (s.hasNext()) {
                String word = s.next();
                groups.computeIfAbsent(alphabetize(word), (unused) -> new TreeSet<>()).add(word); // 1번
            }
        }
        
        for (Set<String> group: groups.values()) {
            if (group.size() >= minGroupSize) {
                System.out.println(group.size() + ": " + group);
            }
        }
        
    }
    
    private static String alphabetize(String s) {
        char[] a = s.toCharArray();
        Arrays.sort(a);
        return new String(a);
    }

}

```

1번 표시를 보면, 맵에 각 단어를 삽입할 때 java8 에 추가된 computeIfAbsent 메서드를 사용했다.  
이 메서드는 맵 안에 키가 있는지 찾은 다음, 있으면 단순히 그 키에 매핑된 값을 반환한다.    
키가 없으면 건네진 함수 객체를 키에 적용하여 값을 계산해낸 다음 그 키와 값을 매핑해놓고, 계산된 값을 반환한다.


```java
import java.io.IOException;
import java.util.HashMap;

public class Anagram {

    public static void main(String[] args) throws IOException {
        Path directory = Path.get(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        try (Stream<String> words = Files.lines(dictionary)) {
            words.collect(
                groupingBy(word -> 
                    word.chars().sorted()
                        .collect(StringBuilder::new, (sb, c) -> sb.append((char) c), StringBuilder::append).toString())               
                )
                .values().stream()
                .filter(group -> group.size() >= minGroupSize)
                .map(group -> group.size() + ": " + group)
                .forEach(System.out::println);
        }
    }
}

```

이처럼 스트림을 과용하면 프로그램이 읽거나 유지보수 하기 어려워진다.


```java
import java.io.IOException;
import java.util.HashMap;

public class Anagram {

    public static void main(String[] args) throws IOException {
        Path directory = Path.get(args[0]);
        int minGroupSize = Integer.parseInt(args[1]);

        try (Stream<String> words = Files.lines(dictionary)) {
            words.collect(groupingBy(word -> alphabetize(word)))
                .values().stream()
                .filter(group -> group.size() >= minGroupSize)
                .forEach(g -> System.out.println(g.size() + ": " + g));
        }
    }
}

```
스트림 API 를 이렇게 적당히 사용하면 원래 코드보다 짧을 뿐 아니라 명확하기까지 하다.   
또한 람다에서 매개변수 이름을 잘 지어야 스트림 파이프라인의 가독성이 유지된다.  
alphabetize 같은 도우미 메서드를 적절히 활용하는 일의 중요성은 일반 반복 코드에서보다는 스트림 파이프라인에서 훨씬 크다.

alphabetize 메서드도 스트림을 사용해 다르게 구현할 수 있다. 그러나 그렇게 하면 명확성이 떨어지고 잘못 구현할 가능성이 커진다.
심지어 java가 기본 타입인 char용 스트림을 지원하지 않기 때문에 느려질 가능성도 있다. (불가능했다.)

```java
"Hello world!".chars().forEach(System.out::print);

```
위 코드는 Hello world! 가 아니라 721011081081113211911111410810033 을 출력한다.   
chars() 가 반환하는 스트림의 원소는 char가 아닌 int 이기 때문이다.  
이처럼 이름이 chars 인데 int 스트림을 반환하면 헷갈릴 수 있다. 올바른 print 메서드를 호출하게 하려면 다음처럼 형변환을 명시적으로 해야한다.

```java
"Hello world!".chars().forEach(x -> System.out.print((char) x));
```
하지만 `char 값들을 처리할 때는 스트림을 삼가는 편이 낫다.`

반복문을 스트림으로 바꿀 수 있더라도 가독성과 유지보수 측면에서 손해를 볼 수 있기 때문에, 기존 코드는 스트림을 사용하도록 리팩터링하되, 새 코드가 더 나아 보일 때만 반영하자.

----

스트림 파이프라인은 되풀이되는 계산을 함수 객체(주로 람다나 메서드 참조)로 표한한다. 반면 반복 코드에서는 코드 블록을 사용해 표현한다.   
그런데 함수 객체로는 할 수 없지만 코드 블록으로는 할 수 있는 일들이 있다.

1. 코드 블록에서는 범위 안의 지역번수를 읽고 수정할 수 있다. (람다에서는 final 이거나 사실상 final인 변수만 읽을 수 있고 지역 변수를 수정하는 건 불가)
2. 코드 블록에서는 return 문을 사용해 메서드에서 빠져나가거나, break나 continue 문으로 블록 바깥의 반복문을 종료하거나 반복을 한 번 건너뛸 수 있다.   
또한 메서드 선언에 명시된 검사 예외를 던질 수 있다. 하지만 람다로는 이 중 어떤 것도 할 수 없다.

계산 로직에서 이상의 일들을 수행해야 한다면 스트림과는 맞지 않는 것이다.   
반대로 다음과 같은 일들에는 스트림이 안성맞춤이다.

1. 원소들의 시퀀스를 일관되게 변환한다.
2. 원소들의 시퀀스를 필터링한다.
3. 원소들의 시퀀스를 하나의 연산을 사용해 결합한다.
4. 원소들의 시퀀스를 컬렉션에 모은다.
5. 원소들의 시퀀스에서 특정 조건을 만족하는 원소를 찾는다.

한편 스트림으로 처리하기 어려운 예도 있다.
1. 한 데이터가 파이프라인의 여러 단계를 통과할 때 이 데이터가 각 단계에서의 값을에 동시에 접근하기는 어려운 경우다.    
스트림 파이프라인은 일단 한 값을 다른 값에 매핑하고 나면 원래의 값은 잃는 구조이기 때문이다. 이런 방식은 코드 양도 많고 지저분하여 스트림을 쓰는 주목적에서 완전히 벗어난다.

```java
static Stream<BigInteger> primes() {
    return Stream.iterate(TWO, BigInteger::nextProbablePrime); // 스트림의 첫 번째 원소, 다음 원소를 생성해주는 함수. - 무한 스트림
}

public static void main(String[] args) {
    primes().map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
        .filter(mersenne -> mersenne.isProbablePrime(50)) // 50은 소수성 검사가 true를 반환할 확률을 제어한다.
        .limit(20)
        .forEach(System.out::println);
}

```
----
# 핵심 정리

수많은 작업들이 스트림과 반복을 조합했을 때 가장 멋지게 해결된다.   
스트림과 반복 중 어느 쪽이 더 나은지 모르겠다면 둘 다 해보고 더 나은 쪽을 택하라.

















