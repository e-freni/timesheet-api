package com.jaewa.timesheet.service;

import com.jaewa.timesheet.exception.UnauthorizedException;
import com.jaewa.timesheet.model.UserRole;
import com.jaewa.timesheet.service.token.Token;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    private AuthorizationService() {
    }

    public static Long getApplicationUserId() {
        return ((Token) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getApplicationUserId();
    }

    public static void checkUserIsAuthorized(Long userId) throws UnauthorizedException {
        if(isAdmin()){
            return;
        }

        if (!getApplicationUserId().equals(userId)){
            throw new UnauthorizedException(String.format("Current user withId %s is unauthorized to access this resource", userId));
        }
    }

    public static boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> UserRole.ADMINISTRATOR.getAuthority().equals(a.getAuthority()));
    }

}
