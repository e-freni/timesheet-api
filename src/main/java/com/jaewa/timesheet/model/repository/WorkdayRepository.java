package com.jaewa.timesheet.model.repository;

import com.jaewa.timesheet.model.Workday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkdayRepository extends JpaRepository<Workday, Long> {

    List<Workday> findByApplicationUserUsername(String username);
}
