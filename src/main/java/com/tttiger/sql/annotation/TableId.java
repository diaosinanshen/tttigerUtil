package com.tttiger.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 秦浩桐
 * @version 1.0
 * @date 2020/01/09 14:46
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableId {
    /**
     * 标记实体字段对应数据库的唯一id，指定自定义列名
     */
    String value() default "";
}
