package chapter7.item48;

import java.math.BigInteger;
import java.util.stream.Stream;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

public class Item48Main {

    public static void main(String[] args) {
        primes().map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
            .filter(mersenne -> mersenne.isProbablePrime(50))
            .limit(20)
            .forEach(System.out::println);
    }

    static Stream<BigInteger> primes() {
        // 무한정 순환하는 스트림을 반환한다.
        return Stream.iterate(TWO, BigInteger::nextProbablePrime);
    }

}
