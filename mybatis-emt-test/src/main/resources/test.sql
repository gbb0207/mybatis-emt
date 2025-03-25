# 1.根据表名查询表在库中是否存在
select * from information_schema.tables where table_name = 'test_table' and table_schema = (select database());

# 2.根据表名查询库中该表的字段结构/列等信息
select * from information_schema.columns where table_name = 'test_table' and table_schema = (select database()) order by ordinal_position asc;

# 3.查询指定表的所有主键和索引信息
SELECT * FROM information_schema.statistics WHERE table_name = 'test_table' and table_schema = (select database());

ALTER TABLE `test_table` MODIFY COLUMN `age` int(11)    NOT NULL   COMMENT '年龄'