package com.jaewa.timesheet.config;

import com.jaewa.timesheet.service.token.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
public class WebSecurityConfiguration {

    private final TokenService tokenService;

    public WebSecurityConfiguration(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    //TODO find a proper web security implementation
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.csrf().disable();

        http.antMatcher("/**")
                .addFilterBefore(new TokenFilter(tokenService), UsernamePasswordAuthenticationFilter.class);
        // @formatter:on
    }

    public void configure(WebSecurity web) {
        // @formatter:off
        web.ignoring()
                .antMatchers("/account/login");
        // @formatter:on
    }

}
