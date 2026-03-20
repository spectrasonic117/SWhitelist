package com.spectrasonic.SWhitelist.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d+)([smh])?$");

    // Parsear string de tiempo a milisegundos
    public static long parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return 0;
        }

        Matcher matcher = TIME_PATTERN.matcher(timeString.toLowerCase());
        if (!matcher.matches()) {
            return 0;
        }

        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);

        if (unit == null) {
            // Sin sufijo, asumir segundos
            return value * 1000;
        }

        return switch (unit) {
            case "s" -> value * 1000;
            case "m" -> value * 60 * 1000;
            case "h" -> value * 60 * 60 * 1000;
            default -> 0;
        };
    }

    // Formatear milisegundos a string legible
    public static String formatDuration(long millis, String nowFormat, String secondFormat, 
                                       String minuteFormat, String hourFormat) {
        if (millis <= 0) {
            return nowFormat;
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + " " + hourFormat;
        } else if (minutes > 0) {
            return minutes + " " + minuteFormat;
        } else {
            return seconds + " " + secondFormat;
        }
    }

    // Validar formato de tiempo
    public static boolean isValidTimeFormat(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return false;
        }
        return TIME_PATTERN.matcher(timeString.toLowerCase()).matches();
    }
}
