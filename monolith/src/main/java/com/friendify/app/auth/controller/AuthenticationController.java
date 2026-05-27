package com.friendify.app.auth.controller;

import java.text.ParseException;

import com.friendify.app.auth.dto.request.AuthenticationRequest;
import com.friendify.app.auth.dto.request.ForgotPasswordRequest;
import com.friendify.app.auth.dto.request.IntrospectRequest;
import com.friendify.app.auth.dto.request.LogoutRequest;
import com.friendify.app.auth.dto.request.RefreshTokenRequest;
import com.friendify.app.auth.dto.request.ResetPasswordRequest;
import com.friendify.app.auth.dto.request.ResendOtpRequest;
import com.friendify.app.auth.dto.request.UserCreationRequest;
import com.friendify.app.auth.dto.request.VerifyUserRequest;
import com.friendify.app.auth.dto.response.AuthenticationResponse;
import com.friendify.app.auth.dto.response.IntrospectResponse;
import com.friendify.app.auth.dto.response.UserResponse;
import com.friendify.app.auth.service.AuthenticationService;
import com.friendify.app.shared.dto.ApiResponse;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/registration")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(authenticationService.register(request))
                .build();
    }

    @PostMapping("/verify-user")
    public ApiResponse<Void> verifyUser(@RequestBody @Valid VerifyUserRequest request) {
        authenticationService.verifyUser(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Void> resendVerificationCode(@RequestBody @Valid ResendOtpRequest request) {
        authenticationService.resendVerificationCode(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.authenticate(request))
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.refreshToken(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ApiResponse.<Void>builder().build();
    }
}
