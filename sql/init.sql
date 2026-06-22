-- ============================================
-- 大学生成绩管理系统 - 数据库初始化脚本
-- MySQL 8.0+, 字符集 utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS grade_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE grade_system;

SET NAMES utf8mb4;

-- ============================================
-- 1. 学院表
-- ============================================
CREATE TABLE IF NOT EXISTS t_college (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '学院名称',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '学院编号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '学院表';

-- ============================================
-- 2. 专业表
-- ============================================
CREATE TABLE IF NOT EXISTS t_major (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '专业名称',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '专业编号',
    college_id BIGINT NOT NULL COMMENT '所属学院',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (college_id) REFERENCES t_college(id)
) COMMENT '专业表';

-- ============================================
-- 3. 班级表
-- ============================================
CREATE TABLE IF NOT EXISTS t_class (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '班级名称(如 计科2101)',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '班级编号',
    major_id BIGINT NOT NULL COMMENT '所属专业',
    grade_year INT NOT NULL COMMENT '入学年份',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (major_id) REFERENCES t_major(id)
) COMMENT '班级表';

-- ============================================
-- 4. 用户表（统一存储管理员/教师/学生）
-- ============================================
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名(学号/工号/admin)',
    password VARCHAR(200) NOT NULL COMMENT 'BCrypt加密密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role ENUM('ADMIN','TEACHER','STUDENT') NOT NULL COMMENT '角色',
    gender ENUM('MALE','FEMALE') COMMENT '性别',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    status ENUM('ACTIVE','DISABLED') DEFAULT 'ACTIVE' COMMENT '账号状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

-- ============================================
-- 5. 学生扩展表
-- ============================================
CREATE TABLE IF NOT EXISTS t_student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL COMMENT '关联用户ID',
    student_no VARCHAR(50) UNIQUE NOT NULL COMMENT '学号',
    class_id BIGINT NOT NULL COMMENT '所属班级',
    FOREIGN KEY (user_id) REFERENCES t_user(id),
    FOREIGN KEY (class_id) REFERENCES t_class(id)
) COMMENT '学生扩展表';

-- ============================================
-- 6. 学期表
-- ============================================
CREATE TABLE IF NOT EXISTS t_semester (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '学期名称(如 2025-2026-2)',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    grade_entry_start DATETIME COMMENT '成绩录入开始时间',
    grade_entry_end DATETIME COMMENT '成绩录入截止时间',
    is_current TINYINT DEFAULT 0 COMMENT '是否当前学期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '学期表';

-- ============================================
-- 7. 课程表
-- ============================================
CREATE TABLE IF NOT EXISTS t_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '课程编号',
    credit DECIMAL(3,1) NOT NULL COMMENT '学分',
    hours INT NOT NULL COMMENT '总学时',
    exam_type ENUM('EXAM','PASS') DEFAULT 'EXAM' COMMENT '考核方式(考试/考查)',
    usual_ratio DECIMAL(3,2) DEFAULT 0.30 COMMENT '平时成绩占比',
    exam_ratio DECIMAL(3,2) DEFAULT 0.70 COMMENT '期末成绩占比',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '课程表';

-- ============================================
-- 8. 课程-班级-教师关联表
-- ============================================
CREATE TABLE IF NOT EXISTS t_course_class (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL COMMENT '授课教师(关联t_user.id)',
    semester_id BIGINT NOT NULL,
    UNIQUE KEY uk_course_class_sem (course_id, class_id, semester_id),
    FOREIGN KEY (course_id) REFERENCES t_course(id),
    FOREIGN KEY (class_id) REFERENCES t_class(id),
    FOREIGN KEY (teacher_id) REFERENCES t_user(id),
    FOREIGN KEY (semester_id) REFERENCES t_semester(id)
) COMMENT '课程-班级-教师关联表';

-- ============================================
-- 9. 成绩表
-- ============================================
CREATE TABLE IF NOT EXISTS t_grade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生(关联t_user.id)',
    course_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    semester_id BIGINT NOT NULL,
    usual_score DECIMAL(5,2) COMMENT '平时成绩(0-100)',
    exam_score DECIMAL(5,2) COMMENT '期末成绩(0-100)',
    total_score DECIMAL(5,2) COMMENT '总评成绩',
    gpa DECIMAL(3,1) COMMENT '绩点(4.0制)',
    status ENUM('DRAFT','SUBMITTED','PUBLISHED') DEFAULT 'DRAFT' COMMENT '状态',
    entry_teacher_id BIGINT COMMENT '录入教师',
    class_rank INT COMMENT '班级排名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stu_course_sem (student_id, course_id, semester_id),
    INDEX idx_course_class_sem (course_id, class_id, semester_id),
    INDEX idx_student_sem (student_id, semester_id),
    FOREIGN KEY (student_id) REFERENCES t_user(id),
    FOREIGN KEY (course_id) REFERENCES t_course(id),
    FOREIGN KEY (class_id) REFERENCES t_class(id),
    FOREIGN KEY (semester_id) REFERENCES t_semester(id),
    FOREIGN KEY (entry_teacher_id) REFERENCES t_user(id)
) COMMENT '成绩表';

-- ============================================
-- 10. 成绩修改申请表
-- ============================================
CREATE TABLE IF NOT EXISTS t_grade_modification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL COMMENT '申请人',
    old_score DECIMAL(5,2) COMMENT '原总评成绩',
    new_score DECIMAL(5,2) COMMENT '新总评成绩',
    reason VARCHAR(500) NOT NULL COMMENT '修改原因',
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    reviewer_id BIGINT COMMENT '审核人',
    review_comment VARCHAR(500) COMMENT '审核意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    review_time DATETIME,
    FOREIGN KEY (grade_id) REFERENCES t_grade(id),
    FOREIGN KEY (teacher_id) REFERENCES t_user(id),
    FOREIGN KEY (reviewer_id) REFERENCES t_user(id)
) COMMENT '成绩修改申请表';

-- ============================================
-- 11. 操作审计日志表
-- ============================================
CREATE TABLE IF NOT EXISTS t_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT NOT NULL COMMENT '操作人',
    op_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    entity_type VARCHAR(50) COMMENT '实体类型',
    entity_id BIGINT COMMENT '实体ID',
    old_value JSON COMMENT '操作前值',
    new_value JSON COMMENT '操作后值',
    op_ip VARCHAR(50) COMMENT '操作IP',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_op_time (create_time),
    INDEX idx_op_type (op_type),
    FOREIGN KEY (operator_id) REFERENCES t_user(id)
) COMMENT '操作审计日志表';

-- ============================================
-- 12. 学业预警规则表
-- ============================================
CREATE TABLE IF NOT EXISTS t_alert_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '规则名称',
    level VARCHAR(20) NOT NULL COMMENT '预警级别(YELLOW/ORANGE/RED)',
    fail_credit_threshold DECIMAL(5,1) NOT NULL COMMENT '不及格学分阈值',
    description VARCHAR(200) COMMENT '规则描述',
    is_active TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '学业预警规则表';

-- ============================================
-- 13. 学业预警记录表
-- ============================================
CREATE TABLE IF NOT EXISTS t_alert_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    rule_id BIGINT NOT NULL,
    semester_id BIGINT NOT NULL,
    fail_credits DECIMAL(5,1) NOT NULL COMMENT '当前不及格学分',
    level VARCHAR(20) NOT NULL COMMENT '实际预警级别',
    is_read TINYINT DEFAULT 0 COMMENT '学生是否已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES t_user(id),
    FOREIGN KEY (rule_id) REFERENCES t_alert_rule(id),
    FOREIGN KEY (semester_id) REFERENCES t_semester(id)
) COMMENT '学业预警记录表';

-- ============================================
-- 初始测试数据
-- ============================================

-- 密码: 123456 (BCrypt加密)
INSERT INTO t_user (username, password, real_name, role, gender, status) VALUES
('admin', '$2b$10$7JPotDTqxjQyTerEXL/xqOV9QGWJEM1jv1/W.45jfZI89Dm37C2vO', '系统管理员', 'ADMIN', 'MALE', 'ACTIVE'),
('T001', '$2b$10$7JPotDTqxjQyTerEXL/xqOV9QGWJEM1jv1/W.45jfZI89Dm37C2vO', '张教授', 'TEACHER', 'MALE', 'ACTIVE'),
('T002', '$2b$10$7JPotDTqxjQyTerEXL/xqOV9QGWJEM1jv1/W.45jfZI89Dm37C2vO', '李副教授', 'TEACHER', 'FEMALE', 'ACTIVE'),
('T003', '$2b$10$7JPotDTqxjQyTerEXL/xqOV9QGWJEM1jv1/W.45jfZI89Dm37C2vO', '王讲师', 'TEACHER', 'MALE', 'ACTIVE');

-- 学院
INSERT INTO t_college (id, name, code) VALUES
(1, '计算机与信息工程学院', 'CS'),
(2, '软件学院', 'SE');

-- 专业
INSERT INTO t_major (id, name, code, college_id) VALUES
(1, '计算机科学与技术', 'CS01', 1),
(2, '软件工程', 'SE01', 2),
(3, '网络工程', 'NET01', 1);

-- 班级: 计科2601→计算机科学与技术, 软工2601→软件工程, 网工2601→网络工程
INSERT INTO t_class (id, name, code, major_id, grade_year) VALUES
(1, '计科2601', 'CS202601', 1, 2026),
(2, '软工2601', 'SE202601', 2, 2026),
(3, '网工2601', 'NET202601', 3, 2026);

-- 批量创建学生用户(40人, 学号2026001-2026040)
INSERT INTO t_user (username, password, real_name, role, gender, status)
SELECT
    CONCAT('2026', LPAD(n, 3, '0')),
    '$2b$10$7JPotDTqxjQyTerEXL/xqOV9QGWJEM1jv1/W.45jfZI89Dm37C2vO',
    CONCAT('学生', n),
    'STUDENT',
    IF(n % 3 = 0, 'FEMALE', 'MALE'),
    'ACTIVE'
FROM (SELECT ones.n + tens.n * 10 + 1 AS n
      FROM (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
            UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) ones,
           (SELECT 0 AS n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) tens
      LIMIT 40) nums;

-- 分配学生到班级(计科2601:14人, 软工2601:13人, 网工2601:13人)
INSERT INTO t_student (user_id, student_no, class_id)
SELECT u.id, u.username,
    CASE
        WHEN CAST(SUBSTRING(u.username, 5) AS UNSIGNED) <= 14 THEN 1
        WHEN CAST(SUBSTRING(u.username, 5) AS UNSIGNED) <= 27 THEN 2
        ELSE 3
    END
FROM t_user u WHERE u.role = 'STUDENT' AND u.username LIKE '2026%';

-- 学期
INSERT INTO t_semester (id, name, start_date, end_date, grade_entry_start, grade_entry_end, is_current) VALUES
(1, '2024-2025-1', '2024-09-01', '2025-01-15', '2025-01-01 00:00:00', '2025-01-20 23:59:59', 0),
(2, '2024-2025-2', '2025-02-17', '2025-07-05', '2025-06-20 00:00:00', '2025-07-15 23:59:59', 0),
(3, '2025-2026-1', '2025-09-01', '2026-01-15', '2026-01-01 00:00:00', '2026-01-22 23:59:59', 0),
(4, '2025-2026-2', '2026-02-16', '2026-07-03', '2026-06-15 00:00:00', '2026-07-12 23:59:59', 1);

-- 课程(5门, 不同学分)
INSERT INTO t_course (id, name, code, credit, hours, exam_type, usual_ratio, exam_ratio) VALUES
(1, 'Java Web应用开发', 'CS310', 3.0, 48, 'EXAM', 0.40, 0.60),
(2, '数据结构与算法', 'CS201', 4.0, 64, 'EXAM', 0.30, 0.70),
(3, '数据库原理与应用', 'CS302', 3.0, 48, 'EXAM', 0.30, 0.70),
(4, '操作系统原理', 'CS401', 4.0, 64, 'EXAM', 0.30, 0.70),
(5, '计算机网络', 'CS305', 3.0, 48, 'EXAM', 0.40, 0.60);

-- 课程-班级-教师绑定【学期3: 2025-2026-1】(用于已有成绩)
-- T001=张教授(2), T002=李副教授(3), T003=王讲师(4)
INSERT INTO t_course_class (course_id, class_id, teacher_id, semester_id) VALUES
(1, 1, 2, 3),  -- Java Web → 计科2601, 张教授
(1, 2, 4, 3),  -- Java Web → 软工2601, 王讲师
(2, 1, 3, 3),  -- 数据结构 → 计科2601, 李副教授
(2, 2, 2, 3),  -- 数据结构 → 软工2601, 张教授
(3, 2, 4, 3),  -- 数据库 → 软工2601, 王讲师
(3, 3, 3, 3),  -- 数据库 → 网工2601, 李副教授
(4, 1, 2, 3),  -- 操作系统 → 计科2601, 张教授
(4, 3, 3, 3),  -- 操作系统 → 网工2601, 李副教授
(5, 3, 4, 3),  -- 计算机网络 → 网工2601, 王讲师
(5, 1, 2, 3);  -- 计算机网络 → 计科2601, 张教授

-- 课程-班级-教师绑定【学期4: 2025-2026-2】(当前学期, 暂无成绩)
INSERT INTO t_course_class (course_id, class_id, teacher_id, semester_id) VALUES
(1, 1, 2, 4),
(1, 2, 4, 4),
(2, 1, 3, 4),
(3, 2, 4, 4),
(4, 1, 2, 4),
(5, 3, 4, 4);

-- ============================================
-- 【学期3 成绩数据】为每个绑定插入全班的成绩
-- ============================================

-- Java Web (course=1) → 计科2601 (class=1), 张教授(T001, user_id=2)
INSERT INTO t_grade (student_id, course_id, class_id, semester_id, usual_score, exam_score, total_score, gpa, status, entry_teacher_id)
SELECT u.id, 1, 1, 3,
    ROUND(70 + RAND() * 25, 1),
    ROUND(55 + RAND() * 35, 1),
    0, 0, 'SUBMITTED', 2
FROM t_student s JOIN t_user u ON s.user_id = u.id WHERE s.class_id = 1;

-- Java Web (course=1) → 软工2601 (class=2), 王讲师(T003, user_id=4)
INSERT INTO t_grade (student_id, course_id, class_id, semester_id, usual_score, exam_score, total_score, gpa, status, entry_teacher_id)
SELECT u.id, 1, 2, 3,
    ROUND(68 + RAND() * 28, 1),
    ROUND(52 + RAND() * 38, 1),
    0, 0, 'SUBMITTED', 4
FROM t_student s JOIN t_user u ON s.user_id = u.id WHERE s.class_id = 2;

-- 数据结构 (course=2) → 计科2601, 李副教授(T002, user_id=3)
INSERT INTO t_grade (student_id, course_id, class_id, semester_id, usual_score, exam_score, total_score, gpa, status, entry_teacher_id)
SELECT u.id, 2, 1, 3,
    ROUND(65 + RAND() * 30, 1),
    ROUND(48 + RAND() * 45, 1),
    0, 0, 'SUBMITTED', 3
FROM t_student s JOIN t_user u ON s.user_id = u.id WHERE s.class_id = 1;

-- 数据结构 (course=2) → 软工2601, 张教授
INSERT INTO t_grade (student_id, course_id, class_id, semester_id, usual_score, exam_score, total_score, gpa, status, entry_teacher_id)
SELECT u.id, 2, 2, 3,
    ROUND(62 + RAND() * 33, 1),
    ROUND(45 + RAND() * 48, 1),
    0, 0, 'SUBMITTED', 2
FROM t_student s JOIN t_user u ON s.user_id = u.id WHERE s.class_id = 2;

-- 数据库 (course=3) → 软工2601, 王讲师
INSERT INTO t_grade (student_id, course_id, class_id, semester_id, usual_score, exam_score, total_score, gpa, status, entry_teacher_id)
SELECT u.id, 3, 2, 3,
    ROUND(70 + RAND() * 25, 1),
    ROUND(55 + RAND() * 38, 1),
    0, 0, 'SUBMITTED', 4
FROM t_student s JOIN t_user u ON s.user_id = u.id WHERE s.class_id = 2;

-- 数据库 (course=3) → 网工2601, 李副教授
INSERT INTO t_grade (student_id, course_id, class_id, semester_id, usual_score, exam_score, total_score, gpa, status, entry_teacher_id)
SELECT u.id, 3, 3, 3,
    ROUND(68 + RAND() * 28, 1),
    ROUND(50 + RAND() * 42, 1),
    0, 0, 'SUBMITTED', 3
FROM t_student s JOIN t_user u ON s.user_id = u.id WHERE s.class_id = 3;

-- 操作系统 (course=4) → 计科2601, 张教授
INSERT INTO t_grade (student_id, course_id, class_id, semester_id, usual_score, exam_score, total_score, gpa, status, entry_teacher_id)
SELECT u.id, 4, 1, 3,
    ROUND(60 + RAND() * 35, 1),
    ROUND(42 + RAND() * 50, 1),
    0, 0, 'SUBMITTED', 2
FROM t_student s JOIN t_user u ON s.user_id = u.id WHERE s.class_id = 1;

-- 操作系统 (course=4) → 网工2601, 李副教授
INSERT INTO t_grade (student_id, course_id, class_id, semester_id, usual_score, exam_score, total_score, gpa, status, entry_teacher_id)
SELECT u.id, 4, 3, 3,
    ROUND(63 + RAND() * 32, 1),
    ROUND(45 + RAND() * 48, 1),
    0, 0, 'SUBMITTED', 3
FROM t_student s JOIN t_user u ON s.user_id = u.id WHERE s.class_id = 3;

-- 计算机网络 (course=5) → 网工2601, 王讲师
INSERT INTO t_grade (student_id, course_id, class_id, semester_id, usual_score, exam_score, total_score, gpa, status, entry_teacher_id)
SELECT u.id, 5, 3, 3,
    ROUND(66 + RAND() * 30, 1),
    ROUND(50 + RAND() * 42, 1),
    0, 0, 'SUBMITTED', 4
FROM t_student s JOIN t_user u ON s.user_id = u.id WHERE s.class_id = 3;

-- 计算机网络 (course=5) → 计科2601, 张教授
INSERT INTO t_grade (student_id, course_id, class_id, semester_id, usual_score, exam_score, total_score, gpa, status, entry_teacher_id)
SELECT u.id, 5, 1, 3,
    ROUND(68 + RAND() * 28, 1),
    ROUND(52 + RAND() * 40, 1),
    0, 0, 'SUBMITTED', 2
FROM t_student s JOIN t_user u ON s.user_id = u.id WHERE s.class_id = 1;

-- ============================================
-- 根据平时/期末成绩和课程占比, 计算总评成绩与绩点
-- ============================================
UPDATE t_grade g
JOIN t_course c ON g.course_id = c.id
SET g.total_score = ROUND(g.usual_score * c.usual_ratio + g.exam_score * c.exam_ratio, 1)
WHERE g.semester_id = 3;

UPDATE t_grade g SET gpa = CASE
    WHEN g.total_score >= 90 THEN 4.0
    WHEN g.total_score >= 85 THEN 3.7
    WHEN g.total_score >= 82 THEN 3.3
    WHEN g.total_score >= 78 THEN 3.0
    WHEN g.total_score >= 75 THEN 2.7
    WHEN g.total_score >= 72 THEN 2.3
    WHEN g.total_score >= 68 THEN 2.0
    WHEN g.total_score >= 64 THEN 1.5
    WHEN g.total_score >= 60 THEN 1.0
    ELSE 0
END WHERE g.semester_id = 3;

-- ============================================
-- 成绩修改申请(模拟教师申请修改, 等待管理员审核)
-- ============================================
-- 张教授申请修改学生2026001的Java Web成绩
INSERT INTO t_grade_modification (grade_id, teacher_id, old_score, new_score, reason, status, create_time)
SELECT g.id, 2, g.total_score, ROUND(g.total_score + 7.5, 1),
    '该生因参加学科竞赛缺交一次作业，现已补交并附竞赛获奖证明，申请将平时成绩上调',
    'PENDING', '2026-01-10 09:30:00'
FROM t_grade g JOIN t_user u ON g.student_id = u.id
WHERE u.username = '2026001' AND g.course_id = 1 AND g.semester_id = 3;

-- 李副教授申请修改学生2026015的数据结构成绩(期末分录入错误)
INSERT INTO t_grade_modification (grade_id, teacher_id, old_score, new_score, reason, status, create_time)
SELECT g.id, 3, 68.0, 82.0,
    '期末成绩录入时误将82分录为68分，经核查原始试卷确认，申请更正',
    'PENDING', '2026-01-12 14:20:00'
FROM t_grade g JOIN t_user u ON g.student_id = u.id
WHERE u.username = '2026015' AND g.course_id = 2 AND g.semester_id = 3;

-- 学业预警规则
INSERT INTO t_alert_rule (name, level, fail_credit_threshold, description) VALUES
('黄色预警', 'YELLOW', 12.0, '单学期不及格学分达到12学分'),
('橙色预警', 'ORANGE', 20.0, '累计不及格学分达到20学分'),
('红色预警', 'RED', 30.0, '累计不及格学分达到30学分，建议退学');
