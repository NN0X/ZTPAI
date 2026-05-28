package com.example.taskmanager.security;

import com.example.taskmanager.domain.AppUser;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService
{
        private final UserRepository userRepository;

        public AppUserDetailsService(UserRepository userRepository)
        {
                this.userRepository = userRepository;
        }

        @Override
        public UserDetails loadUserByUsername(String username)
        {
                AppUser user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

                return User.builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .authorities("ROLE_" + user.getRole().name())
                                .build();
        }
}
