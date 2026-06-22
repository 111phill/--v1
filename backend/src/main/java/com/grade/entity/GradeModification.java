package com.grade.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GradeModification {
    private Long id;
    private Long gradeId;
    private Long teacherId;
    private BigDecimal oldScore;
    private BigDecimal newScore;
    private String reason;
    private String status;     // PENDING / APPROVED / REJECTED
    private Long reviewerId;
    private String reviewComment;
    private LocalDateTime createTime;
    private LocalDateTime reviewTime;

    // 扩展
    private String teacherName;
    private String courseName;
    private String studentName;
}
