package com.grade.mapper;

import com.grade.entity.Semester;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SemesterMapper {

    @Select("SELECT * FROM t_semester WHERE id = #{id}")
    Semester selectById(Long id);

    @Select("SELECT * FROM t_semester WHERE is_current = 1 LIMIT 1")
    Semester selectCurrent();

    @Select("SELECT * FROM t_semester ORDER BY start_date DESC")
    List<Semester> selectAll();

    @Insert("INSERT INTO t_semester (name, start_date, end_date, " +
            "grade_entry_start, grade_entry_end, is_current) VALUES " +
            "(#{name}, #{startDate}, #{endDate}, #{gradeEntryStart}, #{gradeEntryEnd}, #{isCurrent})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Semester semester);

    @Update("UPDATE t_semester SET is_current = 0")
    int clearCurrent();

    @Update("UPDATE t_semester SET is_current = 1 WHERE id = #{id}")
    int setCurrent(Long id);
}
