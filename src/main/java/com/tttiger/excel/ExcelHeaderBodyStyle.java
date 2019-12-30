package com.tttiger.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;

/**
 * @author 秦浩桐
 * @version 1.0
 * @date 2019/12/30 17:03
 */
public interface ExcelHeaderBodyStyle {
    /**
     * 自定义表头样式
     *
     * @param style 设置表头样式
     * @param font  设置表头字体
     */
    void headerStyle(CellStyle style, XSSFFont font);

    /**
     * 自定义表体样式
     *
     * @param style 设置表体样式
     * @param font  设置表体字体
     */
    void bodyStyle(CellStyle style, XSSFFont font);

    /**
     * 设置数据收集行样式
     * @param style 设置收集行样式
     * @param font 社遏制收集行字体
     */
    void collectStyle(CellStyle style, XSSFFont font);
}
