package com.jaewa.timesheet.service;

import com.jaewa.timesheet.AbstractIntegrationTest;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Summary;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.model.repository.ApplicationUserRepository;
import com.jaewa.timesheet.model.repository.WorkdayRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.time.LocalDate;

import static com.jaewa.timesheet.model.UserRole.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SummaryServiceTest extends AbstractIntegrationTest {

    public static final int JANUARY_WITHOUT_SATURDAYS_AND_SUNDAYS = 21;
    @Autowired
    SummaryService summaryService;

    @Autowired
    WorkdayRepository workdayRepository;

    @Autowired
    ApplicationUserRepository applicationUserRepository;

    ApplicationUser u1;

    private void setup() {

        u1 = ApplicationUser.builder()
                .username("baka")
                .email("baka@gmail.com")
                .firstName("Mario")
                .lastName("Rossi")
                .password("$2a$10$zdn7CgQp6TRKIuWvPv6vdOAf7RyJPTxnyDntFqunM2s")
                .role(USER)
                .build();

        this.applicationUserRepository.save(u1);
    }

    @Test
    @Transactional
    void getSummaryDataOnFullWorkingMonth() {
        setup();

        for (int i = 1; i <= JANUARY_WITHOUT_SATURDAYS_AND_SUNDAYS; i++) {
            Workday workday = Workday.builder()
                    .applicationUser(u1)
                    .accidentAtWork(false)
                    .sick(false)
                    .holiday(false)
                    .workingHours(8)
                    .extraHours(0)
                    .workPermitHours(0)
                    .nightWorkingHours(0)
                    .funeralLeave(false)
                    .date(LocalDate.of(2022, 1, i))
                    .build();
            this.workdayRepository.save(workday);
        }

        Summary summaryData = summaryService.getSummaryData(2022, 1, u1.getId());

        assertEquals(168, summaryData.getLoggedHours());
        assertEquals(0, summaryData.getToLogHours());
        assertEquals(0, summaryData.getLoggedPermitHours());
        assertEquals(0, summaryData.getLoggedHolidaysHours());
        assertEquals(0, summaryData.getLoggedExtraHours());
        assertEquals(0, summaryData.getLoggedNightHours());
        assertEquals(0, summaryData.getLoggedSicknessHours());
        assertEquals(0, summaryData.getLoggedAccidentAtWorkHours());
        assertEquals(0, summaryData.getLoggedFuneralLeaveHours());
    }

    @Test
    @Transactional
    void getSummaryDataOnNotFullWorkingMonth() {
        setup();

        for (int i = 1; i <= JANUARY_WITHOUT_SATURDAYS_AND_SUNDAYS - 10; i++) {
            Workday workday = Workday.builder()
                    .applicationUser(u1)
                    .accidentAtWork(false)
                    .sick(false)
                    .holiday(false)
                    .workingHours(8)
                    .extraHours(0)
                    .workPermitHours(0)
                    .nightWorkingHours(0)
                    .funeralLeave(false)
                    .date(LocalDate.of(2022, 1, i))
                    .build();
            this.workdayRepository.save(workday);
        }

        Summary summaryData = summaryService.getSummaryData(2022, 1, u1.getId());

        assertEquals(88, summaryData.getLoggedHours());
        assertEquals(72, summaryData.getToLogHours());
        assertEquals(0, summaryData.getLoggedPermitHours());
        assertEquals(0, summaryData.getLoggedHolidaysHours());
        assertEquals(0, summaryData.getLoggedExtraHours());
        assertEquals(0, summaryData.getLoggedNightHours());
        assertEquals(0, summaryData.getLoggedSicknessHours());
        assertEquals(0, summaryData.getLoggedAccidentAtWorkHours());
        assertEquals(0, summaryData.getLoggedFuneralLeaveHours());
    }

    @Test
    @Transactional
    void getSummaryDataOnNotFullWorkingMonthWithSomeDifferentData() {
        setup();

        for (int i = 1; i <= JANUARY_WITHOUT_SATURDAYS_AND_SUNDAYS - 10; i++) {
            Workday workday = Workday.builder()
                    .applicationUser(u1)
                    .accidentAtWork(false)
                    .sick(false)
                    .holiday(false)
                    .workingHours(8)
                    .extraHours(0)
                    .workPermitHours(0)
                    .nightWorkingHours(0)
                    .funeralLeave(false)
                    .date(LocalDate.of(2022, 1, i))
                    .build();
            this.workdayRepository.save(workday);
        }
        //88 working hours

        Workday workday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(false)
                .workingHours(4)
                .extraHours(0)
                .workPermitHours(4)
                .nightWorkingHours(0)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURDAYS_AND_SUNDAYS - 9))
                .build();
        this.workdayRepository.save(workday);
        //92 working hours - 4 permit


        workday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(true)
                .workingHours(0)
                .extraHours(0)
                .workPermitHours(0)
                .nightWorkingHours(0)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURDAYS_AND_SUNDAYS - 8))
                .build();
        this.workdayRepository.save(workday);
        //92 working hours - 4 permit - 8 holiday

        workday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(true)
                .holiday(false)
                .workingHours(0)
                .extraHours(0)
                .workPermitHours(0)
                .nightWorkingHours(0)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURDAYS_AND_SUNDAYS - 7))
                .build();
        this.workdayRepository.save(workday);
        //92 working hours - 4 permit - 8 holiday - 8 sickness

        workday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(true)
                .sick(false)
                .holiday(false)
                .workingHours(0)
                .extraHours(0)
                .workPermitHours(0)
                .nightWorkingHours(0)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURDAYS_AND_SUNDAYS - 6))
                .build();
        this.workdayRepository.save(workday);
        //92 working hours - 4 permit - 8 holiday - 8 sickness - 8 accident at work

        int extrahours = 4;
        workday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(false)
                .workingHours(8)
                .extraHours(extrahours)
                .workPermitHours(0)
                .nightWorkingHours(0)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURDAYS_AND_SUNDAYS - 5))
                .build();
        this.workdayRepository.save(workday);
        //100 working hours - 4 permit - 8 holiday - 8 sickness - 8 accident at work - 4 extra hours

        workday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(false)
                .workingHours(8)
                .extraHours(0)
                .workPermitHours(0)
                .nightWorkingHours(4)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURDAYS_AND_SUNDAYS - 4))
                .build();
        this.workdayRepository.save(workday);
        //108 working hours - 4 permit - 8 holiday - 8 sickness - 8 accident at work - 4 extra hours - 4 night hours

        workday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(false)
                .workingHours(0)
                .extraHours(0)
                .workPermitHours(0)
                .nightWorkingHours(0)
                .funeralLeave(true)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURDAYS_AND_SUNDAYS - 3))
                .build();
        this.workdayRepository.save(workday);
        //108 working hours - 4 permit - 8 holiday - 8 sickness - 8 accident at work - 4 extra hours - 4 night hours - 8 funeral leave

        Summary summaryData = summaryService.getSummaryData(2022, 1, u1.getId());

        int workingHours = 168;
        int epifany = 8;

        workingHours = workingHours - epifany;

        int notLoggedHours = 8 * 3;

        assertEquals(workingHours - notLoggedHours, summaryData.getLoggedHours());
        assertEquals(24, summaryData.getToLogHours());
        assertEquals(4, summaryData.getLoggedPermitHours());
        assertEquals(4, summaryData.getLoggedExtraHours());
        assertEquals(4, summaryData.getLoggedNightHours());
        assertEquals(8, summaryData.getLoggedFuneralLeaveHours());
        assertEquals(8, summaryData.getLoggedHolidaysHours());
        assertEquals(8, summaryData.getLoggedSicknessHours());
        assertEquals(8, summaryData.getLoggedAccidentAtWorkHours());
    }

    @Test
    @Transactional
    void getSummaryDataOnMonthWithSpecialDays() {
        setup();

        for (int i = 1; i <= 22; i++) {
            Workday workday = Workday.builder()
                    .applicationUser(u1)
                    .accidentAtWork(false)
                    .sick(false)
                    .holiday(false)
                    .workingHours(8)
                    .extraHours(0)
                    .workPermitHours(0)
                    .nightWorkingHours(0)
                    .funeralLeave(false)
                    .date(LocalDate.of(2023, 12, i))
                    .build();
            this.workdayRepository.save(workday);
        }

        Summary summaryData = summaryService.getSummaryData(2023, 12, u1.getId());

        assertEquals(176, summaryData.getLoggedHours());
        assertEquals(0, summaryData.getToLogHours());
        assertEquals(0, summaryData.getLoggedPermitHours());
        assertEquals(0, summaryData.getLoggedHolidaysHours());
        assertEquals(0, summaryData.getLoggedExtraHours());
        assertEquals(0, summaryData.getLoggedNightHours());
        assertEquals(0, summaryData.getLoggedSicknessHours());
        assertEquals(0, summaryData.getLoggedAccidentAtWorkHours());
        assertEquals(0, summaryData.getLoggedFuneralLeaveHours());
    }

    @Test
    @Transactional
    void getSummaryDataOnMonthWithWeekendsAndHolidays() {
        setup();

        for (int i = 1; i <= 21; i++) {
            Workday workday = Workday.builder()
                    .applicationUser(u1)
                    .accidentAtWork(false)
                    .sick(false)
                    .holiday(false)
                    .workingHours(8)
                    .extraHours(0)
                    .workPermitHours(0)
                    .nightWorkingHours(0)
                    .funeralLeave(false)
                    .date(LocalDate.of(2024, 2, i))
                    .build();
            this.workdayRepository.save(workday);
        }

        Summary summaryData = summaryService.getSummaryData(2024, 2, u1.getId());

        int totalHoursInFebruary = 29 * 8;
        int nonWorkingHours = 8 * 8; // february weekends
        int expectedLoggedHours = totalHoursInFebruary - nonWorkingHours;

        assertEquals(expectedLoggedHours, summaryData.getLoggedHours());
        assertEquals(0, summaryData.getToLogHours());
    }

    @Test
    @Transactional
    void getSummaryDataWithExtraAndNightHours() {
        setup();

        Workday workday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(false)
                .workingHours(8)
                .extraHours(4)
                .workPermitHours(0)
                .nightWorkingHours(2)
                .funeralLeave(false)
                .date(LocalDate.of(2023, 11, 15))
                .build();
        this.workdayRepository.save(workday);

        Summary summaryData = summaryService.getSummaryData(2023, 11, u1.getId());

        assertEquals(8, summaryData.getLoggedHours());
        assertEquals(4, summaryData.getLoggedExtraHours());
        assertEquals(2, summaryData.getLoggedNightHours());
        assertEquals(160, summaryData.getToLogHours());
    }


}
