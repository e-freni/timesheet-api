package com.jaewa.timesheet.config;

import com.jaewa.timesheet.service.token.TokenService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TokenFilter extends GenericFilterBean {

    public static final int NOT_FOUND_ERROR_CODE = 401;
    public static final int BEGIN_INDEX = 7;
    private final TokenService tokenService;

    public TokenFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String jwt = getTokenFromHeader(httpRequest);
        if (StringUtils.isBlank(jwt)) {
            httpResponse.sendError(NOT_FOUND_ERROR_CODE);
            return;
        }

        Authentication authentication = tokenService.validateTokenAndGetAuthentication(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }

    private static String getTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.startsWith(bearerToken, "Bearer ")) {
            return bearerToken.substring(BEGIN_INDEX);
        }

        return null;
    }

}

