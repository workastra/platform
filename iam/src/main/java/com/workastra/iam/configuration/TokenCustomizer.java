package com.workastra.iam.configuration;

import com.workastra.core.module.security.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

@Component
class TokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        Authentication authentication = context.getPrincipal();
        JwtClaimsSet.Builder claims = context.getClaims();

        if (authentication != null && authentication.getPrincipal() instanceof User userDetails) {
            // Override the default "sub" claim with the user's ID instead of username, which is more stable and less likely to change.
            claims.subject(userDetails.getId().toString());
        }
    }
}
