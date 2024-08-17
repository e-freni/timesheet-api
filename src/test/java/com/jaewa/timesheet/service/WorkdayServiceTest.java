package com.jaewa.timesheet.service;

import com.jaewa.timesheet.exception.IncoherentDataException;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.model.repository.WorkdayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.jaewa.timesheet.service.WorkdayService.FULL_WORK_DAY_HOURS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkdayServiceTest {

    @Mock
    private WorkdayRepository workdayRepository;

    @InjectMocks
    private WorkdayService workdayService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findWorkdayByUserShouldReturnWorkdaysForGivenUserAndDateRange() {
        String username = "testuser";
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 1, 31);
        Workday workday1 = new Workday();
        Workday workday2 = new Workday();

        when(workdayRepository.findByApplicationUserUsernameAndDate(username, fromDate, toDate))
                .thenReturn(Arrays.asList(workday1, workday2));

        List<Workday> workdays = workdayService.findWorkdayByUser(username, fromDate, toDate);

        assertNotNull(workdays);
        assertEquals(2, workdays.size());
        verify(workdayRepository, times(1)).findByApplicationUserUsernameAndDate(username, fromDate, toDate);
    }

    @Test
    void findByIdShouldReturnWorkdayWhenFound() {
        Long workdayId = 1L;
        Workday workday = new Workday();
        when(workdayRepository.findById(workdayId)).thenReturn(Optional.of(workday));

        Optional<Workday> result = workdayService.findById(workdayId);

        assertTrue(result.isPresent());
        assertEquals(workday, result.get());
    }

    @Test
    void findByIdShouldReturnEmptyWhenNotFound() {
        Long workdayId = 1L;
        when(workdayRepository.findById(workdayId)).thenReturn(Optional.empty());

        Optional<Workday> result = workdayService.findById(workdayId);

        assertFalse(result.isPresent());
    }

    @Test
    void addNewWorkdayShouldSaveWorkdayIfValid() throws IncoherentDataException {
        Workday workday = new Workday();
        workday.setApplicationUser(new ApplicationUser());
        workday.setDate(LocalDate.of(2024, 1, 1));
        workday.setWorkingHours(8);

        when(workdayRepository.findByDateAndApplicationUserUsername(workday.getDate(), workday.getApplicationUser().getUsername()))
                .thenReturn(Optional.empty());

        Workday savedWorkday = new Workday();
        when(workdayRepository.save(workday)).thenReturn(savedWorkday);

        Workday result = workdayService.addNewWorkday(workday);

        assertNotNull(result);
        assertEquals(savedWorkday, result);
        verify(workdayRepository, times(1)).save(workday);
    }

    @Test
    void addNewWorkdayShouldThrowIncoherentDataExceptionIfDateAlreadyLogged() {
        Workday workday = new Workday();
        workday.setApplicationUser(new ApplicationUser());
        workday.setDate(LocalDate.of(2024, 1, 1));

        when(workdayRepository.findByDateAndApplicationUserUsername(workday.getDate(), workday.getApplicationUser().getUsername()))
                .thenReturn(Optional.of(workday));

        assertThrows(IncoherentDataException.class, () -> workdayService.addNewWorkday(workday));
    }

    @Test
    void editWorkdayShouldUpdateWorkdayIfValid() throws IncoherentDataException {
        Workday workday = new Workday();
        workday.setId(1L);
        workday.setWorkingHours(8);

        when(workdayRepository.findById(workday.getId())).thenReturn(Optional.of(workday));
        when(workdayRepository.save(workday)).thenReturn(workday);

        Workday result = workdayService.editWorkday(workday);

        assertNotNull(result);
        assertEquals(workday, result);
        verify(workdayRepository, times(1)).save(workday);
    }

    @Test
    void editWorkdayShouldThrowEntityNotFoundExceptionIfWorkdayNotFound() {
        Workday workday = new Workday();
        workday.setId(1L);
        workday.setWorkingHours(8);

        when(workdayRepository.findById(workday.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> workdayService.editWorkday(workday));
    }

    @Test
    void deleteWorkdayShouldDeleteWorkdayIfFound() {
        Long workdayId = 1L;
        Workday workday = new Workday();

        when(workdayRepository.findById(workdayId)).thenReturn(Optional.of(workday));

        workdayService.deleteWorkday(workdayId);

        verify(workdayRepository, times(1)).deleteById(workdayId);
    }

    @Test
    void deleteWorkdayShouldThrowEntityNotFoundExceptionIfWorkdayNotFound() {
        Long workdayId = 1L;

        when(workdayRepository.findById(workdayId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> workdayService.deleteWorkday(workdayId));
    }

    @Test
    void checkUserNormalDayWorkShouldThrowExceptionForFullHoursAndSick() throws Exception {
        Method method = WorkdayService.class.getDeclaredMethod("checkUserNormalDayWork", Workday.class);
        method.setAccessible(true);

        Workday workday = new Workday();
        workday.setWorkingHours(FULL_WORK_DAY_HOURS);
        workday.setSick(true);

        try {
            method.invoke(workdayService, workday);
            fail("Expected IncoherentDataException to be thrown");
        } catch (InvocationTargetException e) {
            // Verifica che l'eccezione originale sia IncoherentDataException
            Throwable targetException = e.getTargetException();
            assertInstanceOf(IncoherentDataException.class, targetException, "Expected IncoherentDataException, but got " + targetException);
        }
    }

    @Test
    void checkUserExtraHoursShouldThrowExceptionForIncoherentExtraHours() throws Exception {
        Method method = WorkdayService.class.getDeclaredMethod("checkUserExtraHours", Workday.class);
        method.setAccessible(true);

        Workday workday = new Workday();
        workday.setExtraHours(2);
        workday.setWorkingHours(FULL_WORK_DAY_HOURS - 1);

        try {
            method.invoke(workdayService, workday);
            fail("Expected IncoherentDataException to be thrown");
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            assertInstanceOf(IncoherentDataException.class, targetException, "Expected IncoherentDataException, but got " + targetException);
        }
    }


    @Test
    void checkUserNightHoursShouldThrowExceptionForIncoherentNightHours() throws Exception {
        Method method = WorkdayService.class.getDeclaredMethod("checkUserNightHours", Workday.class);
        method.setAccessible(true);

        Workday workday = new Workday();
        workday.setNightWorkingHours(3);
        workday.setWorkingHours(FULL_WORK_DAY_HOURS - 3);

        try {
            method.invoke(workdayService, workday);
            fail("Expected IncoherentDataException to be thrown");
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            assertInstanceOf(IncoherentDataException.class, targetException, "Expected IncoherentDataException, but got " + targetException);
        }
    }

    @Test
    void checkUserHolidayShouldThrowExceptionForHolidayAndAccidentAtWork() throws Exception {
        Method method = WorkdayService.class.getDeclaredMethod("checkUserHoliday", Workday.class);
        method.setAccessible(true);

        Workday workday = new Workday();
        workday.setHoliday(true);
        workday.setAccidentAtWork(true);

        try {
            method.invoke(workdayService, workday);
            fail("Expected IncoherentDataException to be thrown");
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            assertInstanceOf(IncoherentDataException.class, targetException, "Expected IncoherentDataException, but got " + targetException);
        }
    }

    @Test
    void checkUserAccidentAtWorkShouldThrowExceptionForAccidentAndWorkingHours() throws Exception {
        Method method = WorkdayService.class.getDeclaredMethod("checkUserAccidentAtWork", Workday.class);
        method.setAccessible(true);

        Workday workday = new Workday();
        workday.setAccidentAtWork(true);
        workday.setWorkingHours(4);

        try {
            method.invoke(workdayService, workday);
            fail("Expected IncoherentDataException to be thrown");
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            assertInstanceOf(IncoherentDataException.class, targetException, "Expected IncoherentDataException, but got " + targetException);
        }
    }

    @Test
    void checkUserSicknessShouldThrowExceptionForSicknessAndWorkingHours() throws Exception {
        Method method = WorkdayService.class.getDeclaredMethod("checkUserSickness", Workday.class);
        method.setAccessible(true);

        Workday workday = new Workday();
        workday.setSick(true);
        workday.setWorkingHours(4);

        try {
            method.invoke(workdayService, workday);
            fail("Expected IncoherentDataException to be thrown");
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            assertInstanceOf(IncoherentDataException.class, targetException, "Expected IncoherentDataException, but got " + targetException);
        }
    }

    @Test
    void checkUserWorkPermitHoursShouldThrowExceptionForIncoherentHours() throws Exception {
        Method method = WorkdayService.class.getDeclaredMethod("checkUserWorkPermitHours", Workday.class);
        method.setAccessible(true);

        Workday workday = new Workday();
        workday.setWorkPermitHours(4);
        workday.setWorkingHours(5);

        try {
            method.invoke(workdayService, workday);
            fail("Expected IncoherentDataException to be thrown");
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            assertInstanceOf(IncoherentDataException.class, targetException, "Expected IncoherentDataException, but got " + targetException);
        }
    }



}
