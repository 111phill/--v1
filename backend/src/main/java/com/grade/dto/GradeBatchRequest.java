package com.grade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GradeBatchRequest {
    @NotNull private Long courseId;
    @NotNull private Long classId;
    @NotNull private Long semesterId;
    @NotBlank private String action;   // DRAFT / SUBMITTED
    @NotEmpty private List<GradeItem> grades;

    @Data
    public static class GradeItem {
        @NotNull private Long studentId;
        private BigDecimal usualScore;
        private BigDecimal examScore;
    }
}
