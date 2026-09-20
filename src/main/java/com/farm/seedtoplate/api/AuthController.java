package com.farm.seedtoplate.api;

import com.farm.seedtoplate.dto.AuthRequest;
import com.farm.seedtoplate.dto.AuthResponse;
import com.farm.seedtoplate.dto.OtpVerificationRequest;
import com.farm.seedtoplate.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/request-otp")
    public ResponseEntity<AuthResponse> requestOtp(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.requestOtp(request));
    }

    @PostMapping("/auth/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }
}
