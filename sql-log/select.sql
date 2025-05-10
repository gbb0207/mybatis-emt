# 2
CREATE TABLE `student`
(
    `id`   varchar(12),
    `name` varchar(20),
    `age`  int(3)
);

CREATE TABLE `course`
(
    `course_id`   varchar(5),
    `course_name` varchar(20)
);

CREATE TABLE `enroll`
(
    `id`        varchar(12),
    `course_id` varchar(5),
    `grade`     int,
    CONSTRAINT `fk_enroll_student` FOREIGN KEY (`id`) REFERENCES `student` (`id`),
    CONSTRAINT `fk_enroll_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`)
);

INSERT INTO student (id, age, name)
VALUES ('230501050058', 18, '郑钰璐'),
       ('20230002', 21, '李四'),
       ('20230003', 19, '王五'),
       ('20230004', 22, '赵六'),
       ('20230005', 20, '孙七'),
       ('20230006', 23, '周八'),
       ('20230007', 18, '吴九'),
       ('20230008', 21, '郑十');

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
