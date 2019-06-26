package com.tttiger.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 秦浩桐
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelAssociate {
    /**
     * 实体类中关联属性,需要导出的字段
     */
    String[] value();

    /**
     * 排序字段
     */
    int sort() default Integer.MAX_VALUE;
}
