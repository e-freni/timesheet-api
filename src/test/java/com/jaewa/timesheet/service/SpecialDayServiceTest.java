package com.jaewa.timesheet.service;

import com.jaewa.timesheet.model.specialday.SpecialDay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecialDayServiceTest {

    private SpecialDayService specialDayService;

    @BeforeEach
    void setUp() {
        specialDayService = new SpecialDayService();
    }

    @Test
    void getSpecialDaysShouldReturnSpecialDaysForGivenMonth() {
        Integer year = 2024;
        Integer month = 4;

        List<SpecialDay> result = specialDayService.getSpecialDays(year, month);

        assertEquals(2, result.size());
        assertEquals("Festa della liberazione", result.get(0).getName());
        assertEquals("Pasquetta", result.get(1).getName());
    }

    @Test
    void getSpecialDaysShouldReturnEmptyListForMonthWithNoSpecialDays() {
        Integer year = 2024;
        Integer month = 7;

        List<SpecialDay> result = specialDayService.getSpecialDays(year, month);

        assertEquals(0, result.size());
    }
}
