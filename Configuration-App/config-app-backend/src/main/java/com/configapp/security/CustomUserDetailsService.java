package com.configapp.security;

import com.configapp.model.Admin;
import com.configapp.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())
                .authorities("ROLE_ADMIN")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!admin.getActive())
                .build();
    }

    public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
        Admin admin = adminRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())
                .authorities("ROLE_ADMIN")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!admin.getActive())
                .build();
    }
}
