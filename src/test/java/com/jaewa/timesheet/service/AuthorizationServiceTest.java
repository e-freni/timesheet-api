package com.jaewa.timesheet.service;

import com.jaewa.timesheet.exception.UnauthorizedException;
import com.jaewa.timesheet.service.token.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationServiceTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private Token token;

    @BeforeEach
    void setUp() {
        // Inizializza i mock manualmente
        MockitoAnnotations.initMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(token);
    }

    @Test
    void getApplicationUserId_shouldReturnUserId() {
        Long expectedUserId = 123L;
        when(token.getApplicationUserId()).thenReturn(expectedUserId);

        Long actualUserId = AuthorizationService.getApplicationUserId();

        assertEquals(expectedUserId, actualUserId);
        verify(token, times(1)).getApplicationUserId();
    }

    @Test
    void checkUserIsAuthorized_shouldNotThrowExceptionForAuthorizedUser() {
        Long userId = 123L;
        when(token.getApplicationUserId()).thenReturn(userId);

        assertDoesNotThrow(() -> AuthorizationService.checkUserIsAuthorized(userId));
    }

    @Test
    void checkUserIsAuthorized_shouldThrowExceptionForUnauthorizedUser() {
        Long authorizedUserId = 123L;
        Long unauthorizedUserId = 456L;
        when(token.getApplicationUserId()).thenReturn(authorizedUserId);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            AuthorizationService.checkUserIsAuthorized(unauthorizedUserId);
        });

        assertEquals("Current user withId 456 is unauthorized to access this resource", exception.getMessage());
    }
}
