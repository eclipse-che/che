/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.typescript.dto;

import org.eclipse.che.dto.shared.DelegateTo;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Helper class
 *
 * @author Florent Benoit
 */
public class DTOHelper {

    /**
     * Utility class.
     */
    private DTOHelper() {

    }

    /**
     * Check is specified method is DTO getter.
     */
    public static boolean isDtoGetter(Method method) {
        if (method.isAnnotationPresent(DelegateTo.class)) {
            return false;
        }

        if (method.getParameterTypes().length > 0) {
            return false;
        }

        String methodName = method.getName();

        return methodName.startsWith("get") ||
               (methodName.startsWith("is") && ((method.getReturnType() == Boolean.class || method.getReturnType() == boolean.class)));

    }


    /**
     * Check is specified method is DTO setter.
     */
    public static boolean isDtoSetter(Method method) {
        if (method.isAnnotationPresent(DelegateTo.class)) {
            return false;
        }
        String methodName = method.getName();
        return methodName.startsWith("set") && method.getParameterTypes().length == 1;
    }

    /**
     * Check is specified method is DTO with.
     */
    public static boolean isDtoWith(Method method) {
        if (method.isAnnotationPresent(DelegateTo.class)) {
            return false;
        }
        String methodName = method.getName();
        return methodName.startsWith("with") && method.getParameterTypes().length == 1;
    }

    /**
     * Compute field name from the stringified string type
     */
    public static String getFieldName(String type) {
        char[] c = type.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    /**
     * Extract field name from the getter method
     */
    public static String getGetterFieldName(Method method) {
        String methodName = method.getName();
        if (methodName.startsWith("get")) {
            return getFieldName(methodName.substring(3));
        } else if (methodName.startsWith("is")) {
            return getFieldName(methodName.substring(2));
        }
        throw new IllegalArgumentException("Invalid getter method" + method.getName());
    }

    /**
     * Extract field name from the setter method
     */
    public static String getSetterFieldName(Method method) {
        String methodName = method.getName();
        if (methodName.startsWith("set")) {
            return getFieldName(methodName.substring(3));
        }
        throw new IllegalArgumentException("Invalid setter method" + method.getName());
    }

    /**
     * Extract field name from the with method
     */
    public static String getWithFieldName(Method method) {
        String methodName = method.getName();
        if (methodName.startsWith("with")) {
            return getFieldName(methodName.substring(4));
        }
        throw new IllegalArgumentException("Invalid with method" + method.getName());
    }

    /**
     * Convert Java type to TypeScript type
     */
    public static String convertType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            Type rawType = parameterizedType.getRawType();
            return convertParametrizedType(type, parameterizedType, rawType);
        } else if (String.class.equals(type) || (type instanceof Class && ((Class)type).isEnum())) {
            // Maybe find a better enum type for typescript
            return "string";
        } else if (Integer.class.equals(type) || Integer.TYPE.equals(type) || Long.class.equals(type) || Long.TYPE.equals(type) || Double.class.equals(type) || Double.TYPE.equals(type)) {
            return "number";
        } else if (Boolean.class.equals(type)) {
            return "boolean";
        }

        return type.getTypeName();
    }

    /**
     * Handle convert of a parametrized Java type to TypeScript type
     */
    public static String convertParametrizedType(Type type, ParameterizedType parameterizedType, Type rawType) {

        if (List.class.equals(rawType)) {
            return "Array<" + convertType(parameterizedType.getActualTypeArguments()[0]) + ">";
        } else if (Map.class.equals(rawType)) {
            return "Map<" + convertType(parameterizedType.getActualTypeArguments()[0]) + "," +
                   convertType(parameterizedType.getActualTypeArguments()[1]) + ">";
        } else {
            throw new IllegalArgumentException("Invalid type" + type);
        }
    }
}
