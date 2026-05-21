package com.workastra.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.workastra")
@EntityScan(basePackages = "com.workastra")
@EnableJpaRepositories(basePackages = "com.workastra")
@EnableJpaAuditing
public class IAMApplication {

    static void main(String[] args) {
        SpringApplication.run(IAMApplication.class, args);
    }
}
