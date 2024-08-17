package com.jaewa.timesheet.service.passwordtoken;

import com.jaewa.timesheet.exception.IncoherentDataException;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.PasswordResetToken;
import com.jaewa.timesheet.model.repository.PasswordResetTokenRepository;
import com.jaewa.timesheet.service.ApplicationUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetTokenServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private ApplicationUserService applicationUserService;

    @InjectMocks
    private PasswordResetTokenService passwordResetTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createTokenShouldCreateNewTokenForUser() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setUsername("testuser");

        when(applicationUserService.getByLoginInfo(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findByApplicationUserId(user.getId())).thenReturn(Optional.empty());

        PasswordResetToken token = passwordResetTokenService.createToken(user);

        assertNotNull(token);
        assertEquals(user, token.getApplicationUser());
        assertNotNull(token.getToken());
        assertTrue(token.getExpirationDate().isAfter(LocalDateTime.now()));

        verify(passwordResetTokenRepository, times(1)).save(token);
    }

    @Test
    void createTokenShouldThrowUsernameNotFoundExceptionIfUserNotFound() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("nonexistentuser");

        when(applicationUserService.getByLoginInfo(user.getUsername())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> passwordResetTokenService.createToken(user));
    }

    @Test
    void validateTokenShouldReturnTokenIfValid() throws IncoherentDataException {
        String tokenString = UUID.randomUUID().toString();
        ApplicationUser user = new ApplicationUser();
        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenString)
                .expirationDate(LocalDateTime.now().plusHours(1))
                .applicationUser(user)
                .build();

        when(passwordResetTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        PasswordResetToken result = passwordResetTokenService.validateToken(tokenString);

        assertNotNull(result);
        assertEquals(tokenString, result.getToken());
    }

    @Test
    void validateTokenShouldThrowIncoherentDataExceptionIfTokenNotFound() {
        String tokenString = UUID.randomUUID().toString();

        when(passwordResetTokenRepository.findByToken(tokenString)).thenReturn(Optional.empty());

        assertThrows(IncoherentDataException.class, () -> passwordResetTokenService.validateToken(tokenString));
    }

    @Test
    void validateTokenShouldThrowIncoherentDataExceptionIfTokenIsExpired() {
        String tokenString = UUID.randomUUID().toString();
        ApplicationUser user = new ApplicationUser();
        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenString)
                .expirationDate(LocalDateTime.now().minusHours(1))
                .applicationUser(user)
                .build();

        when(passwordResetTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        assertThrows(IncoherentDataException.class, () -> passwordResetTokenService.validateToken(tokenString));

        verify(passwordResetTokenRepository, times(1)).delete(token);
    }

    @Test
    void deleteTokenShouldDeleteToken() {
        PasswordResetToken token = new PasswordResetToken();

        passwordResetTokenService.deleteToken(token);

        verify(passwordResetTokenRepository, times(1)).delete(token);
    }
}
