package com.jaewa.timesheet.service;

import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.model.repository.WorkdayRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;


@Service
public class WorkdayService {

    private final WorkdayRepository workdayRepository;

    private final ApplicationUserService applicationUserService;


    public WorkdayService(WorkdayRepository workdayRepository, ApplicationUserService applicationUserService) {
        this.workdayRepository = workdayRepository;
        this.applicationUserService = applicationUserService;
    }

    public List<Workday> findWorkdayByUser(String username) {

        return workdayRepository.findByApplicationUserUsername(username);
    }

    public Optional<Workday> findById(Long id) {
        return workdayRepository.findById(id);
    }

    public Workday addNewWorkday(Workday workday) {

        addUserToWorkday(workday);
        //TODO add detailed exceptions for each incongruent check
        checkUserSickness(workday);
        checkUserAccidentAtWork(workday);
        checkUserHoliday(workday);
        checkUserExtraHours(workday);
        checkUserWorkPermitHours(workday);
        checkUserNormalDayWork(workday);

        return workdayRepository.save(workday);
    }

    private void checkUserNormalDayWork(Workday workday) {
        if (workday.getWorkingHours() == 8) {
            if (workday.isSick()) {
                throw new RuntimeException();
            }
            if (workday.isHoliday()) {
                throw new RuntimeException();
            }
            if (workday.isAccidentAtWork()) {
                throw new RuntimeException();
            }
            if (workday.getWorkingHours() > 0) {
                throw new RuntimeException();
            }
            if (workday.getExtraHours() > 0) {
                throw new RuntimeException();
            }
            if (workday.getWorkPermitHours() > 0) {
                throw new RuntimeException();
            }
        }
    }

    private void checkUserWorkPermitHours(Workday workday) {
        if (workday.getWorkPermitHours() > 0 && workday.getWorkingHours() + workday.getWorkPermitHours() + workday.getFuneralLeaveHours() != 8) {
            throw new RuntimeException();
        }
    }

    private void checkUserExtraHours(Workday workday) {
        if (workday.getExtraHours() > 0 && workday.getWorkingHours() + workday.getWorkPermitHours() + workday.getFuneralLeaveHours() != 8) {
            throw new RuntimeException();
        }
    }


    private void checkUserHoliday(Workday workday) {
        if (workday.isHoliday()) {

            if (workday.isAccidentAtWork()) {
                throw new RuntimeException();
            }
            if (workday.isSick()) {
                throw new RuntimeException();
            }
            if (workday.getWorkingHours() > 0) {
                throw new RuntimeException();
            }
            if (workday.getExtraHours() > 0) {
                throw new RuntimeException();
            }
            if (workday.getWorkPermitHours() > 0) {
                throw new RuntimeException();
            }

        }
    }

    private void checkUserAccidentAtWork(Workday workday) {
        if (workday.isAccidentAtWork()) {

            if (workday.isHoliday()) {
                throw new RuntimeException();
            }
            if (workday.isSick()) {
                throw new RuntimeException();
            }
            if (workday.getWorkingHours() > 0) {
                throw new RuntimeException();
            }
            if (workday.getExtraHours() > 0) {
                throw new RuntimeException();
            }
            if (workday.getWorkPermitHours() > 0) {
                throw new RuntimeException();
            }

        }
    }

    private void checkUserSickness(Workday workday) {
        if (workday.isSick()) {

            if (workday.isHoliday()) {
                throw new RuntimeException();
            }
            if (workday.isAccidentAtWork()) {
                throw new RuntimeException();
            }
            if (workday.getWorkingHours() > 0) {
                throw new RuntimeException();
            }
            if (workday.getExtraHours() > 0) {
                throw new RuntimeException();
            }
            if (workday.getWorkPermitHours() > 0) {
                throw new RuntimeException();
            }

        }


    }

    private void addUserToWorkday(Workday workday) {
        Optional<ApplicationUser> user = applicationUserService.getById(workday.getApplicationUser().getId());

        if (user.isEmpty()) {
            throw new EntityNotFoundException();
        }

        workday.setApplicationUser(user.get());
    }

}
