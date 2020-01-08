package com.tttiger.sql;

import com.tttiger.sql.annotation.Table;
import com.tttiger.sql.annotation.TableField;
import com.tttiger.util.DateUtil;
import com.tttiger.util.ReflectUtil;
import com.tttiger.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 指定实体类，生成通用mysql语句
 *
 * @author 秦浩桐
 * @version 1.0
 * @date 2020/01/08 23:43
 */
public class SqlUtil {

    public static <T> String insertSql(T entiry) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");

        Class<?> clazz = entiry.getClass();
        String tableName = getTableName(clazz);
        // 添加表名
        sql.append(tableName).append(" (");
        // 获取存在表字段映射的属性
        List<Field> existMappingField = getExistMappingField(clazz);
        List<String> tableField = getTableField(existMappingField);
        for (String fieldName : tableField) {
            sql.append(fieldName).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES(");
        List<String> fieldSqlValue = getFieldSqlValue(existMappingField, entiry);
        for (String sqlValue : fieldSqlValue) {
            sql.append(sqlValue).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        return sql.toString();
    }

    private static List<String> getFieldSqlValue(List<Field> fields, Object entity) {
        List<String> dbValue = new ArrayList<>();
        try {
            for (Field field : fields) {
                Object value = null;
                value = ReflectUtil.getValueUseGetterMethod(entity, field.getName());
                dbValue.add(SqlUtil.convertToDBValue(value));
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return dbValue;
    }

    private static String convertToDBValue(Object value) {
        if (value instanceof String) {
            return "`" + value + "`";
        } else if (value instanceof Date) {
            return "`" + DateUtil.date2Str((Date) value) + "`";
        }
        return value.toString();
    }

    /**
     * 获取属性对应的表字段名，如果标注TableField注解，并且指定value那么使用自定义字段名
     * 否则使用下划线命名规则
     *
     * @param fields 获取字段名属性集合
     * @return 对应字段名集合
     */
    private static List<String> getTableField(List<Field> fields) {
        List<String> list = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(TableField.class)
                    && StringUtil.isNotEmpty(field.getAnnotation(TableField.class).value())) {
                list.add(field.getAnnotation(TableField.class).value());
            } else {
                list.add(StringUtil.humpToUnderline(field.getName()));
            }
        }
        return list;
    }


    /**
     * 获取存在表字段映射的属性
     *
     * @param clazz 实体类型
     * @return 存在映射的非null可空集合
     */
    private static List<Field> getExistMappingField(Class<?> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        List<Field> fields = new ArrayList<>();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(TableField.class) && !field.getAnnotation(TableField.class).exist()) {
                continue;
            }
            fields.add(field);
        }
        return fields;
    }

    /**
     * 获取实体对应表名，如果未使用Table注解指定，默认为下划线命名表名
     *
     * @param clazz 实体类型
     * @return 表名
     */
    private static String getTableName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            return clazz.getAnnotation(Table.class).value();
        }

        return StringUtil.humpToUnderline(clazz.getSimpleName());
    }
}
