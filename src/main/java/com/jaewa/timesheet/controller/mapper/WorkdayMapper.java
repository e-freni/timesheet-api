package com.jaewa.timesheet.controller.mapper;

import com.jaewa.timesheet.controller.dto.WorkdayDto;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.service.ApplicationUserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ApplicationUserService.class})
public interface WorkdayMapper {

    @Mapping(source = "applicationUser.id",target = "usernameId")
    WorkdayDto toDto(Workday workday);

    @Mapping(source = "usernameId", target = "applicationUser")
    Workday toModel(WorkdayDto dto);

}
