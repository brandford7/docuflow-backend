package com.docuflow.api;

import com.docuflow.api.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @EnableConfigurationProperties — binds application.yml app.* properties
 * to the AppProperties class and validates them at startup.
 *
 * @EnableAsync — allows @Async on service methods so file processing
 * runs on a background thread without blocking the HTTP response.
 *
 * @EnableScheduling — activates @Scheduled methods for cleanup jobs
 * (expired file removal, daily usage reset).
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
@EnableAsync
@EnableScheduling
public class DocuflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocuflowApplication.class, args);
    }
}