package com.jaewa.timesheet.model.repository;

import com.jaewa.timesheet.model.Workday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkdayRepository extends JpaRepository<Workday, Long> {

    @Query(value = "SELECT w from Workday w where w.applicationUser.username = ?1 and w.date >= ?2 and w.date <= ?3")
    List<Workday> findByApplicationUserUsernameAndDate(String username, LocalDate fromDate, LocalDate toDate);

    Optional<Workday> findByDateAndApplicationUserUsername(LocalDate date, String username);
}
