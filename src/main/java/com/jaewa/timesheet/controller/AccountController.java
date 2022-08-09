package com.jaewa.timesheet.controller;

import com.jaewa.timesheet.controller.dto.LoginDto;
import com.jaewa.timesheet.controller.dto.TokenDto;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.service.ApplicationUserService;
import com.jaewa.timesheet.service.MailService;
import com.jaewa.timesheet.service.token.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public AccountController(ApplicationUserService applicationUserService, MailService mailService, TokenService tokenService) {
        this.applicationUserService = applicationUserService;
        this.mailService = mailService;
        this.tokenService = tokenService;
    }

    @PostMapping("/account/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginDto loginDto) {
        return applicationUserService.getByLoginInfo(loginDto.getUsername())
                .map(
                        user -> {
                            if (!applicationUserService.isValidPassword(user, loginDto.getPassword())) {
                                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                            }
                            TokenDto tokenDto = TokenDto.builder()
                                    .jwt(tokenService.createToken(user))
                                    .build();
                            return ResponseEntity.ok(tokenDto);
                        }
                )
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
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
