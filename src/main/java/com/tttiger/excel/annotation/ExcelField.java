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
public @interface ExcelField{

    /**
     * 属性导出后的名称
     */
    String value();

    /**
     * 是否为状态属性
     */
    boolean status() default false;

    /**
     * 字段是否参与排序
     */
    int sort() default Integer.MAX_VALUE;

    /**
     * 属性应用的样式
     */
    Class<? extends ExcelFieldStyle> style() default DefaultExcelFieldStyle.class;
}
