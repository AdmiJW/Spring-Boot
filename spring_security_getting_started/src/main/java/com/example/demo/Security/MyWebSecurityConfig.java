package com.example.demo.Security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


// Due to deprecation of WebSecurityConfigurerAdapter, 
// see https://www.baeldung.com/spring-deprecated-websecurityconfigureradapter


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
// You can name this whatever you want. Spring will still find it using @EnableWebSecurity
public class MyWebSecurityConfig {
    AuthenticationProvider a;
    
    // Before, we use configure(HttpSecurity http) and @Override.
    // Now, it returns a SecurityFilterChain, and annotate it with @Bean.
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/register").permitAll()
            .antMatchers("/who_am_i").permitAll()
            .antMatchers("/am_i_admin").hasAuthority("ADMIN")
            .antMatchers("/am_i_user").hasAuthority("USER")
            .and()
        .formLogin()
            .disable()
            // .loginPage("/login") 
            // .defaultSuccessUrl("/login_success", true)
            // .failureForwardUrl("/login_failure")
            // .loginProcessingUrl("/login/process")       
            // .usernameParameter("username")
            // .passwordParameter("password")
            // .permitAll()
            // .and()
        .logout()
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/logout_success")
            .permitAll()
            .and()
        // .exceptionHandling()
        //     .accessDeniedPage("/accessDenied")
        //     .and()
        .csrf()
            .disable()
        .rememberMe()
            .rememberMeParameter("rememberMe")
            .key("theKeyisHere")
            .tokenValiditySeconds(120);

        return http.build();
    }


    // Before, we use configure(AuthenticationManagerBuilder) to set our authentication context.
    // Now, just define a UserDetailsService bean. 
    @Bean
    public UserDetailsService myUserDetailsService() {
        return new MyUserDetailsService();
    }


    // The default password encoder for Spring Boot is DelegatingPasswordEncoder.
    // unless you choose to replace it by defining your own PasswordEncoder bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }



    // Registering AuthenticationManager as a bean enables us to use authenticationManager to 
    // perform authentication process in controllers.
    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
