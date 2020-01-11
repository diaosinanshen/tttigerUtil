package com.tttiger.sql;

import com.tttiger.sql.annotation.TableId;
import com.tttiger.sql.constant.Logical;
import com.tttiger.sql.constant.SqlResultType;
import com.tttiger.sql.constant.SqlType;
import com.tttiger.util.StringUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * @author 秦浩桐
 * @version 1.0
 * @date 2020/01/10 13:59
 */
public abstract class BaseMapper<T> implements Mapper<T> {

    /**
     * 处理类型
     */
    private Class<?> clazz;
    /**
     * 实体对应表名
     */
    private String tableName;
    /**
     * 拼接好的表字段属性
     */
    private String fieldsName;
    /**
     * 存在表字段映射的属性
     */
    private List<Field> fields;
    /**
     * 实体主键属性对应的表字段名
     */
    private String idName;
    /**
     * 实体主键属性
     */
    private Field id;
    /**
     * 实体逻辑删除属性对应表字段名
     */
    private String logicalName;
    /**
     * 实体逻辑删除属性
     */
    private Field logical;

    /**
     * sql执行器
     */
    private Executor<T> executor;


    @Override
    public List<T> select(Wrapper wrapper) {
        StringBuilder sql = new StringBuilder(SqlWord.SELECT);
        sql.append(fieldsName);
        sql.append(SqlWord.FROM).append(tableName);
        setWrapper(sql, wrapper);
        setLogicalCondition(sql, wrapper);
        SqlMethod sqlMethod = new SqlMethod(sql.toString(), SqlType.SELECT_TYPE, SqlResultType.EXPECT_RESULT_LIST);
        Result execute = executor.execute(sqlMethod);
        return (List<T>) execute.getResult();
    }

    protected void setLogicalCondition(StringBuilder sql, Wrapper wrapper) {
        if (logical != null && wrapper != null && StringUtil.isNotEmpty(wrapper.getConditionSql())) {
            sql.append(SqlWord.AND).append(logicalName).append(SqlWord.EQ).append(Logical.EXIST);
        } else if (logical != null && (wrapper == null || StringUtil.isEmpty(wrapper.getConditionSql()))) {
            sql.append(SqlWord.WHERE).append(logicalName).append(SqlWord.EQ).append(Logical.EXIST);
        }
    }

    @Override
    public T selectById(Serializable id) {
        StringBuilder sql = new StringBuilder(SqlWord.SELECT);
        sql.append(fieldsName);
        sql.append(SqlWord.FROM).append(tableName).append(SqlWord.WHERE)
                .append(idName).append(SqlWord.EQ).append(SqlUtil.convertToDBValue(id));
        SqlMethod sqlMethod = new SqlMethod(sql.toString(), SqlType.SELECT_TYPE, SqlResultType.EXPECT_RESULT_LIST);
        Result execute = executor.execute(sqlMethod);
        List resultList = (List) execute.getResult();
        if (resultList == null || resultList.isEmpty()) {
            return null;
        }
        if (resultList.size() > 1) {
            // 返回结果过多抛出异常
        }
        return (T) resultList.get(0);
    }

    @Override
    public boolean insert(T t) {
        StringBuilder sql = new StringBuilder(SqlWord.INSERT);
        sql.append(SqlWord.INTO);
        // 添加表名
        sql.append(tableName);
        // 获取存在表字段映射的属性
        sql.append(SqlUtil.getInsertDBStr(fields, t));
        SqlMethod sqlMethod = new SqlMethod(sql.toString(), SqlType.INSERT_TYPE, SqlResultType.EXPECT_RESULT_INT);
        Result execute = executor.execute(sqlMethod);
        return ((int) execute.getResult()) == 1;
    }

    @Override
    public boolean updateById(T t) {
        StringBuilder sql = new StringBuilder(SqlWord.UPDATE);
        sql.append(tableName).append(SqlWord.SET);
        for (Field temp : fields) {
            Object fieldValue = SqlUtil.getFieldSqlValue(temp, t);
            if (fieldValue == null || temp.isAnnotationPresent(TableId.class)) {
                continue;
            }
            sql.append(SqlUtil.getFieldSqlName(temp))
                    .append(SqlWord.EQ).append(SqlUtil.convertToDBValue(fieldValue)).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(SqlWord.WHERE).append(SqlUtil.getFieldAssignmentStr(id, t));
        SqlMethod sqlMethod = new SqlMethod(sql.toString(), SqlType.UPDATE_TYPE, SqlResultType.EXPECT_RESULT_INT);
        Result execute = executor.execute(sqlMethod);
        return ((int) execute.getResult()) == 1;
    }

    @Override
    public int update(T t, Wrapper wrapper) {
        StringBuilder sql = new StringBuilder(SqlWord.UPDATE);
        sql.append(tableName).append(SqlWord.SET);
        for (Field temp : fields) {
            Object fieldValue = SqlUtil.getFieldSqlValue(temp, t);
            if (fieldValue == null || temp.isAnnotationPresent(TableId.class)) {
                continue;
            }
            sql.append(SqlUtil.getFieldSqlName(temp))
                    .append(SqlWord.EQ).append(SqlUtil.convertToDBValue(fieldValue)).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        setWrapper(sql, wrapper);
        SqlMethod sqlMethod = new SqlMethod(sql.toString(), SqlType.UPDATE_TYPE, SqlResultType.EXPECT_RESULT_INT);
        Result execute = executor.execute(sqlMethod);
        return (int) execute.getResult();
    }

    @Override
    public boolean deleteById(Serializable id) {
        StringBuilder sql = new StringBuilder("");
        // 存在逻辑字段使用逻辑删除
        if (logical == null) {
            sql.append(SqlWord.DELETE).append(SqlWord.FROM)
                    .append(tableName).append(SqlWord.WHERE).append(idName).append(SqlWord.EQ).append(SqlUtil.convertToDBValue(id));
        } else {
            sql.append(SqlWord.UPDATE).append(tableName).append(SqlWord.SET);
            sql.append(logicalName).append(SqlWord.EQ).append(Logical.DELETED);
            sql.append(SqlWord.WHERE).append(idName).append(SqlWord.EQ).append(SqlUtil.convertToDBValue(id));
        }
        SqlMethod sqlMethod = new SqlMethod(sql.toString(), SqlType.DELETE_TYPE, SqlResultType.EXPECT_RESULT_INT);
        Result execute = executor.execute(sqlMethod);
        return (int) execute.getResult() == 1;
    }

    @Override
    public int delete(Wrapper wrapper) {
        StringBuilder sql = new StringBuilder("");
        // 存在逻辑字段使用逻辑删除
        if (logical == null) {
            sql.append(SqlWord.DELETE).append(SqlWord.FROM)
                    .append(tableName).append(SqlWord.WHERE).append(wrapper.getConditionSql());
        } else {
            sql.append(SqlWord.UPDATE).append(tableName).append(SqlWord.SET);
            sql.append(logicalName).append(SqlWord.EQ).append(Logical.DELETED);
            sql.append(SqlWord.WHERE).append(wrapper.getConditionSql());
        }
        SqlMethod sqlMethod = new SqlMethod(sql.toString(), SqlType.DELETE_TYPE, SqlResultType.EXPECT_RESULT_INT);
        Result execute = executor.execute(sqlMethod);
        return (int) execute.getResult();
    }


    public BaseMapper() {
        setGenericsType();
        init();
    }

    /**
     * 初始化获取指定泛型
     */
    private void init() {
        this.tableName = SqlUtil.getTableName(clazz);
        this.fieldsName = SqlUtil.getDBFieldStr(clazz);
        this.idName = SqlUtil.getIdFieldStr(clazz);
        this.fields = SqlUtil.getExistMappingField(clazz);
        this.id = SqlUtil.getTableIdField(clazz);
        this.logical = SqlUtil.getLogicalField(clazz);
        if (logical != null) {
            this.logicalName = SqlUtil.getFieldSqlName(logical);
        }
        this.executor = getExecutor();
    }

    /**
     * 获取父类泛型
     */
    private void setGenericsType() {
        ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
        clazz = (Class) pt.getActualTypeArguments()[0];
    }

    /**
     * 指定语句执行器
     */
    @Override
    public Executor<T> getExecutor() {
        return new DefaultExecutor<>(null, clazz);
    }

    private void setWrapper(StringBuilder sql, Wrapper wrapper) {
        if (wrapper != null) {
            sql.append(SqlWord.WHERE);
            sql.append(wrapper.getConditionSql());
        }
    }
}
