package utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-M-d");
    public static String fmt(LocalDate d) { return d.format(F); }
}
