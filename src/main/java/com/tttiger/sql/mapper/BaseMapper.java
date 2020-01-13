package com.tttiger.sql.mapper;

import com.tttiger.sql.*;
import com.tttiger.sql.annotation.TableId;
import com.tttiger.sql.constant.Logical;
import com.tttiger.sql.exception.MoreResultException;
import com.tttiger.sql.executor.DefaultSqlExecutor;
import com.tttiger.sql.executor.SqlExecutor;
import com.tttiger.sql.handler.TestDataSourceSupplier;
import com.tttiger.sql.wrapper.Wrapper;

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
    private Field idField;
    /**
     * 实体逻辑删除属性对应表字段名
     */
    private String logicalName;
    /**
     * 实体逻辑删除属性
     */
    private Field logical;

    private MapperConfiguration configuration;

    /**
     * sql执行器
     */
    private SqlExecutor<T> executor;
    /**
     * id生成器
     */
    private IdGenerator<String> idGenerator = new UuidGenerator();


    @Override
    public List<T> select(Wrapper wrapper) {
        SqlMethod sqlMethod = SqlBuilder.buildSelect(wrapper, configuration);
        Result execute = executor.execute(sqlMethod);
        return (List<T>) execute.getResult();
    }

    @Override
    public T selectById(Serializable id) {
        SqlMethod sqlMethod = SqlBuilder.buildSelectById(id, configuration);
        Result execute = executor.execute(sqlMethod);
        List resultList = (List) execute.getResult();
        if (resultList == null || resultList.isEmpty()) {
            return null;
        }
        if (resultList.size() > 1) {
            throw new MoreResultException("返回过多的结果");
        }
        return (T) resultList.get(0);
    }

    @Override
    public boolean insert(T t) {
        SqlMethod sqlMethod = SqlBuilder.buildInsert(t,configuration);
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
        sql.append(SqlWord.WHERE).append(SqlUtil.getFieldAssignmentStr(idField, t));
        SqlMethod sqlMethod = new SqlMethod(sql.toString());
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
        SqlMethod sqlMethod = new SqlMethod(sql.toString());
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
        SqlMethod sqlMethod = new SqlMethod(sql.toString());
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
        SqlMethod sqlMethod = new SqlMethod(sql.toString());
        Result execute = executor.execute(sqlMethod);
        return (int) execute.getResult();
    }


    public BaseMapper() {
        setGenericsType();
        init();
        this.configuration = new MapperConfiguration(setGenericsType());
    }

    /**
     * 初始化获取指定泛型
     */
    private void init() {
        this.tableName = SqlUtil.getTableName(clazz);
        this.fieldsName = SqlUtil.getDBFieldStr(clazz);
        this.idName = SqlUtil.getIdFieldStr(clazz);
        this.fields = SqlUtil.getExistMappingField(clazz);
        this.idField = SqlUtil.getTableIdField(clazz);
        this.logical = SqlUtil.getLogicalField(clazz);
        if (logical != null) {
            this.logicalName = SqlUtil.getFieldSqlName(logical);
        }
        this.executor = getExecutor();
    }

    /**
     * 获取父类泛型
     */
    private Class<?> setGenericsType() {
        ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
        clazz = (Class) pt.getActualTypeArguments()[0];
        return clazz;
    }

    /**
     * 指定语句执行器
     */
    @Override
    public SqlExecutor<T> getExecutor() {
        return new DefaultSqlExecutor<>(new TestDataSourceSupplier(), clazz);
    }

    private void setWrapper(StringBuilder sql, Wrapper wrapper) {
        if (wrapper != null) {
            sql.append(SqlWord.WHERE);
            sql.append(wrapper.getConditionSql());
        }
    }
}
