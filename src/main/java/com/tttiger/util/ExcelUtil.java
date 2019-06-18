package com.tttiger.util;

import com.tttiger.excel.annotation.*;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @param <T>
 * @author 秦浩桐
 */
public class ExcelUtil<T> {

    /**
     * 2007 版本以上 最大支持1048576行
     */
    public static final String EXCEl_FILE_2007 = "2007";


    private final Pattern pattern = Pattern.compile("^//d+(//.//d+)?$");


    public void qExportExcel(String title, List<T> dataset, HttpServletResponse response) throws IllegalAccessException, IOException, ExcelAnnotationException {
        OutputStream out = response.getOutputStream();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + title + ".xls");
        if (dataset.isEmpty()) {
            return;
        }

        T t = dataset.get(0);
        Field[] fields = t.getClass().getDeclaredFields();
        List<String> headers = new ArrayList<>();
        // 获取标题行
        createHeader(headers, fields);

        // 声明一个工作薄
        XSSFWorkbook workbook = new XSSFWorkbook();
        // 生成一个表格
        XSSFSheet sheet = workbook.createSheet(title);
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth(20);
        // 设置标题行
        XSSFRow row0 = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            XSSFCell cell = row0.createCell(i);
            cell.setCellValue(headers.get(i));
        }

        int rowIndex = 1;
        for (T tempObj : dataset) {
            XSSFRow row = sheet.createRow(rowIndex);
            int cellIndex = 0;
            for (Field tempF : fields) {
                tempF.setAccessible(true);
                // 如果不是自定义类型,直接设置单元格
                if (tempF.isAnnotationPresent(ExcelField.class)) {
                    XSSFCell cell = row.createCell(cellIndex);
                    Object var1 = tempF.get(tempObj);
                    setCellValue(tempF, cell, var1);
                    cellIndex++;
                    // 是复合类型,需要找到需要设置的,符合类属性
                } else if (tempF.isAnnotationPresent(ExcelAssociate.class)) {
                    String[] fieldNames = tempF.getAnnotation(ExcelAssociate.class).value();
                    Object var1 = tempF.get(tempObj);
                    Class<?> declaringClass = tempF.getType();
                    for (String str : fieldNames) {
                        XSSFCell cell = row.createCell(cellIndex);
                        Field f = findField(declaringClass, str);
                        f.setAccessible(true);
                        Object var2 = f.get(var1);
                        setCellValue(f, cell, var2);
                        cellIndex++;
                    }
                }
            }
            rowIndex++;
        }
        workbook.write(out);
    }


    /**
     * 为状态属性设置值
     *
     * @param status 属性状态标识
     * @param cell   单元格
     * @param obj    属性值
     */

    private void setStatusField(ExcelFieldStatus status, XSSFCell cell, Object obj) {
        Status[] value = status.value();
        for (Status s : value) {
            int var1 = s.value();
            // 状态是数值类型,全部当做Integer来比较
            if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer || obj instanceof Long) {
                if (Integer.valueOf(obj.toString()).equals(var1)) {
                    cell.setCellValue(s.name());
                    return;
                }
            }

        }
        setCellValue(cell, obj);
    }

    /**
     * 查找属性
     *
     * @param clazz 类
     * @param name  属性名
     * @return 找到的属性
     */
    private Field findField(Class<?> clazz, String name) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    /**
     * 为单元格设置值
     *
     * @param field 属性
     * @param cell  单元格
     * @param value 属性值
     * @throws ExcelAnnotationException 注解使用不正确
     */
    private void setCellValue(Field field, XSSFCell cell, Object value) throws ExcelAnnotationException {
        if (field.getAnnotation(ExcelField.class).status()) {
            if (field.isAnnotationPresent(ExcelFieldStatus.class)) {
                ExcelFieldStatus annotation = field.getAnnotation(ExcelFieldStatus.class);
                setStatusField(annotation, cell, value);
            } else {
                throw new ExcelAnnotationException("Excel注解使用错误");
            }
        } else {
            setCellValue(cell, value);
        }
    }

    /**
     * 根据注解生成Excel标题头
     *
     * @param headers 存放标题头的数组
     * @param fields  查找的属性
     */
    private void createHeader(List<String> headers, Field[] fields) {
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelField.class)) {
                String value = field.getAnnotation(ExcelField.class).value();
                headers.add(value);
            } else if (field.isAnnotationPresent(ExcelAssociate.class)) {
                String[] fieldName = field.getAnnotation(ExcelAssociate.class).value();
                Class<?> declaringClass = field.getType();
                for (String str : fieldName) {
                    Field f = findField(declaringClass, str);
                    if (f.isAnnotationPresent(ExcelField.class)) {
                        headers.add(f.getAnnotation(ExcelField.class).value());
                    }
                }
            }
        }
    }

    /**
     * 对值类型进行适配赋值
     *
     * @param cell  单元格
     * @param value 原类型
     */
    private void setCellValue(XSSFCell cell, Object value) {
        String textValue = null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Float) {
            textValue = String.valueOf((Float) value);
            cell.setCellValue(textValue);
        } else if (value instanceof Double) {
            textValue = String.valueOf((Double) value);
            cell.setCellValue(textValue);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        }
        if (value instanceof Boolean) {
            textValue = "是";
            if (!(Boolean) value) {
                textValue = "否";
            }
        } else if (value instanceof Date) {
            textValue = sdf.format((Date) value);
        } else {
            // 其它数据类型都当作字符串简单处理
            if (value != null) {
                textValue = value.toString();
            }
        }
        if (textValue != null) {
            Matcher matcher = pattern.matcher(textValue);
            if (matcher.matches()) {
                // 是数字当作double处理
                cell.setCellValue(Double.parseDouble(textValue));
            } else {
                XSSFRichTextString richString = new XSSFRichTextString(textValue);
                cell.setCellValue(richString);
            }
        }
    }



    private Map<Field, Integer> fieldMap = new HashMap<>();

    private Map<Field, Map<Field, Integer>> associateMap = new HashMap<>();

    public void findField(List<T> entity) {
        if (entity == null || entity.isEmpty()) {
            return;
        }
        Class<?> clazz = entity.get(0).getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        List<Field> commonField = new ArrayList<>();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(ExcelField.class) || field.isAnnotationPresent(ExcelAssociate.class)) {
                commonField.add(field);
            }
        }
        commonField.sort((x, y) -> {
            int var1 = x.isAnnotationPresent(ExcelField.class) ? x.getAnnotation(ExcelField.class).sort() :
                    x.getAnnotation(ExcelAssociate.class).sort();

            int var2 = y.isAnnotationPresent(ExcelField.class) ? y.getAnnotation(ExcelField.class).sort() :
                    y.getAnnotation(ExcelAssociate.class).sort();
            return var1 - var2;
        });
        int flag = 1;
        for (Field field : commonField) {
            if (field.isAnnotationPresent(ExcelField.class)) {
                fieldMap.put(field, flag++);
            } else if (field.isAnnotationPresent(ExcelAssociate.class)) {
                String[] names = field.getAnnotation(ExcelAssociate.class).value();
                Map<Field, Integer> tempMap = new HashMap<>();
                for (String str : names) {
                    Field aField = findField(clazz, str);
                    tempMap.put(aField, flag++);
                }
                associateMap.put(field, tempMap);
            }
        }

    }
}
