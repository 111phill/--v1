package com.grade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Semester {
    private Long id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime gradeEntryStart;
    private LocalDateTime gradeEntryEnd;
    private Integer isCurrent;
    private LocalDateTime createTime;
}
