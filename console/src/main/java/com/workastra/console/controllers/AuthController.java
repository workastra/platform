package com.workastra.console.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/api/v1/auth/me")
    public Authentication me(Authentication authentication) {
        return authentication;
    }
}
