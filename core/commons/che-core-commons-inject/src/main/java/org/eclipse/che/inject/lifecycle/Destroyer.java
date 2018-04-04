/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.inject.lifecycle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

/** @author andrew00x */
public final class Destroyer {
  // Don't prevent instance from being discarded by the garbage collector.
  private final WeakHashMap<Object, Method[]> map;
  private final DestroyErrorHandler errorHandler;

  public Destroyer(DestroyErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
    map = new WeakHashMap<>();
  }

  public void add(Object instance, Method[] m) {
    synchronized (map) {
      map.put(instance, m);
    }
  }

  public void destroy() {
    synchronized (map) {
      for (Map.Entry<Object, Method[]> entry : map.entrySet()) {
        final Object instance = entry.getKey();
        final Method[] methods = entry.getValue();
        for (Method method : methods) {
          try {
            method.invoke(instance);
          } catch (IllegalArgumentException e) {
            // method MUST NOT have any parameters
            errorHandler.onError(instance, method, e);
          } catch (IllegalAccessException e) {
            errorHandler.onError(instance, method, e);
          } catch (InvocationTargetException e) {
            errorHandler.onError(instance, method, e.getTargetException());
          }
        }
      }
    }
  }
}
