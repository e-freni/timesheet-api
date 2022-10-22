package com.jaewa.timesheet.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "workday")
public class Workday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_user", nullable = false)
    private ApplicationUser applicationUser;

    @Column(name = "working_hours", nullable = false)
    private int workingHours;

    @Column(name = "extra_hours", nullable = false)
    private int extraHours;

    @Column(name = "work_permit_hours", nullable = false)
    private int workPermitHours;

    @Column(name = "night_working_hours", nullable = false)
    private int nightWorkingHours;

    @Column(name = "funeral_leave_hours", nullable = false)
    private int funeralLeaveHours;

    @Column(name = "holiday", nullable = false)
    private boolean holiday;

    @Column(name = "sick", nullable = false)
    private boolean sick;

    @Column(name = "accident_at_work", nullable = false)
    private boolean accidentAtWork;

    @Column(name = "notes")
    private String notes;

}



