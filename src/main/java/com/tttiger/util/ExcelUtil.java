package com.tttiger.util;

import com.tttiger.excel.DefaultExcelHeaderBodyStyle;
import com.tttiger.excel.ExcelHeaderBodyStyle;
import com.tttiger.excel.annotation.*;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 秦浩桐
 * @version 1.0
 * @date 2019/09/28 20:20
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
    private Map<String, Integer> headers = new LinkedHashMap<>();

    /**
     * 保存标题头样式
     */
    private ExcelHeaderBodyStyle excelHeaderBodyStyle;

    /**
     * 是否包含数据收集列
     */
    private boolean hasCollect = false;

    /**
     * 保存需要收集的属性
     */
    private Map<Field, Integer> collectFieldMap = new HashMap<>();
    /**
     * 保存收集的结果
     */
    private Map<Integer, BigDecimal> collectResult = new HashMap<>();

    /**
     * @param fileName 文件标题
     * @param data     集合数据
     * @param response Http响应
     * @throws IllegalAccessException
     * @throws IOException
     */
    public void exportExcel(String fileName, List<T> data, HttpServletResponse response) throws IllegalAccessException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
        ExportExcel(fileName, data, response.getOutputStream());
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


        int startColumn = 0;
        int startRow = 0;
        // 设置标题行
        XSSFFont headerFont = workbook.createFont();
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        this.excelHeaderBodyStyle.headerStyle(headerStyle, headerFont);
        headerStyle.setFont(headerFont);
        XSSFRow row0 = sheet.createRow(startRow);
        // 生成标题头
        for (Map.Entry<String, Integer> entry : headers.entrySet()) {
            XSSFCell cell = row0.createCell(startColumn);
            cell.setCellValue(entry.getKey());
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(startColumn, entry.getValue());
            startColumn++;
        }

        int rowIndex = startRow + 1;
        XSSFCellStyle bodyStyle = workbook.createCellStyle();
        XSSFFont bodyFont = workbook.createFont();
        this.excelHeaderBodyStyle.bodyStyle(bodyStyle, bodyFont);
        bodyStyle.setFont(bodyFont);
        // 普通属性映射
        Set<Map.Entry<Field, Integer>> entries = fieldMap.entrySet();
        // 设置复合属性,关联属性
        Set<Map.Entry<Field, Map<Field, Integer>>> tempEntry = associateMap.entrySet();
        for (T tempObj : dataset) {
            XSSFRow row = sheet.createRow(rowIndex);
            // 设置普通属性
            for (Map.Entry<Field, Integer> entry : entries) {
                Object obj = entry.getKey().get(tempObj);
                XSSFCell cell = row.createCell(entry.getValue());
                cell.setCellStyle(bodyStyle);
                setCellValue(entry.getKey(), cell, obj);
                // 尝试收集汇总数据
                collectResult(entry, obj);
            }
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
                    cell.setCellStyle(bodyStyle);
                    setCellValue(entry1.getKey(), cell, obj2);
                    collectResult(entry1, obj2);
                }
            }
            rowIndex++;
        }
        if (hasCollect) {
            XSSFRow resultRow = sheet.createRow(rowIndex);
            XSSFCellStyle collectStyle = workbook.createCellStyle();
            XSSFFont collectFont = workbook.createFont();
            this.excelHeaderBodyStyle.collectStyle(collectStyle,collectFont);
            collectStyle.setFont(collectFont);
            for (Map.Entry<Integer, BigDecimal> entry : collectResult.entrySet()) {
                XSSFCell resultCell = resultRow.createCell(entry.getKey());
                resultCell.setCellStyle(collectStyle);
                resultCell.setCellValue(entry.getValue().doubleValue());
            }
        }
        workbook.write(stream);
    }

    private void setHeaders(XSSFRow row){

    }

    /**
     * 收集汇总数据列
     */
    private void collectResult(Map.Entry<Field, Integer> entry, Object object) throws IllegalAccessException {
        if (this.hasCollect && collectFieldMap.get(entry.getKey()) != null) {
            Integer collectIndex = collectFieldMap.get(entry.getKey());
            entry.getKey().setAccessible(true);
            if (collectResult.get(collectIndex) == null) {
                collectResult.put(collectIndex, new BigDecimal(object + ""));
            } else {
                BigDecimal previousNum = collectResult.get(collectIndex);
                BigDecimal afterNum = previousNum.add(new BigDecimal(object + ""));
                collectResult.put(collectIndex, afterNum);
            }
        }
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
     * 初始化
     *
     * @param entity 导出实体集合
     */
    private void init(List<T> entity) {
        Class<?> clazz = entity.get(0).getClass();

        // 获取表格默认表头，表体样式
        if (clazz.isAnnotationPresent(ExcelStyle.class)) {
            Class<? extends ExcelHeaderBodyStyle> styleClazz = clazz.getAnnotation(ExcelStyle.class).excelStyle();
            try {
                Constructor<? extends ExcelHeaderBodyStyle> constructor = styleClazz.getConstructor();
                this.excelHeaderBodyStyle = constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            this.excelHeaderBodyStyle = new DefaultExcelHeaderBodyStyle();
        }

        // 获取需要导出属性排序
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
        // 初始化标题头，和对应映射关系
        int flag = 0;
        for (Field field : commonField) {
            if (field.isAnnotationPresent(ExcelField.class)) {
                // 添加到标题头
                ExcelField ex = field.getAnnotation(ExcelField.class);
                headers.put(ex.value(), ex.width());
                // 普通属性放入
                fieldMap.put(field, flag);
                if (ex.collect()) {
                    this.hasCollect = true;
                    collectFieldMap.put(field, flag);
                }
                flag++;
            } else if (field.isAnnotationPresent(ExcelAssociate.class)) {
                // 复合属性
                String[] names = field.getAnnotation(ExcelAssociate.class).value();
                Map<Field, Integer> tempMap = new HashMap<>();
                for (String str : names) {
                    // 找到复合属性需要注入的值
                    Field aField = findField(field.getType(), str);
                    if (aField == null || !aField.isAnnotationPresent(ExcelField.class)) {
                        throw new ExcelAnnotationException("复合属性未找到");
                    }
                    ExcelField af = aField.getAnnotation(ExcelField.class);
                    headers.put(af.value(), af.width());
                    // 放入临时map
                    tempMap.put(aField, flag);
                    if (af.collect()) {
                        this.hasCollect = true;
                        collectFieldMap.put(field, flag);
                    }
                    flag++;
                }
                // 复合属性与复合属性字段
                associateMap.put(field, tempMap);
            }
        }

    }
}
