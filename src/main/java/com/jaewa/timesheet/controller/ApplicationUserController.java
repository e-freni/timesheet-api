package com.jaewa.timesheet.controller;

import com.jaewa.timesheet.controller.dto.ApplicationUserDto;
import com.jaewa.timesheet.controller.mapper.ApplicationUserMapper;
import com.jaewa.timesheet.exception.UnauthorizedException;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.UserRole;
import com.jaewa.timesheet.service.ApplicationUserService;
import com.jaewa.timesheet.service.AuthorizationService;
import com.jaewa.timesheet.service.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class ApplicationUserController {

    private final ApplicationUserMapper applicationUserMapper;
    private final ApplicationUserService applicationUserService;
    private final MailService mailService;

    public ApplicationUserController(ApplicationUserService applicationUserService, ApplicationUserMapper applicationUserMapper, MailService mailService) {
        this.applicationUserService = applicationUserService;
        this.applicationUserMapper = applicationUserMapper;
        this.mailService = mailService;
    }

    @GetMapping("/user")
    @RolesAllowed({"ADMIN"})
    public ResponseEntity<List<ApplicationUserDto>> findUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        return ResponseEntity.ok(
                applicationUserService.findUsers(role, page, size)
                        .stream()
                        .map(applicationUserMapper::toDTO)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/user/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public ResponseEntity<ApplicationUserDto> getUser(@PathVariable Long id) throws UnauthorizedException {
        AuthorizationService.checkUserIsAuthorized(id);
        return applicationUserService.getById(id)
                .map(user -> ResponseEntity.ok(applicationUserMapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/user/new")
    @RolesAllowed("ADMIN")
    public ResponseEntity<ApplicationUserDto> addUser(@RequestBody ApplicationUserDto applicationUserDto) throws MailSendException, UnauthorizedException {
        ApplicationUser user = new ApplicationUser();
        applicationUserMapper.toModel(applicationUserDto, user);

        String randomPassword = UUID.randomUUID().toString();
        ApplicationUser newUser = applicationUserService.addUser(user, randomPassword);
        mailService.sendActivationEmail(newUser, randomPassword);

        return ResponseEntity.ok(applicationUserMapper.toDTO(newUser));
    }

    @PutMapping("/user/edit")
    @RolesAllowed({"ADMIN", "USER"})
    public ResponseEntity<ApplicationUserDto> editUser(@RequestBody ApplicationUserDto applicationUserDto) throws UnauthorizedException {
        AuthorizationService.checkUserIsAuthorized(applicationUserDto.getId());
        return applicationUserService.getById(applicationUserDto.getId())
                .map(user -> {
                    applicationUserMapper.toModel(applicationUserDto, user);
                    return ResponseEntity.ok(applicationUserMapper.toDTO(applicationUserService.editUser(user)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/user/delete/{id}")
    @RolesAllowed("ADMIN")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        return applicationUserService.getById(id)
                .map(user -> {
                    applicationUserService.deleteUser(id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
