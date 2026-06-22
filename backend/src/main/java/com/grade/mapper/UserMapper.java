package com.grade.mapper;

import com.grade.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM t_user WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM t_user WHERE id = #{id}")
    User selectById(Long id);

    @Insert("INSERT INTO t_user (username, password, real_name, role, gender, status) " +
            "VALUES (#{username}, #{password}, #{realName}, #{role}, #{gender}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE t_user SET status = #{status} WHERE id = #{userId}")
    int updateStatus(@Param("userId") Long userId, @Param("status") String status);

    @Update("UPDATE t_user SET password = #{encodedPassword} WHERE id = #{userId}")
    int resetPassword(@Param("userId") Long userId, @Param("encodedPassword") String encodedPassword);

    @Select("<script>" +
            "SELECT * FROM t_user WHERE 1=1 " +
            "<if test='role != null'>AND role = #{role}</if> " +
            "<if test='keyword != null'>AND (username LIKE CONCAT('%',#{keyword},'%') " +
            "OR real_name LIKE CONCAT('%',#{keyword},'%'))</if> " +
            "ORDER BY role, username LIMIT #{offset}, #{size}" +
            "</script>")
    List<User> selectByCondition(@Param("role") String role,
                                 @Param("keyword") String keyword,
                                 @Param("offset") int offset,
                                 @Param("size") int size);
}
