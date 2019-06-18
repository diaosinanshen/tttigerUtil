package com.tttiger.excel.annotation;

import java.lang.annotation.*;

@Repeatable(ExcelFieldStatus.class)  //标识容器类
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Status {
    int value();
    String name();
}
