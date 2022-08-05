package com.jaewa.timesheet.service;

import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.model.repository.WorkdayRepository;
import org.springframework.stereotype.Service;

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

    public Optional<Workday> findById(Long id){
        return workdayRepository.findById(id);
    }

}
