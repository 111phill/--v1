package com.grade.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GradeStatistics {
    private Integer totalCount;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal avgScore;
    private BigDecimal stdDeviation;
    private Integer passCount;
    private BigDecimal passRate;
    private List<ScoreDistribution> distribution;

    @Data
    public static class ScoreDistribution {
        private String range;
        private String label;
        private Integer count;
        private BigDecimal ratio;
    }
}
