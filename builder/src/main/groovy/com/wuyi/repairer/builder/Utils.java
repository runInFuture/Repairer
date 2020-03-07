package com.wuyi.repairer.builder;

/**
 * Common util methods collection.
 */
public class Utils {
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean strEquals(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }
}
