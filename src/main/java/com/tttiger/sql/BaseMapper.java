package com.tttiger.sql;

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

    private Class<?> clazz;

    private String tableName;

    private String fieldsName;

    private List<Field> fields;

    private String idName;

    private Field id;

    private String logicalName;

    private Field logical;

    private Executor<T> executor;

    @Override
    public List<T> select(Wrapper wrapper) {
        StringBuilder sql = new StringBuilder(SqlWord.SELECT);
        sql.append(fieldsName);
        sql.append(SqlWord.FROM).append(tableName).append(SqlWord.WHERE);
        sql.append(wrapper.getConditionSql());
        return executor.select(sql.toString());
    }

    @Override
    public T selectById(Serializable id) {
        StringBuilder sql = new StringBuilder(SqlWord.SELECT);
        sql.append(fieldsName);
        sql.append(SqlWord.FROM).append(tableName).append(SqlWord.WHERE)
                .append(id).append(SqlWord.EQ).append(SqlUtil.convertToDBValue(id));
        return executor.selectById(sql.toString());
    }

    @Override
    public boolean insert(T t) {
        StringBuilder sql = new StringBuilder(SqlWord.INSERT);
        sql.append(SqlWord.INTO);
        // 添加表名
        sql.append(tableName);
        // 获取存在表字段映射的属性
        sql.append(SqlUtil.getInsertDBStr(fields, t));
        return executor.insert(sql.toString());
    }

    @Override
    public boolean updateById(T t) {
        StringBuilder sql = new StringBuilder(SqlWord.UPDATE);
        sql.append(tableName).append(SqlWord.SET);
        for (Field temp : fields) {
            Object fieldValue = SqlUtil.getFieldSqlValue(temp, t);
            if (fieldValue == null) {
                continue;
            }
            sql.append(StringUtil.humpToUnderline(temp.getName()))
                    .append(SqlWord.EQ).append(SqlUtil.convertToDBValue(fieldValue)).append(",");
        }
        sql.deleteCharAt(sql.length()-1);
        sql.append(SqlWord.WHERE).append(SqlUtil.getFieldAssignmentStr(id, t));
        return executor.updateById(sql.toString());
    }

    @Override
    public int update(T t, Wrapper wrapper) {
        StringBuilder sql = new StringBuilder(SqlWord.UPDATE);
        sql.append(tableName).append(SqlWord.SET);
        for (Field temp : fields) {
            Object fieldValue = SqlUtil.getFieldSqlValue(temp, t);
            if (fieldValue == null) {
                continue;
            }
            sql.append(StringUtil.humpToUnderline(temp.getName()))
                    .append(SqlWord.EQ).append(SqlUtil.convertToDBValue(fieldValue)).append(",");
        }
        sql.deleteCharAt(sql.length()-1);
        sql.append(SqlWord.WHERE).append(wrapper.getConditionSql());
        return executor.update(sql.toString());
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
            sql.append(logicalName).append(SqlWord.EQ).append("0");
            sql.append(SqlWord.WHERE).append(idName).append(SqlWord.EQ).append(SqlUtil.convertToDBValue(id));
        }
        System.out.println(sql);
        return false;
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
            sql.append(logicalName).append(SqlWord.EQ).append("0");
            sql.append(SqlWord.WHERE).append(wrapper.getConditionSql());
        }
        return 0;
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

    private void setGenericsType() {
        ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
        clazz = (Class) pt.getActualTypeArguments()[0];
    }

    /**
     * 指定语句执行器
     */
    public abstract Executor<T> getExecutor();
}
