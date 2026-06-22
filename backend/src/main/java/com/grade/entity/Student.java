package com.grade.entity;

import lombok.Data;

@Data
public class Student {
    private Long id;
    private Long userId;
    private String studentNo;
    private Long classId;
    private String username;
    private String realName;
    private String gender;
    private String className;
    private String majorName;
    private String collegeName;
}
