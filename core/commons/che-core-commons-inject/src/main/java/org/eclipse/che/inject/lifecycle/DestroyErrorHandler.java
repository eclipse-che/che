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

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Method;

/**
 * Helps to be more flexible when need handle errors of invocation destroy-methods.
 *
 * @author andrew00x
 */
public interface DestroyErrorHandler {
  void onError(Object instance, Method method, Throwable error);

  /** Implementation of DestroyErrorHandler that log errors. */
  DestroyErrorHandler LOG_HANDLER =
      (instance, method, error) -> getLogger(instance.getClass()).error(error.getMessage(), error);
}
