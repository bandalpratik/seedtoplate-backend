package com.farm.seedtoplate.api;

import com.farm.seedtoplate.domain.User;
import com.farm.seedtoplate.domain.UserRole;
import com.farm.seedtoplate.dto.UserIdentifyRequest;
import com.farm.seedtoplate.dto.UserProfileResponse;
import com.farm.seedtoplate.repository.UserRepository;
import com.farm.seedtoplate.security.JwtService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public UserController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/users/identify")
    public ResponseEntity<UserProfileResponse> identify(@Valid @RequestBody UserIdentifyRequest request) {
        String phone = request.phone().trim();
        User user = userRepository.findByPhone(phone)
            .orElseGet(() -> {
                User fresh = new User();
                fresh.setPhone(phone);
                fresh.setFullName(request.fullName());
                fresh.setRole(UserRole.CUSTOMER);
                return fresh;
            });

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }

        user = userRepository.save(user);

        return ResponseEntity.ok(new UserProfileResponse(
            user.getId(),
            user.getPhone(),
            user.getFullName(),
            user.getRole().name(),
            user.getRole() == UserRole.ADMIN,
            jwtService.generateToken(user)
        ));
    }
}
