package com.grade.mapper;

import com.grade.entity.GradeModification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GradeModificationMapper {

    @Insert("INSERT INTO t_grade_modification (grade_id, teacher_id, old_score, new_score, reason) " +
            "VALUES (#{gradeId}, #{teacherId}, #{oldScore}, #{newScore}, #{reason})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GradeModification mod);

    @Select("SELECT m.*, u.real_name AS teacher_name, c.name AS course_name, " +
            "su.real_name AS student_name " +
            "FROM t_grade_modification m " +
            "JOIN t_grade g ON m.grade_id = g.id " +
            "JOIN t_course c ON g.course_id = c.id " +
            "JOIN t_user u ON m.teacher_id = u.id " +
            "JOIN t_user su ON g.student_id = su.id " +
            "WHERE m.status = 'PENDING' ORDER BY m.create_time")
    List<GradeModification> selectPending();

    @Select("SELECT * FROM t_grade_modification WHERE id = #{id}")
    GradeModification selectById(Long id);

    @Update("UPDATE t_grade_modification SET status = #{status}, reviewer_id = #{reviewerId}, " +
            "review_comment = #{comment}, review_time = NOW() WHERE id = #{id}")
    int approve(@Param("id") Long id, @Param("status") String status,
                @Param("comment") String comment, @Param("reviewerId") Long reviewerId);
}
