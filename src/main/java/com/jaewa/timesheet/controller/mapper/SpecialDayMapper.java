package com.jaewa.timesheet.controller.mapper;

import com.jaewa.timesheet.controller.dto.SpecialDayDto;
import com.jaewa.timesheet.model.specialday.SpecialDay;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpecialDayMapper {

    SpecialDayDto toDto(SpecialDay specialDay);

    List<SpecialDayDto> toDtos(List<SpecialDay> specialDays);
}
