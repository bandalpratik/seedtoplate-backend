package com.farm.seedtoplate.service;

import com.farm.seedtoplate.domain.User;
import com.farm.seedtoplate.domain.UserRole;
import com.farm.seedtoplate.dto.UserProfileResponse;
import com.farm.seedtoplate.repository.UserRepository;
import com.farm.seedtoplate.security.JwtService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public Optional<UserProfileResponse> findById(UUID id) {
        return userRepository.findById(id)
            .map(user -> new UserProfileResponse(
                user.getId(),
                user.getPhone(),
                user.getFullName(),
                user.getRole().name(),
                user.getRole() == UserRole.ADMIN,
                jwtService.generateToken(user)
            ));
    }
}
