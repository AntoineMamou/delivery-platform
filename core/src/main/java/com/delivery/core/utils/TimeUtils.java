package com.delivery.core.utils;

public class TimeUtils {
    public static boolean isWithinWindow(int time, int start, int end) {
        return time >= start && time <= end;
    }
}
