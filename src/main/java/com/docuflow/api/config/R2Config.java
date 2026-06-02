package com.docuflow.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Cloudflare R2 client configuration.
 *
 * R2 is S3-compatible — we use the AWS SDK pointed at the R2 endpoint.
 * forcePathStyle(true) is required by R2.
 * Region.of("auto") is the correct value for R2 (not a real AWS region).
 */
@Configuration
@RequiredArgsConstructor
public class R2Config {

    private final AppProperties appProperties;

    @Bean
    public S3Client s3Client() {
        var creds = AwsBasicCredentials.create(
            appProperties.getR2().getAccessKey(),
            appProperties.getR2().getSecretKey());

        return S3Client.builder()
            .endpointOverride(URI.create(appProperties.getR2().getEndpoint()))
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .region(Region.of("auto"))
            .forcePathStyle(true)
            .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var creds = AwsBasicCredentials.create(
            appProperties.getR2().getAccessKey(),
            appProperties.getR2().getSecretKey());

        return S3Presigner.builder()
            .endpointOverride(URI.create(appProperties.getR2().getEndpoint()))
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .region(Region.of("auto"))
            .build();
    }
}
