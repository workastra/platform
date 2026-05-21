package com.workastra.core.module.security.support;

import java.util.concurrent.Callable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityContextTemplate {

    private final Authentication authentication;

    private SecurityContextTemplate(Authentication authentication) {
        this.authentication = authentication;
    }

    public static SecurityContextTemplate use(Callable<Authentication> authentication) {
        try {
            return new SecurityContextTemplate(authentication.call());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void run(Runnable runnable) {
        SecurityContext previous = SecurityContextHolder.getContext();

        try {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            SecurityContextHolder.setContext(context);
            context.setAuthentication(this.authentication);
            runnable.run();
        } finally {
            SecurityContextHolder.setContext(previous);
        }
    }
}
