package com.xyf.mybatis.emt.test.pojo;

import com.xyf.emt.common.field.Column;
import com.xyf.emt.common.table.EntityTable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: 熊韵飞
 * @Description:
 */

@Data
@Accessors(chain = true)
@EntityTable("enroll")
public class Enroll {

    @Column(length = 12)
    private String id;

    @Column(length = 5)
    private String courseId;

    private Integer grade;

}
