package com.grade.controller;

import com.grade.dto.ApiResponse;
import com.grade.entity.AuditLog;
import com.grade.entity.User;
import com.grade.mapper.AuditLogMapper;
import com.grade.mapper.CourseMapper;
import com.grade.mapper.UserMapper;
import com.grade.service.AdminService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserMapper userMapper;
    private final AuditLogMapper auditLogMapper;
    private final CourseMapper courseMapper;

    public AdminController(AdminService adminService, UserMapper userMapper,
                           AuditLogMapper auditLogMapper, CourseMapper courseMapper) {
        this.adminService = adminService;
        this.userMapper = userMapper;
        this.auditLogMapper = auditLogMapper;
        this.courseMapper = courseMapper;
    }

    @PostMapping("/users/import")
    public ApiResponse<Map<String, Object>> importStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "STUDENT") String role) throws IOException {
        Map<String, Object> result = adminService.importStudents(file.getInputStream());
        return ApiResponse.success("导入完成", result);
    }

    @GetMapping("/users")
    public ApiResponse<List<User>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        int offset = (page - 1) * size;
        return ApiResponse.success(
                userMapper.selectByCondition(role, keyword, offset, size));
    }

    @PutMapping("/users/{userId}/status")
    public ApiResponse<?> toggleUserStatus(
            @PathVariable Long userId,
            @RequestParam String status) {
        userMapper.updateStatus(userId, status);
        return ApiResponse.success("状态更新成功");
    }

    @PutMapping("/users/{userId}/reset-password")
    public ApiResponse<?> resetPassword(@PathVariable Long userId) {
        // 默认重置为 "Reset@123" 的 BCrypt 加密值
        userMapper.resetPassword(userId,
                "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh");
        return ApiResponse.success("密码已重置为 Reset@123");
    }

    @GetMapping("/logs")
    public ApiResponse<List<AuditLog>> queryLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String opType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        int offset = (page - 1) * size;
        return ApiResponse.success(
                auditLogMapper.selectByFilter(start, end, opType, offset, size));
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        Map<String, Object> data = adminService.getDashboard();
        return ApiResponse.success(data);
    }

    @PostMapping("/courses/{courseId}/bind")
    public ApiResponse<?> bindCourse(
            @PathVariable Long courseId,
            @RequestParam Long classId,
            @RequestParam Long teacherId,
            @RequestParam Long semesterId) {
        courseMapper.bindClass(courseId, classId, teacherId, semesterId);
        return ApiResponse.success("绑定成功");
    }
}
