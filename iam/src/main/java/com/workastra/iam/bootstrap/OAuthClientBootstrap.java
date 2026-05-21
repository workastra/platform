package com.workastra.iam.bootstrap;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.security.oauth2.server.authorization.autoconfigure.servlet.OAuth2AuthorizationServerProperties;
import org.springframework.core.annotation.Order;
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
@Order(1)
public class OAuthClientBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(OAuthClientBootstrap.class);
    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final LockRegistry<DistributedLock> lockRegistry;
    private final Map<String, OAuth2AuthorizationServerProperties.Client> clients;

    public OAuthClientBootstrap(
        LockRegistry<DistributedLock> lockRegistry,
        RegisteredClientRepository registeredClientRepository,
        OAuth2AuthorizationServerProperties properties,
        PasswordEncoder passwordEncoder
    ) {
        this.lockRegistry = lockRegistry;
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
        this.clients = properties.getClient();
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException, TimeoutException {
        this.lockRegistry.executeLocked("iam.migration", Duration.ofSeconds(30), () -> {
            logger.info("Acquired lock for client registration migration");

            logger.info("Upserting {} client(s) from application properties", this.clients.size());

            for (var client : this.clients.entrySet()) {
                var id = client.getKey();
                var value = client.getValue();

                logger.info("Registering client with id: {}", id);
                this.registeredClientRepository.save(this.toRegisteredClient(id, value));
                logger.info("Client with id: {} registered successfully", value.getRegistration().getClientId());
            }
        });
    }

    private RegisteredClient toRegisteredClient(String id, OAuth2AuthorizationServerProperties.Client client) {
        var registration = client.getRegistration();

        return RegisteredClient.withId(id)
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
                registration.getAuthorizationGrantTypes().forEach(type -> types.add(new AuthorizationGrantType(type)));
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
                    .requireAuthorizationConsent(client.isRequireAuthorizationConsent())
                    .requireProofKey(client.isRequireProofKey())
                    .build()
            )
            .build();
    }
}
