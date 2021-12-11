package com.raccoon.invisor.utils;

public class Utils {

    public static <T> T nvl(T source, T target) {
        return (source == null) ? target : source;
    }

}
