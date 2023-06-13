package chapter2.item1;

public class Person {

    private String name;
    private int age;
    private String address;

    public Person() {
    }

    /**
     * 비추
     */
    public Person(String name, int age, String address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }

    /**
     * 아래처럼 사용하는걸 추천 메소드
     */
    // 첫번째 장점 - 인스턴스 생생 메소드가 이름을 가질 수 있다.
    // 정적 팩터리 메소드를 통해 인스턴스 생성에 대한 내용을 메소드 명으로 설명 할수 있다!!!
    public static Person from(String name, int age, String address) {

        Person person = new Person();

        person.name = name;
        person.age = age;
        person.address = address;

        return person;
    }

    public static Person createZeroAgePerson(String name, String address) {

        Person person = new Person();

        person.name = name;
        person.age = 0;
        person.address = address;

        return person;

    }


}
