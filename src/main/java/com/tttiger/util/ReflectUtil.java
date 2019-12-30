package com.tttiger.util;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author 秦浩桐
 * @date  2019/12/24 03:28
 */
public class ReflectUtil {

    /**
     * 通过调用对象的getter方法来获取属性
     * @param obj 指定对象
     * @param fieldName 指定属性名
     * @return 对象属性内容
     */
    public static Object getGetterMethod(Object obj, String fieldName) {
        try {
            Method declaredMethod = obj.getClass()
                    .getDeclaredMethod("get" + StringUtil.toUpperCaseFirstOne(fieldName), null);
            return declaredMethod.invoke(obj, null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过调用对象的setter方法来尝试设置属性值
     * @param obj 指定对象
     * @param field 设置属性
     * @param fieldName 指定属性名
     * @return 是否设置成功（异常已经捕获）
     */
    public static <T> boolean setObjectField(Object obj, T field, String fieldName) {
        try {
            Method method = obj.getClass().getDeclaredMethod("set" + StringUtil.toUpperCaseFirstOne(fieldName), field.getClass());
            method.invoke(obj, field);
            return true;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ReflectUtil() {
    }
}
