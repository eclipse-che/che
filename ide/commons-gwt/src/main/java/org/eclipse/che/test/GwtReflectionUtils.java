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
package org.eclipse.che.test;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.web.bindery.event.shared.UmbrellaException;

import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Evgen Vidolob
 */
public class GwtReflectionUtils {

    public static void callOnSuccessVoidParameter(RequestCallback<?> callback) throws NoSuchMethodException,
                                                                                      IllegalAccessException,
                                                                                      InvocationTargetException,
                                                                                      InstantiationException {
        Method onSuccess = callback.getClass().getDeclaredMethod("onSuccess", Void.class);
        onSuccess.setAccessible(true);
        Constructor<Void> constructor = Void.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        onSuccess.invoke(callback, constructor.newInstance());
    }

    public static void callOnFailure(AsyncRequestCallback<?> callback, Object... args) {
        callPrivateMethod(callback, "onFailure", args);
    }

    public static void callOnSuccess(AsyncRequestCallback<?> callback, Object... args) {
        callPrivateMethod(callback, "onSuccess", args);
    }

    public static void callOnFailure(RequestCallback<?> callback, Object... args) {
        callPrivateMethod(callback, "onFailure", args);
    }

    public static void callOnSuccess(RequestCallback<?> callback, Object... args) {
        callPrivateMethod(callback, "onSuccess", args);
    }

    public static <T> T callPrivateMethod(Object target, String methodName, Object... args) {
        if (target instanceof JavaScriptObject) {
            throw new UnsupportedOperationException(
                    "Cannot call instance method on Overlay types without specifying its base type");
        }

        Method method = findMethod(target.getClass(), methodName, args);
        if (method == null) {
            throw new RuntimeException("Cannot find method '" + target.getClass().getName() + "."
                                       + methodName + "(..)'");
        }
        return (T)callPrivateMethod(target, method, args);

    }

    private static Method findMethod(Class<?> clazz, String methodName, Object... args) {
        Class<?>[] l = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            l[i] = args[i] != null ? args[i].getClass() : null;
        }
        return findMethod(clazz, methodName, l);
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> searchType = clazz;
        while (!Object.class.equals(searchType) && searchType != null) {
            Method[] methods = searchType.isInterface() ? searchType.getMethods()
                                                        : searchType.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (name.equals(method.getName())
                    && paramTypes.length == method.getParameterTypes().length) {
                    boolean compatibleParams = true;
                    for (int j = 0; j < paramTypes.length; j++) {

                        if (paramTypes[j] == null) {
                            // null class is a wildcard
                            continue;
                        }

                        Class<?> methodParamType = getCheckedClass(method.getParameterTypes()[j]);
                        Class<?> searchParamType = getCheckedClass(paramTypes[j]);

                        if (!methodParamType.isAssignableFrom(searchParamType)) {
                            compatibleParams = false;
                        }
                    }

                    if (compatibleParams) {
                        return method;

                    }
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    private static Class<?> getCheckedClass(Class<?> potentialPrimitiveType) {
        if (!potentialPrimitiveType.isPrimitive()) {
            return potentialPrimitiveType;
        }

        if (potentialPrimitiveType == Byte.TYPE) {
            return Byte.class;
        } else if (potentialPrimitiveType == Short.TYPE) {
            return Short.class;
        } else if (potentialPrimitiveType == Integer.TYPE) {
            return Integer.class;
        } else if (potentialPrimitiveType == Long.TYPE) {
            return Long.class;
        } else if (potentialPrimitiveType == Float.TYPE) {
            return Float.class;
        } else if (potentialPrimitiveType == Double.TYPE) {
            return Double.class;
        } else if (potentialPrimitiveType == Boolean.TYPE) {
            return Boolean.class;
        } else {
            return Character.class;
        }
    }

    public static <T> T callPrivateMethod(Object target, Method method, Object... args) {
        try {
            method.setAccessible(true);
            Object res = method.invoke(target, args);
            return (T)res;
        } catch (InvocationTargetException e) {
            if (AssertionError.class.isInstance(e.getCause())) {
                throw (AssertionError)e.getCause();
            } else if (UmbrellaException.class.isInstance(e.getCause())) {
                throw new RuntimeException("Error while calling method '" + method.toString() + "'",
                                           e.getCause().getCause());
            }
            throw new RuntimeException("Error while calling method '" + method.toString() + "'",
                                       e.getCause());
        } catch (Exception e) {
            throw new RuntimeException("Unable to call method '"
                                       + target.getClass().getSimpleName() + "." + method.getName() + "(..)'", e);
        }
    }
}
