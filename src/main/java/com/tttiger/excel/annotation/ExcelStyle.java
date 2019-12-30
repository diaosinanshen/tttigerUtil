package com.tttiger.excel.annotation;

import com.tttiger.excel.ExcelHeaderBodyStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 秦浩桐
 * @version 1.0
 * @date 2019/12/30 17:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ExcelStyle {
    /**
     * 指定表头，表体样式
     */
    Class<? extends ExcelHeaderBodyStyle> excelStyle();
}
