package com.example.demo.Controller;

import java.util.Collections;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.Entities.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Security.MyUserDetails;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;


    @PostMapping("/login")
    public ResponseEntity<Object> loginPostRoute(
        @RequestBody User user
    ) { 
        Authentication a = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

        try {
            a = authenticationManager.authenticate(a);
            SecurityContextHolder.getContext().setAuthentication(a);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return ResponseEntity.ok( Collections.singletonMap("message", "Login successful") );
    }
    

    @RequestMapping("/logout_success")
    public ResponseEntity<Object> logoutSuccessRoute() {
        return ResponseEntity.ok( Collections.singletonMap("message", "Logout successful") );
    }

    

    @PostMapping("/register")
    public ResponseEntity<Object> registerRoute(
        @RequestBody User user
    ) {
        if ( user.getId() != null && userRepository.existsById( user.getId() ) )
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
        
        // Don't forget that password need to be stored hashed
        user.setPassword( passwordEncoder.encode( user.getPassword() ) );
        userRepository.save(user);
        return ResponseEntity.ok( Collections.singletonMap("message", "Registration successful. Please login") );
    }


    @GetMapping("/who_am_i")
    public ResponseEntity<Object> whoAmIRoute() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if ( !(principal instanceof MyUserDetails) )
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        return ResponseEntity.ok( ( (MyUserDetails)principal).getUser() );
    }


    @GetMapping("/am_i_admin")
    public ResponseEntity<Object> amIAdminRoute() {
        return ResponseEntity.ok( Collections.singletonMap("message", "You are admin") );
    }

    @GetMapping("/am_i_user")
    public ResponseEntity<Object> amIUserRoute() {
        return ResponseEntity.ok( Collections.singletonMap("message", "You are user") );
    }
}
