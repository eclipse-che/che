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
import org.slf4j.MDC;

/** @author andrew00x */
class CopyThreadLocalRunnable implements Runnable {
  private final Runnable wrapped;
  private final ThreadLocalPropagateContext.ThreadLocalState threadLocalState;
  private Map<String, String> currentMdcState;

  CopyThreadLocalRunnable(Runnable wrapped) {
    // Called from main thread. Copy the current values of all the ThreadLocal variables which
    // registered in ThreadLocalPropagateContext.
    this.wrapped = wrapped;
    this.threadLocalState = ThreadLocalPropagateContext.currentThreadState();
    this.currentMdcState = MDC.getCopyOfContextMap();
  }

  @Override
  public void run() {
    try {
      threadLocalState.propagate();
      if (currentMdcState != null) {
        MDC.setContextMap(currentMdcState);
      }
      wrapped.run();
    } finally {
      threadLocalState.cleanup();
      MDC.clear();
    }
  }

  public Runnable getWrapped() {
    return wrapped;
  }
}
