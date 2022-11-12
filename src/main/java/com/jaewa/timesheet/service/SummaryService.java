package com.jaewa.timesheet.service;

import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Summary;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.model.specialday.EasterUtility;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

import static com.jaewa.timesheet.model.specialday.FixedSpecialDay.*;


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

        int workdays = summaryWorkdays.stream().mapToInt(Workday::getWorkingHours).sum();
        int holidaysHours = summaryWorkdays.stream().mapToInt(w -> w.isHoliday() ? 8 : 0).sum();
        int sicknessHours = summaryWorkdays.stream().mapToInt(w -> w.isSick() ? 8 : 0).sum();
        int permitHours = summaryWorkdays.stream().mapToInt(Workday::getWorkPermitHours).sum();
        int extraHours = summaryWorkdays.stream().mapToInt(Workday::getExtraHours).sum();
        int nightHours = summaryWorkdays.stream().mapToInt(Workday::getNightWorkingHours).sum();
        int funeralLeaveHours = summaryWorkdays.stream().mapToInt(w -> w.isFuneralLeave() ? 8 : 0).sum();
        int loggedHours = workdays + holidaysHours + sicknessHours + permitHours + funeralLeaveHours;

        int nonWorkinghours = getWorkOnSpecialDays(month, monthDaysNumber, year);
        int toLogHours = (monthDaysNumber * 8) - nonWorkinghours - loggedHours;

        if (toLogHours < 0) {
            toLogHours = 0;
        }

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

    private int getWorkOnSpecialDays(int month, int monthDaysNumber, int year) {
        int nonWorkingHours = 0;
        String easter = EasterUtility.calculateEaster(year).getDayAndMonth();
        String easterMonday = EasterUtility.calculateEasterMonday(year).getDayAndMonth();

        Calendar calendar = Calendar.getInstance();

        for (int day = 1; day <= monthDaysNumber; day++) {
            String dayWithMonth = String.format("%s/%s", day, month);

            calendar.set(year, (month - 1), day);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                nonWorkingHours += 8;
                continue;
            }

            if (dayWithMonth.equals(NEW_YEARS_EVE.getSpecialDay().getDayAndMonth())
                    || dayWithMonth.equals(EPIPHANY.getSpecialDay().getDayAndMonth())
                    || dayWithMonth.equals(APRIL_TWENTY_FIFTH.getSpecialDay().getDayAndMonth())
                    || dayWithMonth.equals(WORKERS_DAY.getSpecialDay().getDayAndMonth())
                    || dayWithMonth.equals(REPUBLIC_DAY.getSpecialDay().getDayAndMonth())
                    || dayWithMonth.equals(MIDSUMMER.getSpecialDay().getDayAndMonth())
                    || dayWithMonth.equals(MID_AUGUST.getSpecialDay().getDayAndMonth())
                    || dayWithMonth.equals(ALL_SAINTS_DAY.getSpecialDay().getDayAndMonth())
                    || dayWithMonth.equals(IMMACULATE_CONCEPTION.getSpecialDay().getDayAndMonth())
                    || dayWithMonth.equals(CHRISTMAS.getSpecialDay().getDayAndMonth())
                    || dayWithMonth.equals(BOXING_DAY.getSpecialDay().getDayAndMonth())
                    || dayWithMonth.equals(easter)
                    || dayWithMonth.equals(easterMonday)) {
                nonWorkingHours += 8;
            }
        }

        return nonWorkingHours;
    }
}
