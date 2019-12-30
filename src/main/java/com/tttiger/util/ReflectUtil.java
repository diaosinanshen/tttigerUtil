package com.tttiger.util;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author 秦浩桐
 * @date  2019/12/24 03:28
 */
public class ReflectUtil {

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


    public static <T> void setObjectField(Object obj, T field, String fieldName) {
        try {
            Method method = obj.getClass().getDeclaredMethod("set" + StringUtil.toUpperCaseFirstOne(fieldName), field.getClass());
            method.invoke(obj, field);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    private ReflectUtil() {
    }
}
