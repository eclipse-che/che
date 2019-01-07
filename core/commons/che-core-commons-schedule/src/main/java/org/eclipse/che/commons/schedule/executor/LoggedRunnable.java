/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.schedule.executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Invoke given method of given object.
 *
 * @author Sergii Kabashniuk
 */
public class LoggedRunnable implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(LoggedRunnable.class);

  private final Object object;
  private final Method method;

  public LoggedRunnable(Object object, Method method) {
    this.object = object;
    this.method = method;
  }

  @Override
  public void run() {
    long startTime = System.currentTimeMillis();
    try {
      if (object instanceof Runnable
          && method.getName().equals("run")
          && method.getParameterTypes().length == 0) {
        LOG.debug(
            "Invoking method 'run' of class '{}' instance '{}'",
            object.getClass().getName(),
            object);

        ((Runnable) object).run();

        LOG.debug(
            "Method of class '{}' instance '{}' is completed in {} sec",
            object.getClass().getName(),
            object,
            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
      } else {
        try {
          LOG.debug(
              "Invoking method '{}' of class '{}' instance '{}'",
              method.getName(),
              object.getClass().getName(),
              object);

          method.invoke(object);

          LOG.debug(
              "Method of class '{}' instance '{}' is completed in {} sec",
              object.getClass().getName(),
              object,
              TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
        } catch (InvocationTargetException | IllegalAccessException e) {
          LOG.error(
              "Error occurred during invocation of method '{}#{}'. Instance: '{}'. Error: {}",
              object.getClass().getName(),
              method.getName(),
              object,
              e.getMessage(),
              e);
        }
      }
    } catch (Exception e) {
      LOG.error(
          "Error occurred during invocation of method '{}#{}'. Instance: '{}'. Error: {}",
          object.getClass().getName(),
          method.getName(),
          object,
          e.getMessage(),
          e);
      throw e;
    }
  }

  @Override
  public String toString() {
    return "LoggedRunnable{"
        + "methodToInvoke="
        + object.getClass().getName()
        + '#'
        + method.getName()
        + '}';
  }
}
