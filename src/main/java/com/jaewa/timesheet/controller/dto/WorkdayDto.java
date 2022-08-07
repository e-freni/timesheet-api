package com.jaewa.timesheet.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Builder
@Getter
@Setter
public class WorkdayDto {

    private Long id;

    private OffsetDateTime date;

    private Long usernameId;

    private int workingHours;

    private int extraHours;

    private int workPermitHours;

    private int funeralLeaveHours;

    private boolean holiday;

    private boolean sick;

    private boolean accidentAtWork;

    private String notes;

}
