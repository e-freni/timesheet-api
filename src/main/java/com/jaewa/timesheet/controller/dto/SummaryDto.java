package com.jaewa.timesheet.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SummaryDto {

    private int loggedHours;

    private int toLogHours;

    private int loggedPermitHours;

    private int loggedHolidaysHours;

    private int loggedExtraHours;

    private int loggedNightHours;

    private int loggedSicknessHours;

    private int loggedAccidentAtWorkHours;

    private int loggedFuneralLeaveHours;

}
