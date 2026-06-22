package com.grade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ModificationRequest {
    @NotNull private Long gradeId;
    @NotNull private BigDecimal newScore;
    @NotBlank private String reason;
}
