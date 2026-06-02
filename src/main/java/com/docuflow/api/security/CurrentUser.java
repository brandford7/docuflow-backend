package com.docuflow.api.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Injects the authenticated user into controller method parameters.
 *
 * Usage:
 *   public ResponseEntity<UserResponse> getMe(@CurrentUser User user) { ... }
 *
 * Spring resolves this from the SecurityContext that JwtAuthFilter populated.
 * No extra DB query — the user is already loaded.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
