package com.grade.service;

import com.grade.entity.User;
import com.grade.mapper.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Service
public class AdminService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;
    private final SemesterMapper semesterMapper;
    private final GradeMapper gradeMapper;

    public AdminService(UserMapper userMapper, StudentMapper studentMapper,
                        PasswordEncoder passwordEncoder, SemesterMapper semesterMapper,
                        GradeMapper gradeMapper) {
        this.userMapper = userMapper;
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
        this.semesterMapper = semesterMapper;
        this.gradeMapper = gradeMapper;
    }

    @Transactional
    public Map<String, Object> importStudents(InputStream inputStream) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> failDetails = new ArrayList<>();
        int total = 0, success = 0, fail = 0;

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                total++;
                try {
                    String username = getCellString(row, 0);  // 学号
                    String realName = getCellString(row, 1);   // 姓名
                    String gender = getCellString(row, 2);     // 性别
                    String className = getCellString(row, 3);  // 班级名
                    String initPassword = getCellString(row, 4); // 初始密码

                    if (username == null || username.isEmpty()) {
                        fail++;
                        failDetails.add(Map.of("row", i + 1, "reason", "学号为空"));
                        continue;
                    }

                    // 检查学号已存在
                    User exist = userMapper.selectByUsername(username);
                    if (exist != null) {
                        fail++;
                        failDetails.add(Map.of("row", i + 1, "reason", "学号已存在"));
                        continue;
                    }

                    // 创建用户
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode(
                            initPassword != null ? initPassword : "Student@123"));
                    user.setRealName(realName != null ? realName : username);
                    user.setRole("STUDENT");
                    user.setGender("男".equals(gender) ? "MALE" : "FEMALE");
                    user.setStatus("ACTIVE");
                    userMapper.insert(user);

                    // 创建学生记录（班级匹配通过班级名，简化处理用1作为默认班级）
                    com.grade.entity.Student s = new com.grade.entity.Student();
                    s.setUserId(user.getId());
                    s.setStudentNo(username);
                    s.setClassId(1L);
                    studentMapper.insert(s);

                    success++;
                } catch (Exception e) {
                    fail++;
                    failDetails.add(Map.of("row", i + 1, "reason", e.getMessage()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Excel文件解析失败: " + e.getMessage());
        }

        result.put("totalCount", total);
        result.put("successCount", success);
        result.put("failCount", fail);
        result.put("failDetails", failDetails);
        return result;
    }

    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        // 当前学期信息
        dashboard.put("currentSemester",
                semesterMapper.selectCurrent());
        return dashboard;
    }

    private String getCellString(Row row, int col) {
        if (row.getCell(col) == null) return null;
        try {
            return row.getCell(col).getStringCellValue().trim();
        } catch (Exception e) {
            double d = row.getCell(col).getNumericCellValue();
            return String.valueOf((long) d);
        }
    }
}
