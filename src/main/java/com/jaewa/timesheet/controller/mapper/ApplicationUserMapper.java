package com.jaewa.timesheet.controller.mapper;

import com.jaewa.timesheet.controller.dto.ApplicationUserDto;
import com.jaewa.timesheet.model.ApplicationUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ApplicationUserMapper {

    @Mapping(target = "password", ignore = true)
    ApplicationUserDto toDTO(ApplicationUser applicationUser);

    @Mapping(target = "password", ignore = true)
    void toModel(ApplicationUserDto dto, @MappingTarget ApplicationUser applicationUser);

}
