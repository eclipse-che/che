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
package org.eclipse.che.commons.lang.concurrent;

import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.MDC;

/** @author andrew00x */
class CopyThreadLocalCallable<T> implements Callable<T> {
  private final Callable<? extends T> wrapped;
  private final ThreadLocalPropagateContext.ThreadLocalState threadLocalState;
  private Map<String, String> currentMdcState;

  CopyThreadLocalCallable(Callable<? extends T> wrapped) {
    // Called from main thread. Copy the current values of all the ThreadLocal variables which
    // registered in ThreadLocalPropagateContext.
    this.wrapped = wrapped;
    this.threadLocalState = ThreadLocalPropagateContext.currentThreadState();
    this.currentMdcState = MDC.getCopyOfContextMap();
  }

  @Override
  public T call() throws Exception {
    try {
      threadLocalState.propagate();
      if (currentMdcState != null) {
        MDC.setContextMap(currentMdcState);
      }
      return wrapped.call();
    } finally {
      threadLocalState.cleanup();
      MDC.clear();
    }
  }

  public Callable<? extends T> getWrapped() {
    return wrapped;
  }
}
