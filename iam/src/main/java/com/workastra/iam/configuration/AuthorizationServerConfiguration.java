package com.workastra.iam.configuration;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
public class AuthorizationServerConfiguration {

    @Bean
    RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
            Authentication auth = context.getAuthentication();
            Object principal = Objects.requireNonNull(auth.getPrincipal(), "Authentication principal must not be null");

            // Only one principal type for now, but switch is intentional.
            // Adding a new type later means just appending a new case, without touching or risking the existing logic.
            @SuppressWarnings("SwitchStatementWithTooFewBranches")
            Map<String, Object> claims = switch (principal) {
                case JwtAuthenticationToken jwt -> jwt.getToken().getClaims();
                default -> throw new IllegalStateException(
                    "Unsupported principal type: " + principal.getClass().getName()
                );
            };

            return new OidcUserInfo(claims);
        };

        http
            .oauth2AuthorizationServer((authorizationServer) -> {
                http.securityMatcher(authorizationServer.getEndpointsMatcher());

                authorizationServer.oidc((oidc) ->
                    oidc.userInfoEndpoint((userInfo) -> userInfo.userInfoMapper(userInfoMapper))
                );
            })
            .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
            // Redirect to the login page when not authenticated from the authorization endpoint
            .exceptionHandling((exceptions) ->
                exceptions.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
            );

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
            // Enable form login with default settings
            .formLogin(Customizer.withDefaults());
        // Enable OAuth2 federated identity login with default settings
        // .oauth2Login(Customizer.withDefaults());

        return http.build();
    }
}
