package com.jaewa.timesheet.controller.mapper;

import com.jaewa.timesheet.controller.dto.SummaryDto;
import com.jaewa.timesheet.model.Summary;
import com.jaewa.timesheet.service.ApplicationUserService;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ApplicationUserService.class})
public interface SummaryMapper {

    SummaryDto toDto(Summary workday);

}
