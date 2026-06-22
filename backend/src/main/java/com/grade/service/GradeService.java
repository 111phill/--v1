package com.grade.service;

import com.grade.dto.GradeBatchRequest;
import com.grade.dto.GradeStatistics;
import com.grade.dto.ModificationRequest;
import com.grade.entity.*;
import com.grade.mapper.*;
import com.grade.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GradeService {

    private final GradeMapper gradeMapper;
    private final CourseMapper courseMapper;
    private final SemesterMapper semesterMapper;
    private final GradeModificationMapper modificationMapper;

    public GradeService(GradeMapper gradeMapper, CourseMapper courseMapper,
                        SemesterMapper semesterMapper,
                        GradeModificationMapper modificationMapper) {
        this.gradeMapper = gradeMapper;
        this.courseMapper = courseMapper;
        this.semesterMapper = semesterMapper;
        this.modificationMapper = modificationMapper;
    }

    public List<Grade> getStudentGrades(Long semesterId) {
        Long studentId = SecurityUtils.getCurrentUserId();
        return gradeMapper.selectByStudentAndSemester(studentId, semesterId);
    }

    public List<Grade> getClassGrades(Long courseId, Long classId, Long semesterId) {
        return gradeMapper.selectByCourseAndClass(courseId, classId, semesterId);
    }

    @Transactional
    public Map<String, Object> batchSave(Long teacherId, Long courseId, Long classId,
                                          Long semesterId, String action,
                                          List<GradeBatchRequest.GradeItem> items) {
        // 校验授课权
        int binding = courseMapper.countTeacherBinding(teacherId, courseId, classId, semesterId);
        if (binding == 0) {
            throw new RuntimeException("您不是此课程的授课教师");
        }

        // 校验时间窗口
        Semester semester = semesterMapper.selectById(semesterId);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(semester.getGradeEntryStart()) ||
                now.isAfter(semester.getGradeEntryEnd())) {
            throw new RuntimeException("当前不在成绩录入时间窗口内");
        }

        // 防重复提交
        if ("SUBMITTED".equals(action)) {
            int submitted = gradeMapper.countSubmitted(courseId, classId, semesterId);
            if (submitted > 0) {
                throw new RuntimeException("成绩已提交，不可修改。如需修改请发起修改申请");
            }
        }

        Course course = courseMapper.selectById(courseId);
        int savedCount = 0;

        for (GradeBatchRequest.GradeItem item : items) {
            BigDecimal totalScore = calculateTotalScore(
                    item.getUsualScore(), item.getExamScore(), course);
            BigDecimal gpa = scoreToGpa(totalScore);

            Grade grade = new Grade();
            grade.setStudentId(item.getStudentId());
            grade.setCourseId(courseId);
            grade.setClassId(classId);
            grade.setSemesterId(semesterId);
            grade.setUsualScore(item.getUsualScore());
            grade.setExamScore(item.getExamScore());
            grade.setTotalScore(totalScore);
            grade.setGpa(gpa);
            grade.setStatus(action);
            grade.setEntryTeacherId(teacherId);

            gradeMapper.insertOrUpdate(grade);
            savedCount++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("savedCount", savedCount);
        result.put("message", "SUBMITTED".equals(action) ? "成绩提交成功，已锁定" : "成绩暂存成功");
        return result;
    }

    public GradeStatistics getStatistics(Long courseId, Long classId, Long semesterId) {
        Map<String, Object> raw = gradeMapper.calcStatistics(courseId, classId, semesterId);

        GradeStatistics stats = new GradeStatistics();
        if (raw.get("totalCount") != null) {
            stats.setTotalCount(((Number) raw.get("totalCount")).intValue());
        }
        if (raw.get("maxScore") != null) {
            stats.setMaxScore(new BigDecimal(raw.get("maxScore").toString()));
        }
        if (raw.get("minScore") != null) {
            stats.setMinScore(new BigDecimal(raw.get("minScore").toString()));
        }
        if (raw.get("avgScore") != null) {
            stats.setAvgScore(new BigDecimal(raw.get("avgScore").toString()));
        }
        if (raw.get("stdDeviation") != null) {
            stats.setStdDeviation(new BigDecimal(raw.get("stdDeviation").toString()));
        }
        if (raw.get("passCount") != null) {
            stats.setPassCount(((Number) raw.get("passCount")).intValue());
        }
        if (raw.get("passRate") != null) {
            stats.setPassRate(new BigDecimal(raw.get("passRate").toString()));
        }

        // 各分数段分布
        List<Grade> grades = gradeMapper.selectByCourseAndClass(courseId, classId, semesterId);
        int total = grades.size();
        if (total > 0) {
            int[] segments = {0, 0, 0, 0, 0}; // 优秀/良好/中等/及格/不及格
            for (Grade g : grades) {
                BigDecimal s = g.getTotalScore();
                if (s == null) continue;
                double score = s.doubleValue();
                if (score >= 90) segments[0]++;
                else if (score >= 80) segments[1]++;
                else if (score >= 70) segments[2]++;
                else if (score >= 60) segments[3]++;
                else segments[4]++;
            }
            String[] labels = {"优秀", "良好", "中等", "及格", "不及格"};
            String[] ranges = {"[90,100]", "[80,90)", "[70,80)", "[60,70)", "[0,60)"};
            List<GradeStatistics.ScoreDistribution> dist = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                GradeStatistics.ScoreDistribution d = new GradeStatistics.ScoreDistribution();
                d.setRange(ranges[i]);
                d.setLabel(labels[i]);
                d.setCount(segments[i]);
                d.setRatio(BigDecimal.valueOf(segments[i] * 100.0 / total)
                        .setScale(1, RoundingMode.HALF_UP));
                dist.add(d);
            }
            stats.setDistribution(dist);
        }

        return stats;
    }

    @Transactional
    public void requestModification(ModificationRequest request) {
        Long teacherId = SecurityUtils.getCurrentUserId();
        Grade grade = gradeMapper.selectById(request.getGradeId());
        if (grade == null) {
            throw new RuntimeException("成绩记录不存在");
        }

        GradeModification mod = new GradeModification();
        mod.setGradeId(request.getGradeId());
        mod.setTeacherId(teacherId);
        mod.setOldScore(grade.getTotalScore());
        mod.setNewScore(request.getNewScore());
        mod.setReason(request.getReason());
        modificationMapper.insert(mod);
    }

    @Transactional
    public void reviewModification(Long modId, String action, String comment) {
        Long adminId = SecurityUtils.getCurrentUserId();
        GradeModification mod = modificationMapper.selectById(modId);
        if (mod == null) {
            throw new RuntimeException("修改申请不存在");
        }
        if (!"PENDING".equals(mod.getStatus())) {
            throw new RuntimeException("该申请已处理");
        }

        modificationMapper.approve(modId, action, comment, adminId);

        if ("APPROVED".equals(action)) {
            Grade grade = gradeMapper.selectById(mod.getGradeId());
            Course course = courseMapper.selectById(grade.getCourseId());
            BigDecimal gpa = scoreToGpa(mod.getNewScore());
            gradeMapper.updateScore(mod.getGradeId(), mod.getNewScore(), gpa);
        }
    }

    public List<GradeModification> getPendingModifications() {
        return modificationMapper.selectPending();
    }

    public List<Map<String, Object>> getGpaTrend() {
        Long studentId = SecurityUtils.getCurrentUserId();
        return gradeMapper.selectGpaTrend(studentId);
    }

    @Transactional
    public int publishGrades(Long semesterId) {
        return gradeMapper.publishBySemester(semesterId);
    }

    private BigDecimal calculateTotalScore(BigDecimal usual, BigDecimal exam, Course course) {
        if (usual == null) usual = BigDecimal.ZERO;
        if (exam == null) exam = BigDecimal.ZERO;
        BigDecimal usualRatio = course.getUsualRatio() != null ? course.getUsualRatio() : new BigDecimal("0.3");
        BigDecimal examRatio = course.getExamRatio() != null ? course.getExamRatio() : new BigDecimal("0.7");
        return usual.multiply(usualRatio).add(exam.multiply(examRatio))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal scoreToGpa(BigDecimal score) {
        if (score == null) return BigDecimal.ZERO;
        double s = score.doubleValue();
        if (s >= 90) return new BigDecimal("4.0");
        if (s >= 85) return new BigDecimal("3.7");
        if (s >= 82) return new BigDecimal("3.3");
        if (s >= 78) return new BigDecimal("3.0");
        if (s >= 75) return new BigDecimal("2.7");
        if (s >= 72) return new BigDecimal("2.3");
        if (s >= 68) return new BigDecimal("2.0");
        if (s >= 64) return new BigDecimal("1.5");
        if (s >= 60) return new BigDecimal("1.0");
        return BigDecimal.ZERO;
    }
}
