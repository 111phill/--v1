package com.grade.mapper;

import com.grade.dto.GradeStatistics;
import com.grade.entity.Grade;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface GradeMapper {

    // 学生查询本人成绩（按学期）
    @Select("SELECT g.*, c.name AS course_name, c.credit, c.usual_ratio, c.exam_ratio " +
            "FROM t_grade g JOIN t_course c ON g.course_id = c.id " +
            "WHERE g.student_id = #{studentId} AND g.semester_id = #{semesterId} " +
            "ORDER BY c.id")
    List<Grade> selectByStudentAndSemester(@Param("studentId") Long studentId,
                                           @Param("semesterId") Long semesterId);

    // 教师查询授课班级成绩
    @Select("SELECT g.*, c.name AS course_name, c.credit, u.real_name AS student_name, " +
            "u.username AS student_no " +
            "FROM t_grade g " +
            "JOIN t_course c ON g.course_id = c.id " +
            "JOIN t_user u ON g.student_id = u.id " +
            "WHERE g.course_id = #{courseId} AND g.class_id = #{classId} " +
            "AND g.semester_id = #{semesterId} ORDER BY u.username")
    List<Grade> selectByCourseAndClass(@Param("courseId") Long courseId,
                                       @Param("classId") Long classId,
                                       @Param("semesterId") Long semesterId);

    // 插入或更新成绩（暂存/提交）
    @Insert("INSERT INTO t_grade (student_id, course_id, class_id, semester_id, " +
            "usual_score, exam_score, total_score, gpa, status, entry_teacher_id) " +
            "VALUES (#{studentId}, #{courseId}, #{classId}, #{semesterId}, " +
            "#{usualScore}, #{examScore}, #{totalScore}, #{gpa}, #{status}, #{entryTeacherId}) " +
            "ON DUPLICATE KEY UPDATE usual_score = VALUES(usual_score), " +
            "exam_score = VALUES(exam_score), total_score = VALUES(total_score), " +
            "gpa = VALUES(gpa), status = VALUES(status), " +
            "entry_teacher_id = VALUES(entry_teacher_id)")
    int insertOrUpdate(Grade grade);

    // 批量提交
    @Update("UPDATE t_grade SET status = 'SUBMITTED' " +
            "WHERE course_id = #{courseId} AND class_id = #{classId} " +
            "AND semester_id = #{semesterId} AND status = 'DRAFT'")
    int batchSubmit(@Param("courseId") Long courseId,
                    @Param("classId") Long classId,
                    @Param("semesterId") Long semesterId);

    // 查询已提交数量
    @Select("SELECT COUNT(*) FROM t_grade " +
            "WHERE course_id = #{courseId} AND class_id = #{classId} " +
            "AND semester_id = #{semesterId} AND status = 'SUBMITTED'")
    int countSubmitted(@Param("courseId") Long courseId,
                       @Param("classId") Long classId,
                       @Param("semesterId") Long semesterId);

    // 成绩统计
    @Select("SELECT COUNT(*) AS totalCount, MAX(total_score) AS maxScore, " +
            "MIN(total_score) AS minScore, ROUND(AVG(total_score), 1) AS avgScore, " +
            "ROUND(STDDEV(total_score), 2) AS stdDeviation, " +
            "COUNT(CASE WHEN total_score >= 60 THEN 1 END) AS passCount, " +
            "ROUND(COUNT(CASE WHEN total_score >= 60 THEN 1 END) * 100.0 / COUNT(*), 1) AS passRate " +
            "FROM t_grade WHERE course_id = #{courseId} AND class_id = #{classId} " +
            "AND semester_id = #{semesterId} AND status IN ('SUBMITTED','PUBLISHED')")
    Map<String, Object> calcStatistics(@Param("courseId") Long courseId,
                                       @Param("classId") Long classId,
                                       @Param("semesterId") Long semesterId);

    // 更新状态（管理员发布/审核修改）
    @Update("UPDATE t_grade SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    // 更新成绩值
    @Update("UPDATE t_grade SET total_score = #{totalScore}, gpa = #{gpa}, " +
            "update_time = NOW() WHERE id = #{id}")
    int updateScore(@Param("id") Long id,
                    @Param("totalScore") java.math.BigDecimal totalScore,
                    @Param("gpa") java.math.BigDecimal gpa);

    // GPA 趋势
    @Select("SELECT s.name AS semester_name, " +
            "ROUND(SUM(g.gpa * c.credit) / SUM(c.credit), 2) AS semester_gpa, " +
            "ROUND(SUM(SUM(g.gpa * c.credit)) OVER (ORDER BY s.start_date) / " +
            "SUM(SUM(c.credit)) OVER (ORDER BY s.start_date), 2) AS cumulative_gpa " +
            "FROM t_grade g JOIN t_course c ON g.course_id = c.id " +
            "JOIN t_semester s ON g.semester_id = s.id " +
            "WHERE g.student_id = #{studentId} AND g.status IN ('SUBMITTED','PUBLISHED') " +
            "GROUP BY s.id, s.name, s.start_date ORDER BY s.start_date")
    List<Map<String, Object>> selectGpaTrend(Long studentId);

    // 根据ID查询
    @Select("SELECT g.*, c.name AS course_name, u.real_name AS student_name " +
            "FROM t_grade g JOIN t_course c ON g.course_id = c.id " +
            "JOIN t_user u ON g.student_id = u.id WHERE g.id = #{id}")
    Grade selectById(Long id);

    // 管理员发布成绩
    @Update("UPDATE t_grade SET status = 'PUBLISHED' " +
            "WHERE semester_id = #{semesterId} AND status = 'SUBMITTED'")
    int publishBySemester(Long semesterId);
}
