package org.aries.middleware.hermes.common.utils;

import cn.hutool.core.util.ArrayUtil;

import java.lang.reflect.Method;

public class StringUtil {
    public static String simplifyClassName(String className) {
        int idx = className.lastIndexOf(".");
        StringBuilder builder = new StringBuilder();
        builder.append(className.charAt(0)).append(".");

        for (int a = 0; a < idx; a++) {
            char ch = className.charAt(a);
            if (ch == '.') {
                builder.append(className.charAt(a + 1)).append(".");
            }
        }

        builder.append(className.substring(idx + 1));
        return builder.toString();
    }


    public static String buildOperationName(String prefix, Method method) {
        String clazz = method.getDeclaringClass().getSimpleName();
        String mName = method.getName();

        Class<?>[] paramTypes = method.getParameterTypes();
        String[] argNames = new String[paramTypes.length];
        for (int a = 0; a < paramTypes.length; a++)
            argNames[a] = paramTypes[a].getSimpleName();

        return prefix + clazz + "." + mName + "(" + ArrayUtil.join(argNames, ",") + ")";
    }


    public static void main(String[] args) throws Exception {
        Method method = StringUtil.class.getMethod("buildOperationName", String.class, Method.class);
        System.out.println(buildOperationName("Service://", method));
    }
}
