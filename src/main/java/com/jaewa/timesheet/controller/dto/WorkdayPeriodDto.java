package com.jaewa.timesheet.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class WorkdayPeriodDto {
    private String username;
    private LocalDate fromDate;
    private LocalDate toDate;
}
