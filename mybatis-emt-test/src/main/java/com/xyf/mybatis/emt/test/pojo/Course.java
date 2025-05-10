package com.xyf.mybatis.emt.test.pojo;

import com.xyf.emt.common.field.Column;
import com.xyf.emt.common.index.PrimaryKey;
import com.xyf.emt.common.table.EntityTable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: 熊韵飞
 * @Description:
 */

@Data
@Accessors(chain = true)
@EntityTable("course")
public class Course {

    @Column(length = 5)
    private String courseId;

    @Column(length = 20)
    private String courseName;
}
