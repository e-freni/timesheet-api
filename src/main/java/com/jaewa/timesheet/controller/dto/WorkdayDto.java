package com.jaewa.timesheet.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class WorkdayDto {

    private Long id;

    private LocalDate date;

    private Long userId;

    private int workingHours;

    private int extraHours;

    private int workPermitHours;

    private int nightWorkingHours;

    private boolean funeralLeave;

    private boolean holiday;

    private boolean sick;

    private boolean accidentAtWork;

    private String notes;

}
