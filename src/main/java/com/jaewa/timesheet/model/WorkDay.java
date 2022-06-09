package com.jaewa.timesheet.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@Table(name = "workday")
public class WorkDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "date", nullable = false)
    OffsetDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_user")
    ApplicationUser user;

    @Column(name = "working_hours", nullable = false)
    int workingHours;

    @Column(name = "holiday", nullable = false)
    boolean holiday;

    @Column(name = "sick", nullable = false)
    boolean sick;

    @Column(name = "permission_hours", nullable = false)
    int permissionHours;

}
