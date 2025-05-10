package com.xyf.mybatis.emt.test.mybatis;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xyf.emt.starter.EnableEmtTest;
import com.xyf.mybatis.emt.test.mapper.CourseMapper;
import com.xyf.mybatis.emt.test.mapper.EnrollMapper;
import com.xyf.mybatis.emt.test.mapper.StudentMapper;
import com.xyf.mybatis.emt.test.mapper.TestMapper;
import com.xyf.mybatis.emt.test.pojo.Course;
import com.xyf.mybatis.emt.test.pojo.Enroll;
import com.xyf.mybatis.emt.test.pojo.Student;
import com.xyf.mybatis.emt.test.pojo.TestTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: 熊韵飞
 * @Description:
 */

@SpringBootTest
@EnableEmtTest
public class MybatisPlusTest {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private EnrollMapper enrollMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Test
    void addStudent() {
        Student student = new Student().setId("230501050058").setAge(18).setName("郑钰璐");
        Student student1 = new Student().setId("28230002").setAge(21).setName("李四");
        Student student2 = new Student().setId("28230083").setAge(19).setName("王五");
        Student student3 = new Student().setId("28230004").setAge(22).setName("赵六");
        Student student4 = new Student().setId("28230005").setAge(20).setName("孙七");
        Student student5 = new Student().setId("28230006").setAge(23).setName("周八");
        Student student6 = new Student().setId("28230087").setAge(18).setName("吴九");
        Student student7 = new Student().setId("28230008").setAge(21).setName("郑十");

        studentMapper.insert(student);
        studentMapper.insert(student1);
        studentMapper.insert(student2);
        studentMapper.insert(student3);
        studentMapper.insert(student4);
        studentMapper.insert(student5);
        studentMapper.insert(student6);
        studentMapper.insert(student7);
    }

    @Test
    void addCourse() {
        Course course = new Course().setCourseId("C001").setCourseName("数据库原理");
        Course course1 = new Course().setCourseId("C002").setCourseName("计算机网络");
        Course course2 = new Course().setCourseId("C003").setCourseName("操作系统");
        Course course3 = new Course().setCourseId("C004").setCourseName("数据结构");
        Course course4 = new Course().setCourseId("C005").setCourseName("人工智能");
        Course course5 = new Course().setCourseId("C006").setCourseName("软件工程");

        courseMapper.insert(course);
        courseMapper.insert(course1);
        courseMapper.insert(course2);
        courseMapper.insert(course3);
        courseMapper.insert(course4);
        courseMapper.insert(course5);
    }

    @Test
    void addEnroll() {
        Enroll c001 = new Enroll().setId("230501050058").setCourseId("C001").setGrade(85);
        Enroll c0011 = new Enroll().setId("20230002").setCourseId("C001").setGrade(78);
        Enroll c002 = new Enroll().setId("20230002").setCourseId("C002").setGrade(82);
        Enroll c003 = new Enroll().setId("20230003").setCourseId("C003").setGrade(88);
        Enroll c081 = new Enroll().setId("20230004").setCourseId("C081").setGrade(90);
        Enroll c004 = new Enroll().setId("20230004").setCourseId("C004").setGrade(76);
        Enroll c0021 = new Enroll().setId("20230005").setCourseId("C002").setGrade(85);
        Enroll c005 = new Enroll().setId("20230005").setCourseId("C005").setGrade(92);
        Enroll c0031 = new Enroll().setId("20230006").setCourseId("C003").setGrade(79);
        Enroll c006 = new Enroll().setId("20230006").setCourseId("C006").setGrade(87);
        Enroll c0041 = new Enroll().setId("20230007").setCourseId("C004").setGrade(83);
        Enroll c085 = new Enroll().setId("20230008").setCourseId("C085").setGrade(91);

        enrollMapper.insert(c001);
        enrollMapper.insert(c0011);
        enrollMapper.insert(c002);
        enrollMapper.insert(c003);
        enrollMapper.insert(c081);
        enrollMapper.insert(c004);
        enrollMapper.insert(c0021);
        enrollMapper.insert(c005);
        enrollMapper.insert(c0031);
        enrollMapper.insert(c006);
        enrollMapper.insert(c0041);
        enrollMapper.insert(c085);
    }

    @Test
    void select() {

    }
}
