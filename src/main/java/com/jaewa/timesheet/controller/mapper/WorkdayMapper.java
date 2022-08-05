package com.jaewa.timesheet.controller.mapper;

import com.jaewa.timesheet.controller.dto.WorkdayDto;
import com.jaewa.timesheet.model.Workday;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkdayMapper {

    @Mapping(target = "applicationUser", ignore = true)
    WorkdayDto toDto(Workday workday);

    @Mapping(target = "applicationUser", ignore = true)
    Workday toModel(WorkdayDto dto);
}
