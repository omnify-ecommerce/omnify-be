package com.omnify;

import com.omnify.auth.infrastructure.LockoutProperties;
import com.omnify.config.properties.MailProperties;
import com.omnify.security.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({RateLimitProperties.class, LockoutProperties.class, MailProperties.class})
public class OmnifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmnifyApplication.class, args);
    }
}
