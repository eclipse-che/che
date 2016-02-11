/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.inject.lifecycle;

import com.google.inject.AbstractModule;

import org.everrest.core.impl.HelperCache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** @author andrew00x */
abstract class LifecycleModule extends AbstractModule {
    private static class Key {
        final Class<?>                    type;
        final Class<? extends Annotation> annotationType;
        final int                         hashCode;

        static Key of(Class<?> type, Class<? extends Annotation> annotationType) {
            return new Key(type, annotationType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }
            Key key = (Key)o;
            return annotationType.equals(key.annotationType) && type.equals(key.type);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        private Key(Class<?> type, Class<? extends Annotation> annotationType) {
            this.type = type;
            this.annotationType = annotationType;
            int hash = annotationType.hashCode();
            hash = 31 * hash + type.hashCode();
            this.hashCode = hash;
        }
    }

    private static final int SIZE = 1 << 3;
    private static final int MASK = SIZE - 1;

    private final HelperCache<Key, Method[]>[] caches;

    @SuppressWarnings("unchecked")
    LifecycleModule() {
        caches = new HelperCache[SIZE];
        for (int i = 0; i < SIZE; i++) {
            caches[i] = new HelperCache<>(60 * 1000, 50);
        }
    }

    Method[] get(Class<?> type, Class<? extends Annotation> annotationType) {
        final Key key = Key.of(type, annotationType);
        final HelperCache<Key, Method[]> cache = caches[key.hashCode() & MASK];
        synchronized (cache) {
            Method[] methods = cache.get(key);
            if (methods == null) {
                cache.put(key, methods = doGet(type, annotationType));
            }
            return methods;
        }
    }

    private Method[] doGet(Class<?> type, Class<? extends Annotation> annotationType) {
        final List<Method> allMethods = getAllMethods(type);
        final LinkedList<Method> methods = new LinkedList<>();
        final Set<String> methodNames = new HashSet<>();
        for (Method method : allMethods) {
            if (method.isAnnotationPresent(annotationType)
                && method.getParameterTypes().length == 0
                && method.getReturnType() == void.class
                && methodNames.add(method.getName())) {
                method.setAccessible(true);
                methods.addFirst(method);
            }
        }
        return methods.toArray(new Method[methods.size()]);
    }

    private List<Method> getAllMethods(Class<?> c) {
        final List<Method> list = new ArrayList<>();
        while (c != null && c != Object.class) {
            Collections.addAll(list, c.getDeclaredMethods());
            c = c.getSuperclass();
        }
        return list;
    }
}
