package com.grade.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotBlank private String action;   // APPROVE / REJECT
    private String reviewComment;
}
