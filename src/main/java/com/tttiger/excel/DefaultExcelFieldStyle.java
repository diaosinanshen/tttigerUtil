package com.tttiger.excel;

import com.tttiger.excel.annotation.ExcelFieldStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author 秦浩桐
 */
public class DefaultExcelFieldStyle implements ExcelFieldStyle {
    @Override
    public void headerStyle(CellStyle style) {
         bodyStyle(style);
    }

    @Override
    public void bodyStyle(CellStyle style) {
        XSSFCellStyle s = ((XSSFWorkbook)style).createCellStyle();
        s.setFillForegroundColor(new XSSFColor(java.awt.Color.WHITE));
        s.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        s.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        s.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        s.setBorderRight(XSSFCellStyle.BORDER_THIN);
        s.setBorderTop(XSSFCellStyle.BORDER_THIN);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setAlignment(HorizontalAlignment.CENTER);
    }

}
