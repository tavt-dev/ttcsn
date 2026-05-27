package com.friendify.app.auth.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import com.friendify.app.auth.entity.User;
import com.friendify.app.auth.entity.UserOtp;
import com.friendify.app.auth.enums.OtpType;
import com.friendify.app.auth.repository.UserOtpRepository;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpService {

    UserOtpRepository userOtpRepository;
    Random random = new Random();

    @Transactional
    public UserOtp createOtp(User user, OtpType type, int expiryMinutes) {
        UserOtp userOtp = UserOtp.builder()
                .user(user)
                .otpCode(generateVerificationCode())
                .type(type)
                .expiryTime(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .build();

        return userOtpRepository.save(userOtp);
    }

    public UserOtp findLatestOtp(User user, OtpType type) {
        return userOtpRepository
                .findTopByUserAndTypeAndUsedFalseOrderByCreatedAtDesc(user, type)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_FOUND));
    }

    @Transactional
    public void validateOtp(UserOtp userOtp, String providedOtp) {
        if (userOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (!userOtp.getOtpCode().equals(providedOtp)) {
            userOtp.setUsed(true);
            userOtpRepository.save(userOtp);
            throw new AppException(ErrorCode.OTP_INVALID);
        }
    }

    @Transactional
    public void markOtpAsUsed(UserOtp userOtp) {
        userOtp.setUsed(true);
        userOtpRepository.save(userOtp);
    }

    public void checkOtpFrequency(User user, OtpType type) {
        Optional<UserOtp> lastOtp = userOtpRepository.findTopByUserAndTypeAndUsedFalseOrderByCreatedAtDesc(user, type);
        if (lastOtp.isPresent()
                && lastOtp.get().getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(60))) {
            throw new AppException(ErrorCode.OTP_TOO_FREQUENT);
        }
    }

    @Transactional
    public void deactivateOldOtps(String userId, OtpType type) {
        userOtpRepository.deactivateOldOtp(userId, type);
    }

    private String generateVerificationCode() {
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
