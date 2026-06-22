package com.grade.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLog {
    private Long id;
    private Long operatorId;
    private String opType;
    private String entityType;
    private Long entityId;
    private String oldValue;
    private String newValue;
    private String opIp;
    private String remark;
    private LocalDateTime createTime;
}
