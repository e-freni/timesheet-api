package com.jaewa.timesheet.controller.mapper;

import com.jaewa.timesheet.controller.dto.ApplicationUserDto;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.repository.ApplicationUserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ApplicationUserMapper {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Mapping(target = "password", ignore = true)
    public abstract ApplicationUserDto toDTO(ApplicationUser applicationUser);

    @Mapping(target = "password", ignore = true)
    public abstract void toModel(ApplicationUserDto dto, @MappingTarget ApplicationUser applicationUser);

}
