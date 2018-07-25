/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
