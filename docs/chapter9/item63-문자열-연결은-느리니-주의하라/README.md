# 문자열 연결은 느리지 주의하라

한 줄짜리 출력값 또는 작고 크기가 고정된 객체의 문자열 표현을 만들때는 큰 문제 없다.

길이가 긴 문자열을 연결할때는 성능 저하가 발생한다. 

## 성능
문자열 연결 연산자로 문자열 n개를 잇는 시간은 n^2에 비례한다. 

문자열은 불변이라서 두 문자열을 연결할 경우 양쪽의 내용을 모두 복사해야하므로 성능 저하는 피할 수 없는 결과다.

## 개선

아래 코드들은 아이템 이름 목록을 받아서 하나의 문자열로 연결하는 코드이다.

품목이 많아지는 경우 아래 메서드는 심각하게 느려질 수 있다. 자바6 이후로 개선이 되긴 했지만 여전히 느리다.
```java
public static String statement(List<String> itemNameList) {
    String result = "";
    for (String itemName : itemNameList) {
        result += itemName;
    }
    return result;
}
```

성능을 포기하고 싶지 않다면 String 대신 StringBuilder를 사용하자.

아이템을 100개로 세팅하고 테스트를 했을떄 String을 사용했을 때 보다 6.5배가 빨랐다(길이 지정 방식)

길이 지정 방식을 사용하지 않은 경우라하더라도 String보다 5.5배 빨랐다.
```java
public static String statement2(List<String> itemNameList) {

    StringBuilder stringBuilder = new StringBuilder();
    StringBuilder sizeLimitedStringBuilder = new StringBuilder(itemNameList.size() * 100); // 길이 지정 방식이 성능이 제일 빠름.

    for (String itemName : itemNameList) {
        stringBuilder.append(itemName);
    }
    return stringBuilder.toString();
}
```

