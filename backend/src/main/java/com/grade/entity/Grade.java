package com.grade.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Grade {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Long classId;
    private Long semesterId;
    private BigDecimal usualScore;
    private BigDecimal examScore;
    private BigDecimal totalScore;
    private BigDecimal gpa;
    private String status;     // DRAFT / SUBMITTED / PUBLISHED
    private Long entryTeacherId;
    private Integer classRank;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 联表查询扩展字段
    private String courseName;
    private BigDecimal credit;
    private BigDecimal usualRatio;
    private BigDecimal examRatio;
    private String studentName;
    private String studentNo;
}
