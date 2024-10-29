package com.transactionapi.transactionapi.config.security;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.transactionapi.transactionapi.models.AccountModel;
import com.transactionapi.transactionapi.services.AccountService;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountModel model = accountService.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new User(model.getEmail(), model.getPassword(), new ArrayList<>());
    }

}
