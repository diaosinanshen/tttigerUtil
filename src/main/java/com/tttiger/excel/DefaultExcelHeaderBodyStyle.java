package com.tttiger.excel;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;

/**
 * @author 秦浩桐
 * @version 1.0
 * @date 2019/12/30 17:16
 */
public class DefaultExcelHeaderBodyStyle implements ExcelHeaderBodyStyle {
    @Override
    public void headerStyle(CellStyle style,XSSFFont font) {

        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        style.setLocked(true);
        style.setWrapText(true);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
    }

    @Override
    public void bodyStyle(CellStyle style,XSSFFont font) {

        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);

        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
    }

    @Override
    public void collectStyle(CellStyle style, XSSFFont font) {
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);

        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
    }
}
