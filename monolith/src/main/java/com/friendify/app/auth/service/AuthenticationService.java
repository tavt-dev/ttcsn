package com.friendify.app.auth.service;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;

import com.friendify.app.auth.constant.EmailTemplate;
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
import com.friendify.app.auth.entity.Role;
import com.friendify.app.auth.entity.User;
import com.friendify.app.auth.entity.UserOtp;
import com.friendify.app.auth.enums.OtpType;
import com.friendify.app.auth.enums.PredefinedRole;
import com.friendify.app.auth.enums.SignInProvider;
import com.friendify.app.auth.mapper.UserMapper;
import com.friendify.app.auth.port.NotificationDeliveryPort;
import com.friendify.app.auth.repository.RoleRepository;
import com.friendify.app.auth.repository.UserRepository;
import com.friendify.app.profile.dto.request.ProfileCreationRequest;
import com.friendify.app.profile.port.ProfileCreationPort;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    JwtService jwtService;
    OtpService otpService;
    NotificationDeliveryPort notificationDeliveryPort;
    ProfileCreationPort profileCreationPort;

    @Transactional
    public UserResponse register(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(defaultUserRoles());
        user.setEmailVerified(false);
        user.setIsActive(false);
        user.setProvider(SignInProvider.LOCAL);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        profileCreationPort.createProfile(toProfileCreationRequest(request, user.getId()));

        UserOtp userOtp = otpService.createOtp(user, OtpType.REGISTER, 15);
        notificationDeliveryPort.sendEmail(
                request.getEmail(),
                "Verify email",
                EmailTemplate.otpEmail(request.getUsername(), userOtp.getOtpCode()));

        UserResponse response = userMapper.toUserResponse(user);
        response.setId(user.getId());
        return response;
    }

    @Transactional
    public void verifyUser(VerifyUserRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserOtp userOtp = otpService.findLatestOtp(user, OtpType.REGISTER);
        otpService.validateOtp(userOtp, request.getOtpCode());

        user.setEmailVerified(true);
        user.setIsActive(true);
        userRepository.save(user);
        otpService.markOtpAsUsed(userOtp);

        notificationDeliveryPort.sendEmail(
                request.getEmail(),
                "Welcome to Friendify",
                EmailTemplate.welcomeEmail(user.getUsername()));
    }

    @Transactional
    public void resendVerificationCode(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (Boolean.TRUE.equals(user.getIsActive()) && user.isEmailVerified()) {
            throw new AppException(ErrorCode.USER_ALREADY_VERIFIED);
        }

        otpService.checkOtpFrequency(user, OtpType.REGISTER);
        otpService.deactivateOldOtps(user.getId(), OtpType.REGISTER);
        UserOtp newOtp = otpService.createOtp(user, OtpType.REGISTER, 15);

        notificationDeliveryPort.sendEmail(
                user.getEmail(),
                "New verification code",
                EmailTemplate.resendVerificationEmail(user.getUsername(), newOtp.getOtpCode()));
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        return IntrospectResponse.builder()
                .valid(jwtService.isValidToken(request.getToken()))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByUsernameWithRolesAndPermissions(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AppException(ErrorCode.USER_DISABLED);
        }

        return AuthenticationResponse.builder()
                .token(jwtService.generateToken(user))
                .authenticated(true)
                .build();
    }

    public void logout(LogoutRequest request) {
        jwtService.revokeToken(request.getToken());
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws JOSEException, ParseException {
        SignedJWT signedJWT = jwtService.verifyToken(request.getToken(), true);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        String jwtId = claimsSet.getJWTID();
        Date expiryTime = claimsSet.getExpirationTime();
        if (jwtId == null || expiryTime == null || claimsSet.getSubject() == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        jwtService.revokeTokenById(jwtId, expiryTime);
        User user = userRepository.findByIdWithRolesAndPermissions(claimsSet.getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        return AuthenticationResponse.builder()
                .token(jwtService.generateToken(user))
                .authenticated(true)
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!user.isEmailVerified()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        otpService.checkOtpFrequency(user, OtpType.RESET_PASSWORD);
        otpService.deactivateOldOtps(user.getId(), OtpType.RESET_PASSWORD);
        UserOtp newOtp = otpService.createOtp(user, OtpType.RESET_PASSWORD, 15);

        notificationDeliveryPort.sendEmail(
                user.getEmail(),
                "Reset Password - Friendify",
                EmailTemplate.resetPasswordEmail(user.getUsername(), newOtp.getOtpCode()));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!user.isEmailVerified()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        UserOtp userOtp = otpService.findLatestOtp(user, OtpType.RESET_PASSWORD);
        otpService.validateOtp(userOtp, request.getOtpCode());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        otpService.markOtpAsUsed(userOtp);
    }

    private HashSet<Role> defaultUserRoles() {
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        return roles;
    }

    private ProfileCreationRequest toProfileCreationRequest(UserCreationRequest request, String userId) {
        String lastName = hasText(request.getLastName()) ? request.getLastName().trim() : request.getUsername();
        return ProfileCreationRequest.builder()
                .userId(userId)
                .username(request.getUsername())
                .firstName(hasText(request.getFirstName()) ? request.getFirstName().trim() : request.getFirstName())
                .lastName(lastName)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
