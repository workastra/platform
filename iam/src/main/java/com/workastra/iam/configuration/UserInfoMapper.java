package com.workastra.iam.configuration;

import com.workastra.core.module.security.model.User;
import com.workastra.core.module.security.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
class UserInfoMapper implements Function<OidcUserInfoAuthenticationContext, OidcUserInfo> {

    private final UserRepository userRepository;

    UserInfoMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUserInfo apply(OidcUserInfoAuthenticationContext context) {
        Authentication auth = context.getAuthentication();
        Object principal = Objects.requireNonNull(auth.getPrincipal(), "Authentication principal must not be null");

        // Only one principal type for now, but switch is intentional.
        // Adding a new type later means just appending a new case, without touching or  risking the existing logic.
        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        Map<String, Object> info = switch (principal) {
            case JwtAuthenticationToken jwt -> this.buildUserInfoClaims(jwt);
            default -> throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getName());
        };

        return new OidcUserInfo(info);
    }

    private Map<String, Object> buildUserInfoClaims(JwtAuthenticationToken authentication) {
        User user = this.userRepository.findById(UUID.fromString(authentication.getName())).orElseThrow();
        Map<String, Object> customClaims = new HashMap<>();

        customClaims.put(StandardClaimNames.SUB, user.getId());
        customClaims.put(StandardClaimNames.PREFERRED_USERNAME, user.getUsername());
        customClaims.put(StandardClaimNames.GENDER, user.getGender());
        customClaims.put(StandardClaimNames.EMAIL, user.getEmail());
        customClaims.put(StandardClaimNames.EMAIL_VERIFIED, user.isEmailVerified());
        customClaims.put(StandardClaimNames.NAME, user.getFullName());

        if (user.getGivenName() != null) {
            customClaims.put(StandardClaimNames.GIVEN_NAME, user.getGivenName());
        }

        if (user.getFamilyName() != null) {
            customClaims.put(StandardClaimNames.FAMILY_NAME, user.getFamilyName());
        }

        if (user.getMiddleName() != null) {
            customClaims.put(StandardClaimNames.MIDDLE_NAME, user.getMiddleName());
        }

        customClaims.put(StandardClaimNames.LOCALE, user.getLocale().toLanguageTag());
        customClaims.put(StandardClaimNames.ZONEINFO, user.getTimezoneId().getId());
        customClaims.put(StandardClaimNames.UPDATED_AT, user.getUpdatedAt().getEpochSecond());

        return customClaims;
    }
}
