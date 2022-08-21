package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


// Due to deprecation of WebSecurityConfigurerAdapter, see https://www.baeldung.com/spring-deprecated-websecurityconfigureradapter


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
// You can name this whatever you want. Spring will still find it using @EnableWebSecurity
public class MyWebSecurityConfig {
    
    
    // Before, we use configure(HttpSecurity http) and @Override.
    // Now, it returns a SecurityFilterChain, and annotate it with @Bean.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/", "/home").permitAll()
            .anyRequest().authenticated()
            .and().
        formLogin()
            .loginPage("/login")
            .permitAll()
            .and().
        logout()
            .permitAll();
        return http.build();
    }


    // Before, we use AuthenticationManagerBuilder to set our authentication context.
    // Now, just define a UserDetailsManager or UserDetailsService bean.
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        UserDetails user = User.withUsername("user")
            .password(passwordEncoder.encode("password"))
            .roles("USER")
            .build();
        UserDetails admin = User.withUsername("admin")
            .password(passwordEncoder.encode("password"))
            .roles("USER", "ADMIN")
            .build();
        manager.createUser(user);
        manager.createUser(admin);

        return manager;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
