package com.docuflow.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Strongly-typed representation of all custom app.* properties.
 *
 * @Validated + constraint annotations mean the application fails immediately
 * at startup if any required property is missing or invalid.
 * This is far better than a NullPointerException at runtime when the
 * property is first used.
 */
@ConfigurationProperties(prefix = "app")
@Validated
@Getter
@Setter
public class AppProperties {

    @Valid private Jwt jwt = new Jwt();
    @Valid private R2 r2 = new R2();
    @Valid private Processing processing = new Processing();
    @Valid private Paystack paystack = new Paystack();
    @Valid private Cors cors = new Cors();
    @Valid private Mail mail = new Mail();
    @Valid
    private Plans plans = new Plans();



    @NotBlank
    private String frontendUrl;

    // ── Nested config classes ─────────────────────────────────────────────────

    @Getter @Setter
    public static class Jwt {

        @NotBlank
        @Size(min = 32, message = "JWT secret must be at least 32 characters")
        private String secret;

        @Positive
        private long accessTokenExpiryMs = 86_400_000L;

        @Positive
        private long refreshTokenExpiryMs = 604_800_000L;
    }

    @Getter @Setter
    public static class R2 {

        @NotBlank
        private String endpoint;

        @NotBlank
        private String accessKey;

        @NotBlank
        private String secretKey;

        @NotBlank
        private String bucket;

        @NotBlank
        private String publicUrl;

        @Positive
        private int presignedUrlTtlMinutes = 60;
    }

    @Getter @Setter
    public static class Processing {

        @NotBlank
        private String gotenbergUrl;

        @Positive
        private int jobTimeoutSeconds = 120;
    }

    @Getter @Setter
    public static class Paystack {

        @NotBlank
        private String secretKey;

        @NotBlank
        private String webhookSecret;

        private String baseUrl = "https://api.paystack.co";
    }

    @Getter @Setter
    public static class Cors {

        @NotBlank
        private String allowedOrigins;
    }

    @Getter @Setter
    public static class Mail {

        @NotBlank
        private String from;

        private String fromName = "DocuFlow";
    }

    @Getter @Setter
    public static class Plans {

        @Valid
        private PlanLimits free = new PlanLimits();

        @Valid
        private PlanLimits pro = new PlanLimits();
    }

    @Getter @Setter
    public static class PlanLimits {

        @Positive
        private int maxFileSizeMb = 5;

        // -1 means unlimited
        private int maxOpsPerDay = 3;

        @Positive
        private int storageLimitMb = 500;

        @Positive
        private int fileExpiryDays = 7;
    }
}
