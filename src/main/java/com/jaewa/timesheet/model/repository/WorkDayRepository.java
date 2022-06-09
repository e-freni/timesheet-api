package com.jaewa.timesheet.model.repository;

import com.jaewa.timesheet.model.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkDayRepository  extends JpaRepository<WorkDay, Long> {



}
