package com.grade.mapper;

import com.grade.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentMapper {

    @Select("SELECT s.*, u.username, u.real_name, u.gender, cl.name AS class_name, " +
            "m.name AS major_name, co.name AS college_name " +
            "FROM t_student s " +
            "JOIN t_user u ON s.user_id = u.id " +
            "JOIN t_class cl ON s.class_id = cl.id " +
            "JOIN t_major m ON cl.major_id = m.id " +
            "JOIN t_college co ON m.college_id = co.id " +
            "WHERE s.user_id = #{userId}")
    Student selectByUserId(Long userId);

    @Select("SELECT s.*, u.username, u.real_name, u.gender, cl.name AS class_name, " +
            "m.name AS major_name, co.name AS college_name " +
            "FROM t_student s " +
            "JOIN t_user u ON s.user_id = u.id " +
            "JOIN t_class cl ON s.class_id = cl.id " +
            "JOIN t_major m ON cl.major_id = m.id " +
            "JOIN t_college co ON m.college_id = co.id " +
            "WHERE s.class_id = #{classId} ORDER BY s.student_no")
    List<Student> selectByClassId(Long classId);

    @Insert("INSERT INTO t_student (user_id, student_no, class_id) " +
            "VALUES (#{userId}, #{studentNo}, #{classId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Student student);

    @Select("SELECT s.*, u.username, u.real_name, u.gender, cl.name AS class_name " +
            "FROM t_student s " +
            "JOIN t_user u ON s.user_id = u.id " +
            "JOIN t_class cl ON s.class_id = cl.id " +
            "WHERE s.student_no = #{studentNo}")
    Student selectByStudentNo(String studentNo);
}
