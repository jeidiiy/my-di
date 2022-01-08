package com.wscrg;

public class StringUtils {

    public static String lowerCaseFirst(String str) {
        char[] arr = str.toCharArray();
        arr[0] = Character.toLowerCase(arr[0]);
        return new String(arr);
    }

}
