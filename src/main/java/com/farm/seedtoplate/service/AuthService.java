package com.farm.seedtoplate.service;

import com.farm.seedtoplate.domain.User;
import com.farm.seedtoplate.domain.UserRole;
import com.farm.seedtoplate.dto.AuthRequest;
import com.farm.seedtoplate.dto.AuthResponse;
import com.farm.seedtoplate.dto.OtpVerificationRequest;
import com.farm.seedtoplate.exception.ApiException;
import com.farm.seedtoplate.repository.UserRepository;
import com.farm.seedtoplate.security.JwtService;
import java.time.Instant;
import java.util.Random;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse requestOtp(AuthRequest request) {
        String phone = request.phone().trim();
        User user = userRepository.findByPhone(phone)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setPhone(phone);
                newUser.setRole(UserRole.CUSTOMER);
                return newUser;
            });

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }

        String otpCode = String.format("%06d", new Random().nextInt(900000) + 100000);
        user.setOtpCode(otpCode);
        user.setOtpExpiresAt(Instant.now().plusSeconds(300));
        user.setOtpVerified(false);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(
            user.getId(),
            user.getPhone(),
            user.getFullName(),
            user.getRole().name(),
            user.getRole() == UserRole.ADMIN,
            token
        );
    }

    @Transactional
    public AuthResponse verifyOtp(OtpVerificationRequest request) {
        User user = userRepository.findByPhone(request.phone())
            .orElseThrow(() -> new ApiException("User not found"));

        if (user.getOtpCode() == null || user.getOtpExpiresAt() == null || user.getOtpExpiresAt().isBefore(Instant.now())) {
            throw new ApiException("OTP has expired or is invalid");
        }

        if (!user.getOtpCode().equals(request.otpCode())) {
            throw new ApiException("Incorrect OTP");
        }

        user.setOtpVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(
            user.getId(),
            user.getPhone(),
            user.getFullName(),
            user.getRole().name(),
            user.getRole() == UserRole.ADMIN,
            token
        );
    }
}
