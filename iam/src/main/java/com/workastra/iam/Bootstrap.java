package com.workastra.iam;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.security.oauth2.server.authorization.autoconfigure.servlet.OAuth2AuthorizationServerProperties;
import org.springframework.integration.support.locks.DistributedLock;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;

@Component
public class Bootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationServerProperties properties;
    private final PasswordEncoder passwordEncoder;
    private final LockRegistry<DistributedLock> lockRegistry;

    public Bootstrap(
        LockRegistry<DistributedLock> lockRegistry,
        RegisteredClientRepository registeredClientRepository,
        OAuth2AuthorizationServerProperties properties,
        PasswordEncoder passwordEncoder
    ) {
        this.lockRegistry = lockRegistry;
        this.registeredClientRepository = registeredClientRepository;
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException, TimeoutException {
        this.lockRegistry.executeLocked("iam.migration", Duration.ofSeconds(30), () -> {
            logger.info("Acquired lock for client registration migration");

            var clients = this.properties.getClient();

            logger.info("Upserting {} client(s) from application properties", clients.size());

            for (var client : clients.entrySet()) {
                var id = client.getKey();
                var value = client.getValue();

                logger.info("Registering client with id: {}", id);

                var registration = value.getRegistration();
                RegisteredClient registeredClient = RegisteredClient.withId(id)
                    .clientName(registration.getClientName())
                    .clientId(registration.getClientId())
                    .clientSecret(
                        registration.getClientSecret() == null
                            ? ""
                            : this.passwordEncoder.encode(registration.getClientSecret())
                    )
                    .clientAuthenticationMethods(methods -> {
                        methods.clear();
                        registration
                            .getClientAuthenticationMethods()
                            .forEach(method -> methods.add(ClientAuthenticationMethod.valueOf(method)));
                    })
                    .authorizationGrantTypes(types -> {
                        types.clear();
                        registration
                            .getAuthorizationGrantTypes()
                            .forEach(type -> types.add(new AuthorizationGrantType(type)));
                    })
                    .redirectUris(uris -> {
                        uris.clear();
                        uris.addAll(registration.getRedirectUris());
                    })
                    .scopes(scopes -> {
                        scopes.clear();
                        scopes.addAll(registration.getScopes());
                    })
                    .postLogoutRedirectUris(uris -> {
                        uris.clear();
                        uris.addAll(registration.getPostLogoutRedirectUris());
                    })
                    .clientSettings(
                        ClientSettings.builder()
                            .requireAuthorizationConsent(value.isRequireAuthorizationConsent())
                            .requireProofKey(value.isRequireProofKey())
                            .build()
                    )
                    .build();

                this.registeredClientRepository.save(registeredClient);

                logger.info("Client with id: {} registered successfully", registration.getClientId());
            }
        });
    }
}
