package com.xyf.emt.core.strategy.mysql;

import com.xyf.emt.core.strategy.mysql.data.MysqlColumnMetadata;
import com.xyf.emt.core.strategy.mysql.data.dbdata.InformationSchemaColumn;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ColumnPositionHelper {

    /**
     * 将数据库列的位置改为和实体类列的位置一样
     * @param dbColumns 数据库字段集合
     * @param expectPositions 实体类/期望字段集合
     */
    public static void generateChangePosition(List<InformationSchemaColumn> dbColumns, List<MysqlColumnMetadata> expectPositions) {

        /*
            传入：(tableColumnList, mysqlColumnMetadataList) =》数据库列 dbColumns/realPositions，实体类列 expectPositions/entityColumnsSet
            注意，只有数据库列才有 ordinal_position；position 的位置是按实体类中顺序每位自动增加 1 生成的位置
         */

        // 数据库中实际字段位置集合，List<列名, 位置数>
        List<InformationSchemaColumnPosition> realPositions = dbColumns.stream()
                .map(col -> new InformationSchemaColumnPosition(col.getColumnName(), col.getOrdinalPosition()))
                .collect(Collectors.toList());

        // 实体类中的字段，Set<列名>，Set 方便去重，直接调 contains() 就能找重复值！
        Set<String> entityColumnsSet = expectPositions.stream().map(MysqlColumnMetadata::getName).collect(Collectors.toSet());

        // 1.删除实体类有但是数据库没有的字段
        // 这个集合的作用：保存某个期望字段前有几个需要移走的字段数量
        List<InformationSchemaColumnPosition> removeColumns = new ArrayList<>(realPositions.size());    // 我称之为废弃集合
        // 这个循环只用于重置字段位置：字段不存在，存入废弃集合；存在，废弃集合中的数量就是需要往前移的位数
        /*
            例1：
            数据库列：["A", "B", "C", "D", "E", "F", "G"] 实体类或期望列：["A", "C", "D", "G"]
            原始位数：[ 1 ,  2 ,  3 ,  4 ,  5 ,  6 ,  7 ] 实体类期望位数：[ 1 ,  2 ,  3 ,  4 ]
            ----------------------------------------------------------------------------
            删除的列：[     "B",           "E", "F"     ]
            期望的列：["A",      "C", "D",           "G"]
            实际位置：["A", "C", "D", "G"]
          * 原始位数：[ 1 ,  3 ,  4 ,  7 ]
          * 删除位数：[ 0 ,  1 ,  1 ,  3 ] =》removeColumns 记录了 B（B没了CD前移1位）、E、F（BEF没了G前移3位）
            两者相减：[ 1 ,  2 ,  3 ,  4 ] =》数据库列修改后的位置信息
            ============================================================================================================
            例2：
            数据库列：["A", "B", "C", "D", "E", "F", "G"] 实体类或期望列：["C", "D", "A", "G"]
            原始位数：[ 1 ,  2 ,  3 ,  4 ,  5 ,  6 ,  7 ] 实体类期望位数：[ 1 ,  2 ,  3 ,  4 ] =》次数无法直接修改成这样的期望位数！
            ----------------------------------------------------------------------------
            删除的列：[     "B",           "E", "F"     ]
            期望的列：["C",      "D", "A",           "G"] =》这里杂乱无章？但是期望列是一个 Set，无序！说明这里只是用于去除实体类不存在列
            注：实体类集合.存在(数据库列)
            遍历过程：【实体类集合有：A、C、D、G】
                括号里是数据库列！移动的也是数据库列的原始位置！
                1.实体类集合.存在("A") =》存在 =》废弃集合为空 =》啥也不做
                2.实体类集合.存在("B") =》不存在，将 B 存入废弃集合，此时 size=1
                3.实体类集合.存在("C") =》存在 =》废弃集合不为空 =》C 位置前移 size=1 位，3-1=2
                4.实体类集合.存在("D") =》存在 =》废弃集合不为空 =》D 位置前移 size=1 位，4-1=3
                5.实体类集合.存在("E") =》不存在，将 E 存入废弃集合，此时 size=2
                6.实体类集合.存在("F") =》不存在，将 F 存入废弃集合，此时 size=3
                7.实体类集合.存在("G") =》存在 =》废弃集合不为空 =》G 位置前移 size=3 位，7-3=4
            可以看出，修改的只是数据库列的位置
         */
        for (InformationSchemaColumnPosition realPosition : realPositions) {
            if (!entityColumnsSet.contains(realPosition.getColumnName())) { // 用 Set 去重：实体类.存在(数据库列)
                removeColumns.add(realPosition);
                continue;   // 添加实体类中不存在但数据库字段存在的列
            }
            if (!removeColumns.isEmpty()) { // 往前移已经确定实体中不存在的列的位数，
                realPosition.setOrdinalPosition(realPosition.getOrdinalPosition() - removeColumns.size());
            }
        }
        realPositions.removeAll(removeColumns); // 这里才实际删除实体类有但是数据库没有的字段

        // 2.下方将向（已经删除了实体类中不存在的列的）数据库字段集合中，在最后的位置添加数据库中不存在的列
        // 例如：处理好的数据库列：["A", "B", "C"] =》实体类或期望列：["A", "B", "C", "D", "E"]，向前者添加不存在列
        // 这里的 realPositions 已经删除了实体类中不存在的列，用 Set 方便去重
        Set<String> dbColumnsSet = realPositions.stream().map(InformationSchemaColumnPosition::getColumnName).collect(Collectors.toSet());
        // 实体类列 expectPositions（可能存在数据库列不存在的）
        for (MysqlColumnMetadata expectPosition : expectPositions) {
            if (!dbColumnsSet.contains(expectPosition.getName())) {
                realPositions.add(new InformationSchemaColumnPosition(expectPosition.getName(), realPositions.size() + 1));
            }
        }

        // 3.下方将替换数据库字段位置成实体类字段位置
        /*
            数据库列：["A", "B", "C", "D", "E", "F", "G"] 实体类或期望列：["C", "D", "A", "G", "X", "Y"]
            在第一步中，已经将数据库列改为了：["A", "C", "D", "G"]
            在第二步中，已经将数据库列改为了：["A", "C", "D", "G", "X", "Y"]
            此时第三步，需要将数据库列位置改成和期望列一样的位置！
            即：["A", "C", "D", "G", "X", "Y"] =》["C", "D", "A", "G", "X", "Y"]
         */
        // 这里的 realPositions 已经加入了实体类中有但是数据库中没有的字段
        // 数据库字段列表 变形：《列名，列位置》
        Map<String, InformationSchemaColumnPosition> dbColumnPositionMap = realPositions.stream()
                .collect(Collectors.toMap(InformationSchemaColumnPosition::getColumnName, Function.identity()));
        // 实体类字段列表 变形：《列名，实体字段描述（原始形参）》
        Map<String, MysqlColumnMetadata> columnMetadataMap = expectPositions.stream()
                .collect(Collectors.toMap(MysqlColumnMetadata::getName, Function.identity()));
        /*
            思路：不是交换！不是交换！不是交换！先剔除，再插入，再修改位置数。
            数据库/实际/real   = ["A", "C", "D", "G", "X", "Y"] 经过两轮后变成这样，之后下面每层循环都会改变
            实体类/期望/expect = ["C", "D", "A", "G", "X", "Y"] 始终不变！
           |---------------------------------------------------------------------------------------------------------------------------------
           | 注意：在按期望位置变更实际位置后，会有受影响的字段后移的，即位置数需要增加。
           | 位置受影响的字段 ∈ (剔除字段的重新插入位index, 剔除字段的原本位置\]
           | 例如下方第一次循环：C 往前移到第 index=0 位，C 原本位置在第 1 位，那么 C 变更后受影响的字段在区间 (0,1] 中，变更后需要修改的是第 1 位。即修改 A。
           |---------------------------------------------------------------------------------------------------------------------------------
            循环过程：一一对应（实际，期望）。【注意，每次循环都会改变实际列表，看最后那里的才是每次循环修改后的实际列表 ->】
                0.(A C) =》获取 C 实际位置，2 =》剔除C，变成["A",  \ , "D", "G", "X", "Y"] =》重新插入到遍历位置 index=0 =》["C", "A", "D", "G", "X", "Y"]
                1.(A D) =》获取 D 实际位置，3 =》剔除D，变成["C", "A",  \ , "G", "X", "Y"] =》重新插入到遍历位置 index=1 =》["C", "D", "A", "G", "X", "Y"]
                2.(A A) =》一致，跳出本次
                ......后续实际都一致，略过。
         */
        for (int index = 0; index < expectPositions.size(); index++) {
            // 当前位置期望的列名
            String expectColumnName = expectPositions.get(index).getName(); // 期望位置列名
            // 当前位置实际的列名
            String realColumnName = realPositions.get(index).getColumnName();   // 目前实际位置列名
            // 位置一致，啥也不管
            if (Objects.equals(expectColumnName, realColumnName)) {
                continue;
            }
            // 获取期望列名的实际位置
            Integer expectColumnNameRealPosition = dbColumnPositionMap.get(expectColumnName).getOrdinalPosition();
            // 从列表中删除
            realPositions.remove(expectColumnNameRealPosition - 1);
            // 再次插入新的位置（这里重新创建了一个 InformationSchemaColumnPosition，所以变更位的位置数修改了）
            realPositions.add(index, new InformationSchemaColumnPosition(expectColumnName, index + 1));
            // 该位置后面的列（直到删除的位置为止）的位置均+1
            for (int i = index + 1; i < expectColumnNameRealPosition; i++) {
                InformationSchemaColumnPosition columnPosition = realPositions.get(i);
                columnPosition.setOrdinalPosition(columnPosition.getOrdinalPosition() + 1); // 上面围住框里的内容
            }

            /*
                下方是 CREATE 语句中 字段 相关 SQL 片段中的 位置 参数 {position}，在最后一条，实际会被写进 sql 中
                `{columnName}` {typeAndLength} {qualifier} {character} {collate} {null} {default} {autoIncrement} {columnComment} {position}
                --------------------------------------------------------------------------------------------------------
                期望：["C", "D", "A", "G", "X", "Y"]      【这里下方 ↓ 是期望列顺序，即按左边这个列表的顺序】
                index=0，第一轮：["A", "C", "D", "G", "X", "Y"] =》"C" 前 ""，newPreColumn=""。
                index=1，第二轮：["C", "A", "D", "G", "X", "Y"] =》"D" 前 realPositions.get(1-1) 即第0位 "C"，newPreColumn="C"。
                index=2，第三轮：["C", "D", "A", "G", "X", "Y"] =》"A" 前 realPositions.get(2-1) 即第1位 "D"，newPreColumn="D"。
                index=3，第四轮：["C", "D", "A", "G", "X", "Y"] =》"G" 前 realPositions.get(3-1) 即第2位 "A"，newPreColumn="A"。
                index=4，第五轮：["C", "D", "A", "G", "X", "Y"] =》位置没有变化，直接跳出本次循环。这里到了 "X"，他前面一直是 "G"，没有新的前字段 newPreColumn。
                ......后面完全一样，就略了，会和上面一条直接在最开始 continue。
                那么为什么不直接按期望列直接设置这个 newPreColumn 呢？？
                实际就是避免重复遍历 expectPositions，这里 columnMetadataMap 本身就是 expectPositions 直接变形得来的 Map，
                目的就是为了减少遍历。这里 newPreColumn 直接按 实际列表 取位置就行了，时间复杂度 O(1).
                如果要遍历，那么 expectPositions.forEach() 至少是 O(n)。
             */
            // 最重要的位置，为 mysqlColumnMetadataList 中的每个对象设置 newPreColumn 值。
            // 注意，这里还在第二层 for 中，当位置一致，不会产生 newPreColumn！而是直接 continue。
            if (index == 0) {
                // 如果新位置在最前面
                columnMetadataMap.get(expectColumnName).setNewPreColumn("");    // 期望列前方没有东西
            } else {
                // 取前一个字段的名字，声明排在他后面
                columnMetadataMap.get(expectColumnName).setNewPreColumn(realPositions.get(index - 1).getColumnName());
            }
        }
    }

    /**
     * 字段位置
     */
    @Data
    @AllArgsConstructor
    public static class InformationSchemaColumnPosition {   // 列名，位置
        private String columnName;
        private Integer ordinalPosition;
    }

    // 尝试
    public static void main(String[] args) {

        List<InformationSchemaColumn> dbColumns = Arrays.asList(db(1, "A"), db(2, "B"), db(3, "C"), db(4, "D"), db(5, "E"), db(6, "H"));
        List<MysqlColumnMetadata> expectPositions = Arrays.asList(app(1, "B"), app(2, "G"), app(3, "F"), app(4, "C"), app(5, "A"), app(6, "E"), app(7, "D"));

        generateChangePosition(dbColumns, expectPositions);

        for (MysqlColumnMetadata columnMetadata : expectPositions) {
            System.out.println(columnMetadata + " - " + columnMetadata.getName());
        }
    }


    private static InformationSchemaColumn db(int index, String name) { // 模拟数据库中的位置
        InformationSchemaColumn informationSchemaColumn = new InformationSchemaColumn();
        informationSchemaColumn.setColumnName(name);
        informationSchemaColumn.setOrdinalPosition(index);
        return informationSchemaColumn;
    }

    private static MysqlColumnMetadata app(int index, String org) { // 模拟实体类的位置
        MysqlColumnMetadata informationSchemaColumn = new MysqlColumnMetadata();
        informationSchemaColumn.setName(org);
        informationSchemaColumn.setPosition(index);
        return informationSchemaColumn;
    }
}
