package com.jaewa.timesheet.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Summary {

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
