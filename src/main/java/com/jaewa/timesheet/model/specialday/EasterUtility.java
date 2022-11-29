package com.jaewa.timesheet.model.specialday;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class EasterUtility {

    private EasterUtility() {
        //è una classe statica
    }

    public static LocalDate easterAlgorithm(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int s = h + l - 7 * m + 114;
        int month = s / 31;
        int day = (s % 31) + 1;
        return LocalDate.of(year, month, day);
    }


    public static SpecialDay calculateEaster(int year) {
        LocalDate easterLocalDate = easterAlgorithm(year);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M");
        return new SpecialDay(easterLocalDate.format(formatter), "Pasqua");
    }

    public static SpecialDay calculateEasterMonday(int year) {
        LocalDate easterLocalDate = easterAlgorithm(year);
        LocalDate easterMondayLocalDate = easterLocalDate.plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M");
        return new SpecialDay(easterMondayLocalDate.format(formatter), "Pasquetta");
    }
}
