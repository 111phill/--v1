package com.grade.mapper;

import com.grade.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper {

    @Select("SELECT c.*, cc.class_id, cl.name AS class_name, " +
            "cc.teacher_id, u.real_name AS teacher_name, " +
            "(SELECT COUNT(*) FROM t_student s WHERE s.class_id = cc.class_id) AS student_count, " +
            "(SELECT COUNT(*) FROM t_grade g WHERE g.course_id = cc.course_id " +
            "AND g.class_id = cc.class_id AND g.semester_id = cc.semester_id) AS entered_count " +
            "FROM t_course c " +
            "JOIN t_course_class cc ON c.id = cc.course_id " +
            "JOIN t_class cl ON cc.class_id = cl.id " +
            "JOIN t_user u ON cc.teacher_id = u.id " +
            "WHERE cc.teacher_id = #{teacherId} AND cc.semester_id = #{semesterId}")
    List<Course> selectByTeacherId(@Param("teacherId") Long teacherId,
                                   @Param("semesterId") Long semesterId);

    @Select("SELECT * FROM t_course WHERE id = #{id}")
    Course selectById(Long id);

    @Select("SELECT * FROM t_course ORDER BY code")
    List<Course> selectAll();

    @Insert("INSERT INTO t_course (name, code, credit, hours, exam_type, usual_ratio, exam_ratio) " +
            "VALUES (#{name}, #{code}, #{credit}, #{hours}, #{examType}, #{usualRatio}, #{examRatio})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);

    @Insert("INSERT INTO t_course_class (course_id, class_id, teacher_id, semester_id) " +
            "VALUES (#{courseId}, #{classId}, #{teacherId}, #{semesterId})")
    int bindClass(@Param("courseId") Long courseId, @Param("classId") Long classId,
                  @Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);

    @Select("SELECT COUNT(*) FROM t_course_class " +
            "WHERE teacher_id = #{teacherId} AND course_id = #{courseId} " +
            "AND class_id = #{classId} AND semester_id = #{semesterId}")
    int countTeacherBinding(@Param("teacherId") Long teacherId,
                            @Param("courseId") Long courseId,
                            @Param("classId") Long classId,
                            @Param("semesterId") Long semesterId);
}
