package com.jaewa.timesheet.service.passwordtoken;

import com.jaewa.timesheet.exception.IncoherentDataException;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.PasswordResetToken;
import com.jaewa.timesheet.model.repository.PasswordResetTokenRepository;
import com.jaewa.timesheet.service.ApplicationUserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ApplicationUserService applicationUserService;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, ApplicationUserService applicationUserService) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.applicationUserService = applicationUserService;
    }

    public PasswordResetToken createToken(ApplicationUser user) {
        Optional<ApplicationUser> userByLoginInfo = applicationUserService.getByLoginInfo(user.getUsername());
        if (userByLoginInfo.isEmpty()) {
            throw new UsernameNotFoundException(String.format("User %s not found", user));
        }

        Long id = null;
        Optional<PasswordResetToken> byApplicationUserId = passwordResetTokenRepository.findByApplicationUserId(userByLoginInfo.get().getId());

        if (byApplicationUserId.isPresent()) {
            id = byApplicationUserId.get().getId();
        }

        PasswordResetToken token = PasswordResetToken.builder()
                .id(id)
                .token(UUID.randomUUID().toString())
                .expirationDate(LocalDateTime.now().plusHours(1))
                .applicationUser(user)
                .build();

        passwordResetTokenRepository.save(token);
        return token;
    }

    public PasswordResetToken validateToken(String token) throws IncoherentDataException {
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);

        if (passwordResetToken.isEmpty()) {
            throw new IncoherentDataException("There are no valid tokens");
        }

        if (passwordResetToken.get().getExpirationDate().isBefore(LocalDateTime.now())) {
            deleteToken(passwordResetToken.get());
            throw new IncoherentDataException("Token is expired, please repeat the procedure");
        }

        return passwordResetToken.get();
    }

    public void deleteToken(PasswordResetToken passwordResetToken) {
        passwordResetTokenRepository.delete(passwordResetToken);
    }

}

