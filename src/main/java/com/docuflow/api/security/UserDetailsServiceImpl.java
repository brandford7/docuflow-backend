package com.docuflow.api.security;

import com.docuflow.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security calls this during authentication to load the user by email.
 *
 * loadUserByUsername() is called by DaoAuthenticationProvider at login time.
 * Our User entity implements UserDetails so we return it directly.
 * Only active (non-deleted) users are returned.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return userRepository.findActiveByEmail(email.toLowerCase().strip())
            .orElseThrow(() ->
                new UsernameNotFoundException("No active user found: " + email));
    }
}
