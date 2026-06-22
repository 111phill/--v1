package com.grade.controller;

import com.grade.dto.*;
import com.grade.entity.Grade;
import com.grade.entity.GradeModification;
import com.grade.service.GradeService;
import com.grade.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    // 学生查询本人成绩
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<Grade>> getMyGrades(@RequestParam Long semesterId) {
        return ApiResponse.success(gradeService.getStudentGrades(semesterId));
    }

    // 教师查询授课班级成绩
    @GetMapping("/class")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<Grade>> getClassGrades(
            @RequestParam Long courseId,
            @RequestParam Long classId,
            @RequestParam Long semesterId) {
        return ApiResponse.success(
                gradeService.getClassGrades(courseId, classId, semesterId));
    }

    // 教师录入成绩（暂存/提交）
    @PostMapping("/batch")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Map<String, Object>> batchSave(
            @Valid @RequestBody GradeBatchRequest request) {
        Long teacherId = SecurityUtils.getCurrentUserId();
        Map<String, Object> result = gradeService.batchSave(
                teacherId, request.getCourseId(), request.getClassId(),
                request.getSemesterId(), request.getAction(), request.getGrades());
        return ApiResponse.success(result);
    }

    // 教师查询成绩统计
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<GradeStatistics> getStatistics(
            @RequestParam Long courseId,
            @RequestParam Long classId,
            @RequestParam Long semesterId) {
        return ApiResponse.success(
                gradeService.getStatistics(courseId, classId, semesterId));
    }

    // 教师发起修改申请
    @PostMapping("/modification")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<?> requestModification(
            @Valid @RequestBody ModificationRequest request) {
        gradeService.requestModification(request);
        return ApiResponse.success("修改申请已提交");
    }

    // 管理员查询待审核修改申请
    @GetMapping("/modifications/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<GradeModification>> getPendingModifications() {
        return ApiResponse.success(gradeService.getPendingModifications());
    }

    // 管理员审核修改申请
    @PutMapping("/modification/{modId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> reviewModification(
            @PathVariable Long modId,
            @Valid @RequestBody ReviewRequest request) {
        gradeService.reviewModification(
                modId, request.getAction(), request.getReviewComment());
        return ApiResponse.success("审核处理完成");
    }

    // 管理员发布成绩
    @PutMapping("/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> publish(@RequestParam Long semesterId) {
        int count = gradeService.publishGrades(semesterId);
        return ApiResponse.success("已发布 " + count + " 条成绩");
    }

    // 学生 GPA 趋势
    @GetMapping("/gpa-trend")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<Map<String, Object>>> getGpaTrend() {
        return ApiResponse.success(gradeService.getGpaTrend());
    }
}
