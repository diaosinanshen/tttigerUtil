package com.tttiger.util;


import java.security.MessageDigest;

/**
 * 字符串相关方法
 *
 * @author 秦浩桐
 */
public class StringUtil {

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }


    public static boolean isNotEmpty(String str) {
        return !StringUtil.isEmpty(str);
    }


    /**
     * 首字母转小写
     * @param str 转换字符串
     * @return 首字母转为小写
     */
    public static String toLowerCaseFirstOne(String str) {
        if (Character.isLowerCase(str.charAt(0))) {
            return str;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(str.charAt(0))).append(str.substring(1)).toString();

        }
    }


    /**
     * 首字母转大写
     * @param str 转换字符串
     * @return 首字母转为大写
     */
    public static String toUpperCaseFirstOne(String str) {
        if (Character.isUpperCase(str.charAt(0))) {
            return str;
        } else {
            return (new StringBuilder()).append(Character.toUpperCase(str.charAt(0))).append(str.substring(1)).toString();
        }
    }

    /**
     * 给定一个url判断url是否是一级路径
     * 一级路径:/root
     * 二级路径:/root/path
     * @param path url路径
     * @return 是否为一级路径
     */
    public static boolean isRootPath(String path) {
        return path.indexOf("/") == path.lastIndexOf("/");
    }


    /**
     * 盐值加密字符
     *
     * @param param 加密字符串
     * @param salt  盐
     * @return 加密字符串
     */
    public static String md5(String param, String salt) {
        return md5(param + salt);
    }

    /**
     * 加密字符串
     *
     * @param s 字符串
     * @return 加密字符串
     */
    public static String md5(String s) {
        char[] hexDigits =
                {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes("utf-8");
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    private StringUtil(){}
}
