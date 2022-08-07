package com.jaewa.timesheet.controller.mapper;

import com.jaewa.timesheet.controller.dto.WorkdayDto;
import com.jaewa.timesheet.model.Workday;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkdayMapper {

    @Mapping(source = "applicationUser.id",target = "usernameId")
    WorkdayDto toDto(Workday workday);

    //FIXME find a way to map applicationUser without service usage
    @Mapping(source = "usernameId", target = "applicationUser.id")
    Workday toModel(WorkdayDto dto);
}
