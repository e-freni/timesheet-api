package com.jaewa.timesheet.service;

import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.UserRole;
import com.jaewa.timesheet.model.repository.ApplicationUserRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class ApplicationUserService {
    private final ApplicationUserRepository applicationUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ApplicationUserService(ApplicationUserRepository applicationUserRepository, PasswordEncoder passwordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<ApplicationUser> findUsers(UserRole role, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id"));

        if (role != null) {
            return applicationUserRepository.findAll(Example.of(ApplicationUser.builder().role(role).build()), pageRequest);

        } else {
            return applicationUserRepository.findAll(pageRequest);

        }
    }

    public Optional<ApplicationUser> getById(Long id) {
        return applicationUserRepository.findById(id);
    }
    public ApplicationUser findById(Long id) {
        Optional<ApplicationUser> optionalApplicationUser = applicationUserRepository.findById(id);

        if(optionalApplicationUser.isEmpty()){
            throw new EntityNotFoundException("User doesn't exists");
        }

        return optionalApplicationUser.get();
    }

    public Optional<ApplicationUser> getByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }

        return applicationUserRepository.findOne(Example.of(ApplicationUser.builder().username(username.trim()).build()));
    }

    public Optional<ApplicationUser> getByLoginInfo(String usernameOrEmail) {
        if (StringUtils.isBlank(usernameOrEmail)) {
            return Optional.empty();
        }

        return applicationUserRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }

    public ApplicationUser addUser(ApplicationUser user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        return saveUser(user);
    }

    public ApplicationUser saveUser(ApplicationUser user) {
        return applicationUserRepository.save(user);
    }

    public void deleteUser(Long id) {
        applicationUserRepository.deleteById(id);
    }

    public void changePassword(String username, String newPassword) {
        getByUsername(username).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            applicationUserRepository.save(user);
        });
    }

    public boolean isValidPassword(ApplicationUser user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

}
