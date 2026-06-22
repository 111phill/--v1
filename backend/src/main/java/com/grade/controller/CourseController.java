package com.grade.controller;

import com.grade.dto.ApiResponse;
import com.grade.entity.Course;
import com.grade.entity.Semester;
import com.grade.entity.Student;
import com.grade.mapper.CourseMapper;
import com.grade.mapper.SemesterMapper;
import com.grade.mapper.StudentMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CourseController {

    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;
    private final SemesterMapper semesterMapper;

    public CourseController(CourseMapper courseMapper, StudentMapper studentMapper,
                            SemesterMapper semesterMapper) {
        this.courseMapper = courseMapper;
        this.studentMapper = studentMapper;
        this.semesterMapper = semesterMapper;
    }

    @GetMapping("/courses")
    public ApiResponse<List<Course>> getCourses(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long semesterId) {
        if (teacherId != null) {
            if (semesterId == null) {
                Semester current = semesterMapper.selectCurrent();
                semesterId = current != null ? current.getId() : 4L;
            }
            return ApiResponse.success(
                    courseMapper.selectByTeacherId(teacherId, semesterId));
        }
        return ApiResponse.success(courseMapper.selectAll());
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<Course> getCourse(@PathVariable Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            return ApiResponse.error(404, "课程不存在");
        }
        return ApiResponse.success(course);
    }

    @GetMapping("/students")
    public ApiResponse<List<Student>> getStudents(@RequestParam Long classId) {
        return ApiResponse.success(studentMapper.selectByClassId(classId));
    }

    @GetMapping("/semesters")
    public ApiResponse<List<Semester>> getSemesters() {
        return ApiResponse.success(semesterMapper.selectAll());
    }

    @GetMapping("/semesters/current")
    public ApiResponse<Semester> getCurrentSemester() {
        return ApiResponse.success(semesterMapper.selectCurrent());
    }
}
