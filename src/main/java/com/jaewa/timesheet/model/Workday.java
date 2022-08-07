package com.jaewa.timesheet.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "workday")
public class Workday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private OffsetDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_user", nullable = false)
    private ApplicationUser applicationUser;

    @Column(name = "working_hours", nullable = false)
    private int workingHours;

    @Column(name = "extra_hours", nullable = false)
    private int extraHours;

    @Column(name = "work_permit_hours", nullable = false)
    private int workPermitHours;

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
