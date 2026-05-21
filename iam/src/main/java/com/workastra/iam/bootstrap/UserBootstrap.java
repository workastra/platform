package com.workastra.iam.bootstrap;

import com.workastra.core.module.security.model.User;
import com.workastra.core.module.security.repository.UserRepository;
import com.workastra.core.module.security.support.Identifiers;
import com.workastra.core.module.security.support.SecurityContextTemplate;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.integration.support.locks.DistributedLock;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class UserBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(UserBootstrap.class);
    private final LockRegistry<DistributedLock> lockRegistry;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Identifiers identifiers;

    public UserBootstrap(
        LockRegistry<DistributedLock> lockRegistry,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        Identifiers identifiers
    ) {
        this.lockRegistry = lockRegistry;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.identifiers = identifiers;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.lockRegistry.executeLocked("iam.users", Duration.ofSeconds(30), () -> {
            logger.info("Acquired lock for user migration");

            var systemUser = this.userRepository.findByUsername("system");

            if (systemUser == null) {
                logger.info(
                    "There are no system user found, the process cannot continue without a system user. Please run the SQL migration script to create the system user before starting the application."
                );
                return;
            }

            if (this.userRepository.findByUsername("root") != null) {
                logger.info("Root user already exists, skipping root user creation");
                return;
            }

            SecurityContextTemplate.use(this.identifiers.ofUsername("system")).run(() -> {
                var rootUser = User.builder()
                    .username("root")
                    .password(this.passwordEncoder.encode("root"))
                    .email("root@internal.com")
                    .build();

                this.userRepository.save(rootUser);
            });

            logger.info("Root user created successfully");
        });
    }
}
