# 2
#父键
CREATE TABLE `department`
(
    `dept_id`   VARCHAR(5) PRIMARY KEY,
    `dept_name` varchar(20)
);

CREATE TABLE `class`
(
    `class_id` varchar(5),
    `class_name` varchar(20),
    `dept_id` varchar(5),
    PRIMARY KEY (class_id)
);

CREATE TABLE `student`
(
    `id` VARCHAR(12) PRIMARY KEY,
    `name` varchar(20),
    `age`  int(3),
    `email` varchar(30),
    `phone` varchar(11),
    `class_id` varchar(5),
    FOREIGN KEY (class_id)
    REFERENCES class(class_id)
    ON DELETE CASCADE
    ON UPDATE SET NULL
);

#子键
CREATE TABLE `course`
(
    `course_id` varchar(5),
    `course_name` varchar(50),
    PRIMARY KEY (course_id)
);

CREATE TABLE `enroll`
(
    `id` varchar(12),
    `course_id` varchar(5),
    `grade`     int,
    `enroll_data` datetime,
    FOREIGN KEY (id)
    REFERENCES student(id)
    ON DELETE CASCADE #当父表记录被删除时，子表关联记录自动删除
    ON UPDATE SET NULL #当父表主键更新时，子表外键设为NULL
);

#2.1
CREATE TABLE `teacher`
(
    `teacher_id` VARCHAR(5) PRIMARY KEY,
    `teacher_name` varchar(20),
    `dept_id` varchar(5),
    `hire_data` datetime,
    FOREIGN KEY (dept_id)
    REFERENCES department(dept_id)
    ON DELETE CASCADE #当父表记录被删除时，子表关联记录自动删除
    ON UPDATE SET NULL #当父表主键更新时，子表外键设为NULL
);

CREATE TABLE `teachers`
(
    `teacher_id` varchar(5),
    `course_id`   varchar(5),
    `course_name` varchar(20),
    PRIMARY KEY (teacher_id, course_id),
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
    FOREIGN KEY (course_id) REFERENCES course(course_id)
);

CREATE TABLE `belongs`
(
    `teacher_id` varchar(5),
    `dept_id`   varchar(5),
    `dept_name` varchar(20),
    PRIMARY KEY (teacher_id),
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
    FOREIGN KEY (dept_id) REFERENCES department(dept_id)
);

#3.1
CREATE TABLE `project`
(
    `project_id` varchar(5),
    `project_name` varchar(20),
    `teacher_id` varchar(5),
    PRIMARY KEY (project_id)
);

CREATE TABLE `belong_to`
(
    `id`   varchar(12),
    `name` varchar(20),
    `class_id` varchar(5),
    `class_name` varchar(20),
    `dept_id` varchar(5)
);

CREATE TABLE `guides`
(
    `teacher_id` varchar(5),
    `teacher_name` varchar(20),
    `dept_id` varchar(5),
    `project_id` varchar(5),
    `project_name` varchar(20)
);

INSERT IGNORE INTO department (dept_id, dept_name)
VALUES ('D001', '计算机学院'),
       ('D002', '软件学院'),
       ('D003', '信息学院'),
       ('D004', '人工智能学院'),
       ('D005', '网络空间安全学院');

INSERT INTO class (class_id, class_name, dept_id)
VALUES ('CL001', '计算机一班', 'D001'),
       ('CL002', '软件一班', 'D002'),
       ('CL003', '信息一班', NULL),
       ('CL004', '计算机二班', 'D001'),
       ('CL005', '软件二班', NULL);

INSERT INTO student (id, age, name, class_id)
VALUES ('230501050058', 18, '郑钰璐', 'CL001'),
       ('20230002', 21, '李四', 'CL002'),
       ('20230003', 19, '王五', NULL),
       ('20230004', 22, '赵六', 'CL005'),
       ('20230005', 20, '孙七', 'CL002'),
       ('20230006', 23, '周八', NULL),
       ('20230007', 18, '吴九', 'CL003'),
       ('20230008', 21, '郑十', 'CL004');

INSERT INTO course (course_id, course_name)
VALUES ('C001', '数据库原理'),
       ('C002', '计算机网络'),
       ('C003', '操作系统'),
       ('C004', '数据结构'),
       ('C005', '人工智能'),
       ('C006', '软件工程');

INSERT INTO enroll (id, course_id, grade)
VALUES ('230501050058', 'C001', 85),
       ('20230002', 'C001', 78),
       ('20230002', 'C002', 82),
       ('20230003', 'C003', 88),
       ('20230004', 'C001', 90),
       ('20230004', 'C004', 76),
       ('20230005', 'C002', 85),
       ('20230005', 'C005', 92),
       ('20230006', 'C003', 79),
       ('20230006', 'C006', 87),
       ('20230007', 'C004', 83),
       ('20230008', 'C005', 91);

INSERT INTO teacher (teacher_id, teacher_name, dept_id)
VALUES ('T001', '张老师', 'D001'),
       ('T002', '李老师', 'D002'),
       ('T003', '王老师', 'D003'),
       ('T004', '赵老师', 'D004'),
       ('T005', '孙老师', 'D005');

INSERT INTO teachers (teacher_id, course_id, course_name)
VALUES ('T001', 'C001', '数据库原理'),
       ('T002', 'C002', '计算机网络'),
       ('T003', 'C003', '操作系统'),
       ('T004', 'C004', '数据结构'),
       ('T005', 'C005', '人工智能');

INSERT INTO belongs (teacher_id, dept_id, dept_name)
VALUES ('T001', 'D001', '计算机学院'),
       ('T002', 'D002', '软件学院'),
       ('T003', 'D003', '信息学院'),
       ('T004', 'D002', '人工智能学院'),
       ('T005', 'D001', '网络空间安全学院');


#3
UPDATE student
SET
    email = '111@qq.com',
    phone = '123456789'
WHERE
    id = '230501050058';


DELETE FROM belongs WHERE teacher_id = 'T005';   # 先删除子表的数据
DELETE FROM teachers WHERE teacher_id = 'T005';  # 同上
DELETE FROM teacher WHERE teacher_id = 'T005';

# 3
SELECT *
FROM student
WHERE age >= 20;

# 4
SELECT s.name, c.course_name, e.grade
FROM student s,
     course c,
     enroll e
WHERE s.id = e.id
  AND c.course_id = e.course_id;

# 5
SELECT s.name, c.course_name
FROM student s,
     course c,
     enroll e
WHERE s.id = e.id
  AND c.course_id = e.course_id
  AND e.grade >= 85;

# 6
SELECT s.name, s.age
FROM student s,
     course c,
     enroll e
WHERE s.id = e.id
  AND c.course_id = e.course_id
  AND c.course_name = '数据库原理';

# 7
SELECT s.name
FROM student s
         LEFT JOIN
     enroll e ON s.id = e.id
WHERE e.id IS NULL;

#2.4
SELECT s.name, s.age
FROM student s
WHERE s.id IN (
    SELECT e.id
    FROM enroll e
    WHERE e.course_id IN (
        SELECT c.course_id
        FROM course c
        WHERE c.course_name IN ('数据库原理', '计算机网络')
    )
);

#2.5
SELECT s.name
FROM student s
WHERE s.id NOT IN (
    SELECT e.id
    FROM enroll e
    WHERE e.course_id IN (
        SELECT c.course_id
        FROM course c
        WHERE c.course_name = '人工智能'
    )
);

#2.6
SELECT t.teacher_id,
       c.course_name
FROM teacher t
JOIN teachers ts ON t.teacher_id = ts.teacher_id
JOIN course c ON ts.course_id = c.course_id
JOIN department d on t.dept_id = d.dept_id
WHERE t.dept_id = '计算机学院';

UPDATE  teacher
SET hire_data = '2023-01-01'
WHERE teacher_id IN(
    SELECT  ts.teacher_id
    FROM teachers ts
    JOIN course c ON ts.course_id = c.course_id
    JOIN department d ON teacher.dept_id = d.dept_id
    WHERE d.dept_name = '计算机学院'
);

#3.2