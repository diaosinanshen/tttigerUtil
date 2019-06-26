package com.tttiger.excel.annotation;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author 秦浩桐
 */
public interface ExcelFieldStyle {

    /**
     * @return 单元格样式
     */
    void headerStyle(CellStyle style);

    /**
     * @return 主体样式
     */
    void bodyStyle(CellStyle style);
}
