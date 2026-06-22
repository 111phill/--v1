package com.grade.controller;

import com.grade.dto.ApiResponse;
import com.grade.dto.LoginRequest;
import com.grade.dto.LoginResponse;
import com.grade.service.AuthService;
import com.grade.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("登录成功", response);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh() {
        Long userId = SecurityUtils.getCurrentUserId();
        LoginResponse response = authService.refresh(userId);
        return ApiResponse.success("Token续期成功", response);
    }
}
