package com.jaewa.timesheet.service;

import com.jaewa.timesheet.exception.IncoherentDataException;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.model.repository.WorkdayRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;


@Service
public class WorkdayService {

    private final WorkdayRepository workdayRepository;


    public WorkdayService(WorkdayRepository workdayRepository) {
        this.workdayRepository = workdayRepository;
    }

    public List<Workday> findWorkdayByUser(String username) {

        return workdayRepository.findByApplicationUserUsername(username);
    }

    public Optional<Workday> findById(Long id) {
        return workdayRepository.findById(id);
    }

    public Workday addNewWorkday(Workday workday) throws IncoherentDataException {

        checkDateAlreadyLogged(workday);
        checkWorkdayCoherence(workday);
        return workdayRepository.save(workday);
    }

    private void checkWorkdayCoherence(Workday workday) throws IncoherentDataException {
        checkUserSickness(workday);
        checkUserAccidentAtWork(workday);
        checkUserHoliday(workday);
        checkUserExtraHours(workday);
        checkUserWorkPermitHours(workday);
        checkUserNormalDayWork(workday);
    }

    private void checkDateAlreadyLogged(Workday workday) throws IncoherentDataException {
        Optional<Workday> result = workdayRepository.findByDateAndApplicationUserUsername(workday.getDate(), workday.getApplicationUser().getUsername());

        if (result.isPresent()){
            throw new IncoherentDataException(String.format("This day %s has already been logged", workday.getDate()));
        }

    }

    private void checkUserNormalDayWork(Workday workday) throws IncoherentDataException {
        if (workday.getWorkingHours() == 8) {
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
        if (workday.getWorkingHours() != 8 && !workday.isSick() && !workday.isHoliday() && !workday.isAccidentAtWork()
                && workday.getWorkPermitHours() == 0 && workday.getFuneralLeaveHours() == 0) {
            throw new IncoherentDataException("There are no conditions to log less than 8 working hours");
        }
    }

    private void checkUserWorkPermitHours(Workday workday) throws IncoherentDataException {
        if (workday.getWorkPermitHours() > 0 && workday.getWorkingHours() + workday.getWorkPermitHours() + workday.getFuneralLeaveHours() != 8) {
            throw new IncoherentDataException("Permit hours are not coherent with others hours");
        }
    }

    private void checkUserExtraHours(Workday workday) throws IncoherentDataException {
        if (workday.getExtraHours() > 0 && workday.getWorkingHours() + workday.getWorkPermitHours() + workday.getFuneralLeaveHours() != 8) {
            throw new IncoherentDataException("Extra hours are not coherent with others hours");
        }
    }


    private void checkUserHoliday(Workday workday) throws IncoherentDataException {
        if (workday.isHoliday()) {
            if (workday.isAccidentAtWork()) {
                throw new IncoherentDataException("Holiday is not coherent with accident at work");
            }
            if (workday.isSick()) {
                throw new IncoherentDataException("Holiday is not coherent with sickness");
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

    private void checkUserAccidentAtWork(Workday workday) throws IncoherentDataException {
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

    private void checkUserSickness(Workday workday) throws IncoherentDataException {
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

    public void deleteWorkday(Workday workday) {

        Optional<Workday> result = workdayRepository.findById(workday.getId());

        if (result.isEmpty()) {
            throw new EntityNotFoundException("Can't find workday with id:" + workday.getId());
        }

        workdayRepository.deleteById(workday.getId());
    }
}
