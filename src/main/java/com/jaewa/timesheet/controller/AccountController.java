package com.jaewa.timesheet.controller;

import com.jaewa.timesheet.controller.dto.LoginDto;
import com.jaewa.timesheet.controller.dto.TokenDto;
import com.jaewa.timesheet.service.ApplicationUserService;
import com.jaewa.timesheet.service.token.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AccountController {

    private final ApplicationUserService applicationUserService;
    private final TokenService tokenService;

    public AccountController(ApplicationUserService applicationUserService, TokenService tokenService) {
        this.applicationUserService = applicationUserService;
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
}
