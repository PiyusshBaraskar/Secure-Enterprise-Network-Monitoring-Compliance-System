package com.enterprise.security.monitor.config;

import com.enterprise.security.monitor.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                // Static resources
                .antMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // H2 Console (disabled in production, allowed here for debugging fallback)
                .antMatchers("/h2-console/**").permitAll()
                
                // Granular permission check per module
                .antMatchers("/devices/add", "/devices/save", "/devices/edit/**", "/devices/delete/**").hasAuthority("MANAGE_DEVICES")
                .antMatchers("/devices/**").hasAnyAuthority("READ_DASHBOARD", "MANAGE_DEVICES")
                
                .antMatchers("/scans/run", "/scans/start").hasAuthority("RUN_SCANS")
                .antMatchers("/scans/**").hasAnyAuthority("RUN_SCANS", "READ_DASHBOARD")
                
                .antMatchers("/alerts/assign/**", "/alerts/resolve/**", "/alerts/status/**").hasAuthority("VIEW_ALERTS")
                .antMatchers("/alerts/**").hasAnyAuthority("VIEW_ALERTS", "READ_DASHBOARD")
                
                .antMatchers("/logs/export/**").hasAuthority("VIEW_LOGS")
                .antMatchers("/logs/**").hasAnyAuthority("VIEW_LOGS", "READ_DASHBOARD")
                
                .antMatchers("/compliance/run").hasAuthority("RUN_AUDITS")
                .antMatchers("/compliance/**").hasAnyAuthority("RUN_AUDITS", "READ_DASHBOARD")
                
                .antMatchers("/reports/generate/**").hasAuthority("GENERATE_REPORTS")
                .antMatchers("/reports/**").hasAnyAuthority("GENERATE_REPORTS", "READ_DASHBOARD")
                
                .antMatchers("/users/**").hasAuthority("MANAGE_USERS")
                
                // Dashboard and root URLs
                .antMatchers("/", "/dashboard").hasAnyAuthority("READ_DASHBOARD", "ROLE_ADMIN", "ROLE_ANALYST", "ROLE_AUDITOR")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            .and()
            .logout()
                .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            .and()
            .exceptionHandling()
                .accessDeniedPage("/403");

        // Disable CSRF and frame options for H2 Console
        http.csrf().ignoringAntMatchers("/h2-console/**");
        http.headers().frameOptions().sameOrigin();
    }
}
