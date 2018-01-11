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
package org.eclipse.che.commons.lang.concurrent;

/** @author andrew00x */
class CopyThreadLocalRunnable implements Runnable {
  private final Runnable wrapped;
  private final ThreadLocalPropagateContext.ThreadLocalState threadLocalState;

  CopyThreadLocalRunnable(Runnable wrapped) {
    // Called from main thread. Copy the current values of all the ThreadLocal variables which
    // registered in ThreadLocalPropagateContext.
    this.wrapped = wrapped;
    this.threadLocalState = ThreadLocalPropagateContext.currentThreadState();
  }

  @Override
  public void run() {
    try {
      threadLocalState.propagate();
      wrapped.run();
    } finally {
      threadLocalState.cleanup();
    }
  }

  public Runnable getWrapped() {
    return wrapped;
  }
}
