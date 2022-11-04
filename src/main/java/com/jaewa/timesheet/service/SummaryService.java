package com.jaewa.timesheet.service;

import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Summary;
import com.jaewa.timesheet.model.Workday;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;


@Service
public class SummaryService {

    private final WorkdayService workdayService;

    private final ApplicationUserService applicationUserService;

    public SummaryService(WorkdayService workdayService, ApplicationUserService applicationUserService) {
        this.workdayService = workdayService;
        this.applicationUserService = applicationUserService;
    }

    public Summary getSummaryData(int year, int month, Long applicationUserId) {
        LocalDate date = LocalDate.of(year, month, 1);
        LocalDate from = date.withDayOfMonth(1);

        int monthDaysNumber = date.getMonth().length(date.isLeapYear());
        LocalDate to = date.withDayOfMonth(monthDaysNumber);

        ApplicationUser applicationUser = applicationUserService.findById(applicationUserId);

        List<Workday> summaryWorkdays = workdayService.findWorkdayByUser(applicationUser.getUsername(), from, to);

        int accidentAtWorkHours = summaryWorkdays.stream().mapToInt(w -> w.isAccidentAtWork() ? 8 : 0).sum();

        int workHours = summaryWorkdays.stream().mapToInt(Workday::getWorkingHours).sum();
        int holidaysHours = summaryWorkdays.stream().mapToInt(w -> w.isHoliday() ? 8 : 0).sum();
        int sicknessHours = summaryWorkdays.stream().mapToInt(w -> w.isSick() ? 8 : 0).sum();
        int permitHours = summaryWorkdays.stream().mapToInt(Workday::getWorkPermitHours).sum();
        int extraHours = summaryWorkdays.stream().mapToInt(Workday::getExtraHours).sum();
        int nightHours = summaryWorkdays.stream().mapToInt(Workday::getNightWorkingHours).sum();
        int funeralLeaveHours = summaryWorkdays.stream().mapToInt(w -> w.isFuneralLeave() ? 8 : 0).sum();
        int loggedHours = workHours + holidaysHours + sicknessHours + permitHours + nightHours + funeralLeaveHours + extraHours;

        Calendar calendar = Calendar.getInstance();

        int nonWorkinghours = 0;

        for (int day = 1; day <= monthDaysNumber; day++) {
            calendar.set(year, month - 1, day);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                nonWorkinghours += 8;
            }
        }

        //XXX FIXME nonWorkingHours could be useful to define nonWorkingDays after we define how to log them
        int toLogHours = (monthDaysNumber * 8) - nonWorkinghours - loggedHours - extraHours; // subtract extra hours because they should not be included in the calculation

        return Summary.builder()
                .loggedAccidentAtWorkHours(accidentAtWorkHours)
                .loggedHolidaysHours(holidaysHours)
                .loggedSicknessHours(sicknessHours)
                .loggedPermitHours(permitHours)
                .loggedExtraHours(extraHours)
                .loggedNightHours(nightHours)
                .loggedFuneralLeaveHours(funeralLeaveHours)
                .loggedHours(loggedHours)
                .toLogHours(toLogHours)
                .build();

    }
}
