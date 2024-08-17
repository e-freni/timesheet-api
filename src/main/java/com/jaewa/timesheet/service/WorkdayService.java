package com.jaewa.timesheet.service;

import com.jaewa.timesheet.exception.IncoherentDataException;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.model.repository.WorkdayRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


@Service
public class WorkdayService {

    public static final int FULL_WORK_DAY_HOURS = 8;
    public static final int MAX_EXTRA_HOURS_AMOUNT = 10;
    public static final int MAX_NIGHT_HOURS = 5;
    private final WorkdayRepository workdayRepository;


    public WorkdayService(WorkdayRepository workdayRepository) {
        this.workdayRepository = workdayRepository;
    }

    public List<Workday> findWorkdayByUser(String username, String fromDate, String toDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        LocalDate from = LocalDate.parse(fromDate, formatter);
        LocalDate to = LocalDate.parse(toDate, formatter);

        return workdayRepository.findByApplicationUserUsernameAndDate(username, from, to);
    }

    public List<Workday> findWorkdayByUser(String username, LocalDate fromDate, LocalDate toDate) {

        return workdayRepository.findByApplicationUserUsernameAndDate(username, fromDate, toDate);
    }

    public Optional<Workday> findById(Long id) {
        return workdayRepository.findById(id);
    }

    public Workday addNewWorkday(Workday workday) throws IncoherentDataException {

        checkDateAlreadyLogged(workday);
        checkWorkdayCoherence(workday);
        return workdayRepository.save(workday);
    }

    private static void checkWorkdayCoherence(Workday workday) throws IncoherentDataException {
        checkUserSickness(workday);
        checkUserAccidentAtWork(workday);
        checkUserHoliday(workday);
        checkUserExtraHours(workday);
        checkUserNightHours(workday);
        checkUserWorkPermitHours(workday);
        checkUserNormalDayWork(workday);
    }

    private void checkDateAlreadyLogged(Workday workday) throws IncoherentDataException {
        Optional<Workday> result = workdayRepository.findByDateAndApplicationUserUsername(workday.getDate(), workday.getApplicationUser().getUsername());

        if (result.isPresent()){
            throw new IncoherentDataException(String.format("This day %s has already been logged", workday.getDate()));
        }

    }

    private static void checkUserNormalDayWork(Workday workday) throws IncoherentDataException {
        if (workday.getWorkingHours() == FULL_WORK_DAY_HOURS) {
            if (workday.isSick()) {
                throw new IncoherentDataException("8 working hours are not coherent with sickness");
            }
            if (workday.isHoliday()) {
                throw new IncoherentDataException("8 working hours are not coherent with holidays");
            }
            if (workday.isAccidentAtWork()) {
                throw new IncoherentDataException("8 working hours are not coherent with an accident at work");
            }
            if (workday.getWorkPermitHours() > 0) {
                throw new IncoherentDataException("8 working hours are not coherent with any work permit hours");
            }
        }
        if (!workday.isSick() && !workday.isHoliday() && !workday.isAccidentAtWork() && !workday.isFuneralLeave()
                && workday.getWorkingHours() != FULL_WORK_DAY_HOURS
                && workday.getWorkPermitHours() == 0) {
            throw new IncoherentDataException("There are no conditions to log less/more than 8 working hours");
        }
    }

    private static void checkUserWorkPermitHours(Workday workday) throws IncoherentDataException {
        if (workday.getWorkPermitHours() > 0 && workday.getWorkingHours() + workday.getWorkPermitHours() != FULL_WORK_DAY_HOURS) {
            throw new IncoherentDataException("Permit hours are not coherent with others hours");
        }
    }

    private static void checkUserExtraHours(Workday workday) throws IncoherentDataException {
        if (workday.getExtraHours() > 0 && workday.getExtraHours() <= MAX_EXTRA_HOURS_AMOUNT && workday.getWorkingHours() + workday.getWorkPermitHours() != FULL_WORK_DAY_HOURS) {
            throw new IncoherentDataException("Extra hours are not coherent");
        }
    }

    private static void checkUserNightHours(Workday workday) throws IncoherentDataException {
        if (workday.getNightWorkingHours() > 0 && workday.getNightWorkingHours() <= MAX_NIGHT_HOURS && workday.getWorkingHours() + workday.getWorkPermitHours() != FULL_WORK_DAY_HOURS) {
            throw new IncoherentDataException("Night hours are not coherent");
        }
    }


    private static void checkUserHoliday(Workday workday) throws IncoherentDataException {
        if (workday.isHoliday()) {
            if (workday.isAccidentAtWork()) {
                throw new IncoherentDataException("Holiday is not coherent with accident at work");
            }
            if (workday.isSick()) {
                throw new IncoherentDataException("Holiday is not coherent with sickness");
            }
            if (workday.isFuneralLeave()) {
                throw new IncoherentDataException("Funeral leave is not coherent with sickness");
            }
            if (workday.getWorkingHours() > 0) {
                throw new IncoherentDataException("Holiday is not coherent with any working hours");
            }
            if (workday.getExtraHours() > 0) {
                throw new IncoherentDataException("Holiday is not coherent with any extra hours");
            }
            if (workday.getWorkPermitHours() > 0) {
                throw new IncoherentDataException("Holiday is not coherent with any work permit hours");
            }

        }
    }

    private static void checkUserAccidentAtWork(Workday workday) throws IncoherentDataException {
        if (workday.isAccidentAtWork()) {
            if (workday.isHoliday()) {
                throw new IncoherentDataException("Accident at work is not coherent with holidays");
            }
            if (workday.isSick()) {
                throw new IncoherentDataException("Accident at work is not coherent with sickness");
            }
            if (workday.getWorkingHours() > 0) {
                throw new IncoherentDataException("Accident at work is not coherent with any working hours");
            }
            if (workday.getExtraHours() > 0) {
                throw new IncoherentDataException("Accident at work is not coherent with any extra hours");
            }
            if (workday.getWorkPermitHours() > 0) {
                throw new IncoherentDataException("Accident at work is not coherent with any work permit hours");
            }

        }
    }

    private static void checkUserSickness(Workday workday) throws IncoherentDataException {
        if (workday.isSick()) {
            if (workday.isHoliday()) {
                throw new IncoherentDataException("Sickness is not coherent with holidays");
            }
            if (workday.isAccidentAtWork()) {
                throw new IncoherentDataException("Sickness is not coherent with accident at work");
            }
            if (workday.getWorkingHours() > 0) {
                throw new IncoherentDataException("Sickness is not coherent with any working hours");
            }
            if (workday.getExtraHours() > 0) {
                throw new IncoherentDataException("Sickness is not coherent with any extra hours");
            }
            if (workday.getWorkPermitHours() > 0) {
                throw new IncoherentDataException("Sickness is not coherent with any work permit hours");
            }
        }
    }

    public Workday editWorkday(Workday workday) throws IncoherentDataException {

        Optional<Workday> result = workdayRepository.findById(workday.getId());

        if (result.isEmpty()) {
            throw new EntityNotFoundException("Can't find workday with id:" + workday.getId());
        }

        checkWorkdayCoherence(workday);
        return workdayRepository.save(workday);
    }

    public void deleteWorkday(Long workdayId) {

        Optional<Workday> result = workdayRepository.findById(workdayId);

        if (result.isEmpty()) {
            throw new EntityNotFoundException("Can't find workday with id:" + workdayId);
        }

        workdayRepository.deleteById(workdayId);
    }
}
