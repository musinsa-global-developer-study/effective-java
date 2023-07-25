# 표준 예외를 사용하라

## 표준예외를 사용하자.

표준 예외를 재사용하면 다음과 같은 장점이 있다.
- 여러분의 API가 다른 사람이 익히고 사용하기 쉬워진다.
    - 표준 예외이므로 예외가 어떤 것을 의미하는지 쉽게 이해할 수 있어, 표준 예외를 사용하는 API는 익히기 쉽다.
- 예외 클래스 수가 적을수록 메모리 사용량도 줄고 클래스를 적재하는 시간도 적게 걸린다.

## 대표적인 표준 예외 종류

대표적으로 자주 사용하는 표준 예외는 다음과 같다.

|예외|주요 쓰임|
|---|---|
|IllegalArgumentException|허용하지 않는 값이 인수로 건네졌을 때(null은 따로 NullPointerException으로 처리)|
|IllegalStateException|객체가 메서드를 수행하기에 적절하지 않은 상태일 때|
|NullPointerException|null을 허용하지 않는 메서드에 null을 건냈을 때|
|IndexOutOfBoundException|인덱스가 범위를 넘어섰을 때|
|ConcurrentModificationException|허용하지 않는 동사 수정이 발견됐을 때|
|UnsupportedOperationException|호출한 메서드를 지원하지 않을 때|

앞서의 표로 정리한 `주요 쓰임`이 상호 배타적이지 않은 탓에, 종종 표준 예외를 선택하기가 어려울 때도 있다.<br/>
그 중에 `IllegalArgumentException`과 `IllegalStateException` 간의 선택하는 규칙은 다음과 같다. 인수 값이 무엇이었든 어차피 실패했을 거라면 `IllegalArgumentException`, 그렇지 않으면 `IllegalStateException`을 사용하자.
