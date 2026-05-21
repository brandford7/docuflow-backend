package com.docuflow.api.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Convenience annotation for injecting the authenticated user into controller methods.
 *
 * Without this, every controller method that needs the current user must do:
 *
 *   @GetMapping("/me")
 *   public UserResponse getMe(Authentication authentication) {
 *       UserEntity user = (UserEntity) authentication.getPrincipal();
 *       ...
 *   }
 *
 * With this annotation, the same method becomes:
 *
 *   @GetMapping("/me")
 *   public UserResponse getMe(@CurrentUser UserEntity user) {
 *       ...
 *   }
 *
 * How it works:
 * @AuthenticationPrincipal tells Spring Security to resolve the principal
 * (the authenticated user object) from the SecurityContext and inject it
 * as a method parameter. Our principal is the UserEntity itself because
 * UserEntity implements UserDetails — so Spring Security stores it directly
 * as the principal when JwtAuthFilter authenticates the request.
 *
 * @Target(PARAMETER) means this annotation can only be placed on method parameters.
 * @Retention(RUNTIME) means it is available at runtime for Spring to read.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}