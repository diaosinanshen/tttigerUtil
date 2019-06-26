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
     * 字符串是否为数字验证
     */
    private final Pattern pattern = Pattern.compile("^//d+(//.//d+)?$");

    /**
     * 保存普通导出属性
     */
    private Map<Field, Integer> fieldMap = new HashMap<>();

    /**
     * 保存复合导出属性
     */
    private Map<Field, Map<Field, Integer>> associateMap = new HashMap<>();

    /**
     * 保存导出标题
     */
    private List<String> headers = new ArrayList<>();

    /**
     * @param title    文件标题
     * @param dataset  集合数据
     * @param response Http响应
     * @throws IllegalAccessException
     * @throws IOException
     */
    public void ExportExcel(String title, List<T> dataset, HttpServletResponse response) throws IllegalAccessException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + title + ".xls");
        ExportExcel(title, dataset, response.getOutputStream());
    }

    /**
     * @param title   文件标题
     * @param dataset 集合数据
     * @param stream  导出流
     * @throws IllegalAccessException
     * @throws IOException
     */
    public void ExportExcel(String title, List<T> dataset, OutputStream stream) throws IllegalAccessException, IOException {
        if (dataset == null || dataset.isEmpty()) {
            return;
        }
        // 初始化
        init(dataset);

        // 声明一个工作薄
        XSSFWorkbook workbook = new XSSFWorkbook();
        // 生成一个表格
        XSSFSheet sheet = workbook.createSheet(title);
        workbook.createCellStyle()
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth(20);
        // 设置标题行
        XSSFRow row0 = sheet.createRow(0);


        // 生成标题头
        for (int i = 0; i < headers.size(); i++) {
            XSSFCell cell = row0.createCell(i);
            cell.setCellValue(headers.get(i));
        }

        int rowIndex = 1;
        for (T tempObj : dataset) {
            XSSFRow row = sheet.createRow(rowIndex);
            for (int i = 0; i < headers.size(); i++) {
                Set<Map.Entry<Field, Integer>> entries = fieldMap.entrySet();
                // 设置普通属性
                for (Map.Entry<Field, Integer> entry : entries) {
                    Object obj = entry.getKey().get(tempObj);
                    XSSFCell cell = row.createCell(entry.getValue());
                    cell.setCellStyle(style);
                    setCellValue(entry.getKey(), cell, obj);
                }
                // 设置复合属性,关联属性
                Set<Map.Entry<Field, Map<Field, Integer>>> tempEntry = associateMap.entrySet();
                // 每个关联属性
                for (Map.Entry<Field, Map<Field, Integer>> mapEntry : tempEntry) {
                    // 复合属性值
                    Object obj = mapEntry.getKey().get(tempObj);
                    // 复合属性所需导出字段
                    Set<Map.Entry<Field, Integer>> fieldEntry = mapEntry.getValue().entrySet();
                    for (Map.Entry<Field, Integer> entry1 : fieldEntry) {
                        entry1.getKey().setAccessible(true);
                        Object obj2 = entry1.getKey().get(obj);
                        XSSFCell cell = row.createCell(entry1.getValue());
                        cell.setCellStyle(style);
                        setCellValue(entry1.getKey(), cell, obj2);
                    }
                }
            }
            rowIndex++;
        }
        workbook.write(stream);
    }

    /**
     * 根据名字查找属性field
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
     * @param field 属性类型
     * @param cell  单元格
     * @param value 属性值
     * @throws ExcelAnnotationException 注解使用不正确
     */
    private void setCellValue(Field field, XSSFCell cell, Object value) {
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


    /**
     * 初始化工具类
     *
     * @param entity 导出实体集合
     */
    private void init(List<T> entity) {
        Class<?> clazz = entity.get(0).getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        List<Field> commonField = new ArrayList<>();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(ExcelField.class) || field.isAnnotationPresent(ExcelAssociate.class)) {
                field.setAccessible(true);
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
        int flag = 0;
        for (Field field : commonField) {
            if (field.isAnnotationPresent(ExcelField.class)) {
                // 添加到标题头
                headers.add(field.getAnnotation(ExcelField.class).value());
                // 普通属性放入
                fieldMap.put(field, flag++);
            } else if (field.isAnnotationPresent(ExcelAssociate.class)) {
                // 复合属性
                String[] names = field.getAnnotation(ExcelAssociate.class).value();
                Map<Field, Integer> tempMap = new HashMap<>();
                for (String str : names) {
                    // 找到复合属性需要注入的值
                    Field aField = findField(field.getType(), str);
                    if (!aField.isAnnotationPresent(ExcelField.class)) {
                        throw new ExcelAnnotationException("复合属性未找到");
                    }
                    headers.add(aField.getAnnotation(ExcelField.class).value());
                    // 放入临时map
                    tempMap.put(aField, flag++);
                }
                // 复合属性与复合属性字段
                associateMap.put(field, tempMap);
            }
        }

    }
}
