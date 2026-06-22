package com.grade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String role;       // ADMIN / TEACHER / STUDENT
    private String gender;
    private String phone;
    private String email;
    private String status;     // ACTIVE / DISABLED
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
