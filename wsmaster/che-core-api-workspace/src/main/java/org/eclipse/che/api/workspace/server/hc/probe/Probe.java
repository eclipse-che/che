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
package org.eclipse.che.api.workspace.server.hc.probe;

/**
 * One-time probe for a server. Should not be used directly but rather by a probe scheduling
 * framework.
 *
 * @author Alexander Garagatyi
 */
public abstract class Probe {

  private Thread probeThread;

  /**
   * Checks {@link Probe}. Note that it must not be called more than one time.
   *
   * @return true if probe finishes successfully, false otherwise
   * @throws IllegalStateException if called second time
   */
  final boolean probe() {
    if (probeThread != null) {
      throw new IllegalStateException(
          "This probe can be used only once, but second usage is detected!");
    }
    probeThread = Thread.currentThread();
    try {
      return doProbe();
    } finally {
      // clear interrupted state
      Thread.interrupted();
    }
  }

  /**
   * Returns {@code true} if probe finishes successfully, {@code false} otherwise. Must return false
   * when probe is interrupted even if interruption is not respected by probe implementation.
   */
  protected abstract boolean doProbe();

  /**
   * Interrupts execution of the probe. May be useful when probing takes too much time. Doesn't
   * guarantee that interruption is respected by the probe or will lead to the immediate stop of
   * usage of a thread where {@link #probe()} is called.
   */
  public void cancel() {
    probeThread.interrupt();
  }
}
