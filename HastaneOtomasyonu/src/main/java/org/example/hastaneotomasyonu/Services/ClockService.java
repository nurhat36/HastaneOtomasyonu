package org.example.hastaneotomasyonu.Services;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockService {
    private static String currentTime = getCurrentTimeFormatted();

    public static String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public static String getCurrentTime() {
        return currentTime;
    }

    public static void updateTime() {
        currentTime = getCurrentTimeFormatted();
    }
}
