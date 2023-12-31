# 예외의 상세 메시지에 실패 관련 정보를 담으라

## 예외의 상세 메시지는 주로 어떻게 사용될까?

예외를 잡지 못해 프로그램이 실패하면 자바 시스템은 그 예외의 스택 추적(stack trace) 정보를 자동으로 출력한다. <br/>
별도로 `toString()` 을 override 하지 않는 이상 보통 예외의 클래스 이름 뒤에 상세 메시지가 붙는 형태로 출력된다. <br/>
출력된 이 정보가 실패 원인을 분석해야하는 프로그래머 혹은 사이트 신뢰성 엔지니어(SRE)가 얻을 수 있는 유일한 정보인 경우가 많다.

즉, `사후 분석`을 목적으로 사용되고, 이를 위해 실패 순간의 상황을 정확히 포착해 예외의 상세 메시지에 담아야한다.



## 예외 상세메시지는 발생한 예외에 관여된 모든 매개변수와 필드의 값을 담아야 한다.

실패 순간을 포착하려면 발생한 예외에 관여된 모든 매개변수와 필드의 값을 실패 메시지에 담아야 한다. <br/>

> IndexOutOfBoundsException 의 상세 메시지는 범위의 최솟값과 최댓값 그리고 그 범위를 벗어났다는 인덱스 값 모두 담아야 한다.

위와 같이 단순히 범위가 벗어났다는 메시지만 제공하는 것 대신 범위가 어떻게 되었고, 어떤 값이여서 해당 예외가 발생한지 정보를 다 제공해주어야 추후 이슈에 대해 원인 분석할 때 많은 정보를 제공해준다. <br/>
단, 많은 정보를 담는다고 소스코드에서 얻을 수 있는 정보까지 담을 필요는 없다. 스택 추적에는 예외가 발생한 파일 이름과 줄번호는 물론 스택에서 호출한 다른 메서드들까지 제공하기 때문이다.

> 단, 보안과 관련된 정보는 주의해서 다뤄야한다. 스택 추적 정보는 많은 사람이 볼 수 있으므로 **상세 메세지에 비밀번호나 암호 키 같은 정보**까지 담아서는 안된다. 



## 예외의 상세 메시지와 최종 사용자에게 보여줄 오류 메시지를 혼동해서는 안된다.

예외의 상세메시지의 주 소비층은 문제를 분석해야 할 프로그래머와 SRE 엔지니어이기 때문에 가독성보다는 담긴 내용이 중요하고, <br/>
최종 사용자에게 보여줄 오류메시지는 현지어로 번역이 중요하거나, 범용적인 내용으로 제공하는 것이 중요하다.



## 상세 메시지를 제공하기 위해 생성자를 추가하는 것도 고려하라

실패를 적절히 포착하려면 필요한 정보를 예외 생성자에서 모두 받아서 상세 메시지까지 미리 생성해놓는 방법도 괜찮다. <br/>

```java
public class IndexOutOfOfBoundException {

    private final static String MESSAGE = "최솟값: %d, 최댓값: %d, 인덱스: %d";
    
    ...
    
    public IndexOutOfOfBoundException(final int lowerBound, final int upperBound, final int index) {
        // 실패를 포착하는 상세 메시지를 생성한다.
        super(String.format(MESSAGE, lowerBound, upperBound, index));
        
        // 프로그램에서 이용할 수 있도록 실패 정보를 저장해둔다.
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.index = index;
    }
}
```
