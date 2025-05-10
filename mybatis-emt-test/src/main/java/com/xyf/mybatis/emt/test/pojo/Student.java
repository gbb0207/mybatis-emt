package com.xyf.mybatis.emt.test.pojo;

import com.xyf.emt.common.field.Column;
import com.xyf.emt.common.index.PrimaryKey;
import com.xyf.emt.common.table.EntityTable;
import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;

/**
 * @Author: 熊韵飞
 * @Description:
 */

@Data
@Accessors(chain = true)
@EntityTable("student")
public class Student {

    @Column(length = 12)
    private String id;

    @Column(length = 20)
    private String name;

    @Column(length = 3)
    private Integer age;
}
