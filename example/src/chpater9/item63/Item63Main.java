package chpater9.item63;

import java.util.ArrayList;
import java.util.List;

public class Item63Main {

    public static void main(String[] args) {
        List<String> itemNameList = List.of("A","B","C","D");

        System.out.println(statement(itemNameList));
        System.out.println(statement2(itemNameList));
    }

    public static String statement(List<String> itemNameList) {
        String result = "";
        for (String itemName : itemNameList) {
            result += itemName;
        }
        return result;
    }

    public static String statement2(List<String> itemNameList) {

        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder sizeLimitedStringBuilder = new StringBuilder(itemNameList.size() * 100); // 길이 지정 방식이 성능이 제일 빠름.

        for (String itemName : itemNameList) {
            stringBuilder.append(itemName);
        }
        return stringBuilder.toString();
    }

}
