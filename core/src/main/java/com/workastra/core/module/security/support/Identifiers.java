package com.workastra.core.module.security.support;

import com.workastra.core.module.security.repository.UserRepository;
import java.util.concurrent.Callable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class Identifiers {

    private final UserRepository userRepository;

    Identifiers(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Callable<Authentication> ofUsername(String username) {
        return () -> {
            var user = this.userRepository.findByUsername(username);

            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }

            return UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
        };
    }
}
