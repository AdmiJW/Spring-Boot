package com.example.demo.Security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.example.demo.Entities.User;



@Getter
@Setter
@AllArgsConstructor
public class MyUserDetails implements UserDetails {

    // Using this and @Getter, I can get the User object
    private User user;
    

    // These are the methods that has to be overridden by UserDetails interface.
    // If you do not want to implement your own UserDetails, you may use org.springframework.security.core.userdetails.User class.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList( new SimpleGrantedAuthority( user.getAuthority() ) );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
