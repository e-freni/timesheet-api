package com.jaewa.timesheet.service;

import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Summary;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.model.repository.ApplicationUserRepository;
import com.jaewa.timesheet.model.repository.WorkdayRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.transaction.Transactional;
import java.time.LocalDate;

import static com.jaewa.timesheet.model.UserRole.USER;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = SummaryServiceTest.Initializer.class)
@Testcontainers
class SummaryServiceTest extends BaseDataTest {

    public static final int JANUARY_WITHOUT_SATURNDAYS_AND_SUNDAYS = 21;
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

        for (int i = 1; i <= JANUARY_WITHOUT_SATURNDAYS_AND_SUNDAYS; i++) {
            Workday workday = Workday.builder()
                    .applicationUser(u1)
                    .accidentAtWork(false)
                    .sick(false)
                    .holiday(false)
                    .workingHours(8)
                    .extraHours(0)
                    .workPermitHours(0)
                    .nightWorkingHours(0)
                    .funeralLeaveHours(0)
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

        for (int i = 1; i <= JANUARY_WITHOUT_SATURNDAYS_AND_SUNDAYS - 10; i++) {
            Workday workday = Workday.builder()
                    .applicationUser(u1)
                    .accidentAtWork(false)
                    .sick(false)
                    .holiday(false)
                    .workingHours(8)
                    .extraHours(0)
                    .workPermitHours(0)
                    .nightWorkingHours(0)
                    .funeralLeaveHours(0)
                    .date(LocalDate.of(2022, 1, i))
                    .build();
            this.workdayRepository.save(workday);
        }

        Summary summaryData = summaryService.getSummaryData(2022, 1, u1.getId());

        assertEquals(88, summaryData.getLoggedHours());
        assertEquals(80, summaryData.getToLogHours());
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

        //TODO check calculation

        for (int i = 1; i <= JANUARY_WITHOUT_SATURNDAYS_AND_SUNDAYS - 10; i++) {
            Workday workday = Workday.builder()
                    .applicationUser(u1)
                    .accidentAtWork(false)
                    .sick(false)
                    .holiday(false)
                    .workingHours(8)
                    .extraHours(0)
                    .workPermitHours(0)
                    .nightWorkingHours(0)
                    .funeralLeaveHours(0)
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
                .funeralLeaveHours(0)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURNDAYS_AND_SUNDAYS - 9))
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
                .funeralLeaveHours(0)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURNDAYS_AND_SUNDAYS - 8))
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
                .funeralLeaveHours(0)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURNDAYS_AND_SUNDAYS - 7))
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
                .funeralLeaveHours(0)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURNDAYS_AND_SUNDAYS - 6))
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
                .funeralLeaveHours(0)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURNDAYS_AND_SUNDAYS - 5))
                .build();
        this.workdayRepository.save(workday);
        //100 working hours - 4 permit - 8 holiday - 8 sickness - 8 accident at work - 4 extra hours

        workday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(false)
                .workingHours(4)
                .extraHours(0)
                .workPermitHours(0)
                .nightWorkingHours(4)
                .funeralLeaveHours(0)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURNDAYS_AND_SUNDAYS - 4))
                .build();
        this.workdayRepository.save(workday);
        //104 working hours - 4 permit - 8 holiday - 8 sickness - 8 accident at work - 4 extra hours - 4 night hours

        workday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(false)
                .workingHours(4)
                .extraHours(0)
                .workPermitHours(0)
                .nightWorkingHours(0)
                .funeralLeaveHours(4)
                .date(LocalDate.of(2022, 1, JANUARY_WITHOUT_SATURNDAYS_AND_SUNDAYS - 3))
                .build();
        this.workdayRepository.save(workday);

        Summary summaryData = summaryService.getSummaryData(2022, 1, u1.getId());

        int notLoggedHours = 8 * 3;
        assertEquals(168 - notLoggedHours + extrahours, summaryData.getLoggedHours());
        assertEquals(24, summaryData.getToLogHours());
        assertEquals(4, summaryData.getLoggedPermitHours());
        assertEquals(4, summaryData.getLoggedExtraHours());
        assertEquals(4, summaryData.getLoggedNightHours());
        assertEquals(4, summaryData.getLoggedFuneralLeaveHours());
        assertEquals(8, summaryData.getLoggedHolidaysHours());
        assertEquals(8, summaryData.getLoggedSicknessHours());
        assertEquals(8, summaryData.getLoggedAccidentAtWorkHours());
    }

}
