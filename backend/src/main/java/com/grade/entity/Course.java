package com.grade.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Course {
    private Long id;
    private String name;
    private String code;
    private BigDecimal credit;
    private Integer hours;
    private String examType;
    private BigDecimal usualRatio;
    private BigDecimal examRatio;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 扩展字段
    private Long classId;
    private String className;
    private Long teacherId;
    private String teacherName;
    private Integer studentCount;
    private Integer enteredCount;
}
