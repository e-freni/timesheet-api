package com.jaewa.timesheet.model.repository;

import com.jaewa.timesheet.model.Workday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkdayRepository extends JpaRepository<Workday, Long> {

    List<Workday> findByApplicationUserUsername(String username);

    Optional<Workday> findByDateAndApplicationUserUsername(LocalDate date, String username);
}
