package com.jaewa.timesheet.service;

import com.jaewa.timesheet.model.specialday.EasterUtility;
import com.jaewa.timesheet.model.specialday.FixedSpecialDay;
import com.jaewa.timesheet.model.specialday.SpecialDay;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpecialDayService {
    public List<SpecialDay> getSpecialDays(Integer year, Integer month) {
        List<SpecialDay> specialDays = FixedSpecialDay.getAllFixedSpecialDays();
        specialDays.add(EasterUtility.calculateEaster(year));
        specialDays.add(EasterUtility.calculateEasterMonday(year));

        List<SpecialDay> monthSpecialDays = new ArrayList<>();
        for (SpecialDay s : specialDays) {
            if (s.getDayAndMonth().split("/")[1].equals(String.valueOf(month))) {
                monthSpecialDays.add(s);
            }
        }
        return monthSpecialDays;
    }
}
