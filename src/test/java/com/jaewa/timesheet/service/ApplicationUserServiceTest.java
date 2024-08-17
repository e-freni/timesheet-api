package com.jaewa.timesheet.service;

import com.jaewa.timesheet.exception.IncoherentDataException;
import com.jaewa.timesheet.exception.UnauthorizedException;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.UserRole;
import com.jaewa.timesheet.model.repository.ApplicationUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplicationUserServiceTest {

    @Mock
    private ApplicationUserRepository applicationUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ApplicationUserService applicationUserService;

    private ApplicationUser user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = ApplicationUser.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@example.com")
                .password("password")
                .role(UserRole.USER)
                .build();
    }

    @Test
    void testFindUsersByRole() {
        when(applicationUserRepository.findAll(any(), any(PageRequest.class))).thenReturn(Page.empty());

        Page<ApplicationUser> result = applicationUserService.findUsers(UserRole.USER, 0, 10);

        assertNotNull(result);
        verify(applicationUserRepository, times(1)).findAll(any(), any(PageRequest.class));
    }

    @Test
    void testFindUsersWithoutRole() {
        when(applicationUserRepository.findAll(any(PageRequest.class))).thenReturn(Page.empty());

        Page<ApplicationUser> result = applicationUserService.findUsers(null, 0, 10);

        assertNotNull(result);
        verify(applicationUserRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void testGetById() {
        when(applicationUserRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<ApplicationUser> result = applicationUserService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(applicationUserRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByIdThrowsEntityNotFoundException() {
        when(applicationUserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> applicationUserService.findById(1L));
        verify(applicationUserRepository, times(1)).findById(1L);
    }

    @Test
    void testGetByUsername() {
        when(applicationUserRepository.findOne(any())).thenReturn(Optional.of(user));

        Optional<ApplicationUser> result = applicationUserService.getByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(applicationUserRepository, times(1)).findOne(any());
    }

    @Test
    void testGetByUsernameReturnsEmpty() {
        when(applicationUserRepository.findOne(any())).thenReturn(Optional.empty());

        Optional<ApplicationUser> result = applicationUserService.getByUsername("nonexistentuser");

        assertTrue(result.isEmpty());
        verify(applicationUserRepository, times(1)).findOne(any());
    }

    @Test
    void testAddUserThrowsUnauthorizedExceptionWhenUserExists() {
        when(applicationUserRepository.findOne(any())).thenReturn(Optional.of(user));

        ApplicationUser newUser = ApplicationUser.builder().username("testuser").email("testuser@example.com").build();

        assertThrows(UnauthorizedException.class, () -> applicationUserService.addUser(newUser, "password"));
        verify(applicationUserRepository, times(1)).findOne(any());
    }

    @Test
    void testAddUserSuccessfully() throws UnauthorizedException {
        when(applicationUserRepository.findOne(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedpassword");
        when(applicationUserRepository.save(any(ApplicationUser.class))).thenReturn(user);

        ApplicationUser newUser = ApplicationUser.builder().username("newuser").email("newuser@example.com").build();

        ApplicationUser savedUser = applicationUserService.addUser(newUser, "password");

        assertEquals(user, savedUser);
        verify(applicationUserRepository, times(1)).save(any(ApplicationUser.class));
    }

    @Test
    void testEditUserSuccessfully() {
        when(applicationUserRepository.save(any(ApplicationUser.class))).thenReturn(user);

        ApplicationUser updatedUser = applicationUserService.editUser(user);

        assertEquals(user, updatedUser);
        verify(applicationUserRepository, times(1)).save(any(ApplicationUser.class));
    }

    @Test
    void testDeleteUser() {
        doNothing().when(applicationUserRepository).deleteById(1L);

        applicationUserService.deleteUser(1L);

        verify(applicationUserRepository, times(1)).deleteById(1L);
    }

    @Test
    void testChangePasswordSuccessfully() {
        when(applicationUserRepository.findOne(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newencodedpassword");

        applicationUserService.changePassword("testuser", "newpassword");

        assertEquals("newencodedpassword", user.getPassword());
        verify(applicationUserRepository, times(1)).save(user);
    }

    @Test
    void testIsValidPassword() {
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        boolean isValid = applicationUserService.isValidPassword(user, "password");

        assertTrue(isValid);
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    void testCheckPasswordMatchSuccessfully() throws IncoherentDataException {
        when(applicationUserRepository.findOne(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> applicationUserService.checkPasswordMatch("testuser", "password"));
    }

    @Test
    void testCheckPasswordMatchThrowsIncoherentDataExceptionWhenPasswordDoesNotMatch() {
        when(applicationUserRepository.findOne(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(IncoherentDataException.class, () -> applicationUserService.checkPasswordMatch("testuser", "wrongpassword"));
    }

    @Test
    void testCheckPasswordMatchThrowsIncoherentDataExceptionWhenUserNotFound() {
        when(applicationUserRepository.findOne(any())).thenReturn(Optional.empty());

        assertThrows(IncoherentDataException.class, () -> applicationUserService.checkPasswordMatch("nonexistentuser", "password"));
    }
}
