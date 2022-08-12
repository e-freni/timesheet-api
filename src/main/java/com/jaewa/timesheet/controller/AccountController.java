package com.jaewa.timesheet.controller;

import com.jaewa.timesheet.controller.dto.ApplicationUserDto;
import com.jaewa.timesheet.controller.dto.LoginDto;
import com.jaewa.timesheet.controller.dto.TokenDto;
import com.jaewa.timesheet.controller.mapper.ApplicationUserMapper;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.service.ApplicationUserService;
import com.jaewa.timesheet.service.AuthorizationService;
import com.jaewa.timesheet.service.MailService;
import com.jaewa.timesheet.service.token.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@RestController
public class AccountController {

    private final ApplicationUserService applicationUserService;
    private final MailService mailService;
    private final TokenService tokenService;
    private final ApplicationUserMapper applicationUserMapper;

    public AccountController(ApplicationUserService applicationUserService, MailService mailService, TokenService tokenService, ApplicationUserMapper applicationUserMapper) {
        this.applicationUserService = applicationUserService;
        this.mailService = mailService;
        this.tokenService = tokenService;
        this.applicationUserMapper = applicationUserMapper;
    }

    @GetMapping("/account/info")
    public ResponseEntity<ApplicationUserDto> getAccount() {

        return applicationUserService.getById(AuthorizationService.getApplicationUserId())
                .map(applicationUserMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }



    @PostMapping("/account/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginDto loginDto) {
        Optional<ApplicationUser> user = applicationUserService.getByLoginInfo(loginDto.getUsername());

        if(user.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!applicationUserService.isValidPassword(user.get(), loginDto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        TokenDto tokenDto = TokenDto.builder()
                .token(tokenService.createToken(user.get()))
                .build();
        return ResponseEntity.ok(tokenDto);

    }

    @PostMapping("/account/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody String username) {
        Optional<ApplicationUser> user = applicationUserService.getByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        //TODO send by email a resetConfirmation link
        String randomPassword = UUID.randomUUID().toString();
        applicationUserService.changePassword(username, randomPassword);
        mailService.sendResetPasswordEmail(user.get(), randomPassword);
        return ResponseEntity.ok().build();

    }
}
