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
package org.eclipse.che.workspace.infrastructure.docker;

import java.util.concurrent.CountDownLatch;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeStartInterruptedException;

/**
 * Controls the runtime start flow and helps to cancel it.
 *
 * <p>The runtime start with cancellation using the Start Synchronizer might look like:
 *
 * <pre>
 * ...
 *     public void startRuntime() {
 *          startSynchronizer.setStartThread();
 *          try {
 *               // .....
 *               startSynchronizer.complete();
 *          } catch (Exception ex) {
 *               startSynchronizer.completeExceptionally(ex);
 *               throw ex;
 *          }
 *     }
 * ...
 * </pre>
 *
 * <p>At the same time stopping might look like:
 *
 * <pre>
 * ...
 *     public void stopRuntime() {
 *          if (startSynchronizer.interrupt()) {
 *               try {
 *                  startSynchronizer.await();
 *               } catch (RuntimeStartInterruptedException ex) {
 *                  // normal stop
 *               } catch (InterruptedException ex) {
 *                  Thread.currentThread().interrupt();
 *                  ...
 *               }
 *          }
 *     }
 * ...
 * </pre>
 *
 * @author Alexander Garagatyi
 */
public class StartSynchronizer {

  private Exception exception;
  private Thread startThread;
  private CountDownLatch completionLatch;

  public StartSynchronizer() {
    this.completionLatch = new CountDownLatch(1);
  }

  /**
   * Sets {@link Thread#currentThread()} as a {@link #startThread}.
   *
   * @throws InternalInfrastructureException when {@link #startThread} is already set.
   */
  public synchronized void setStartThread() throws InternalInfrastructureException {
    if (startThread != null) {
      throw new InternalInfrastructureException(
          "Docker infrastructure context of workspace already started");
    }
    startThread = Thread.currentThread();
  }

  /**
   * Releases waiting task and reset the starting thread.
   *
   * @throws InterruptedException when execution thread was interrupted just before this method call
   */
  public synchronized void complete() throws InterruptedException {
    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException();
    }
    startThread = null;
    completionLatch.countDown();
  }

  /**
   * Releases waiting task, reset the starting thread and sets an exception if it is not null.
   *
   * @param ex completion exception might be null
   */
  public synchronized void completeExceptionally(Exception ex) {
    exception = ex;
    startThread = null;
    completionLatch.countDown();
  }

  /**
   * Interrupts the {@link #startThread} if its value different to null.
   *
   * @return true if {@link #startThread} interruption flag set, otherwise false will be returned
   */
  public synchronized boolean interrupt() {
    if (startThread != null) {
      startThread.interrupt();
      return true;
    }
    return false;
  }

  /**
   * Waits until {@link #complete} is called and rethrow the {@link #exception} if it present. This
   * call is blocking and it should be used with {@link #interrupt} method.
   *
   * @throws InterruptedException when this thread is interrupted while waiting for {@link
   *     #complete}
   * @throws RuntimeStartInterruptedException when {@link #startThread} successfully interrupted
   * @throws InfrastructureException when any error occurs while waiting for {@link #complete}
   */
  public void await() throws InterruptedException, InfrastructureException {
    completionLatch.await();
    synchronized (this) {
      if (exception != null) {
        try {
          throw exception;
        } catch (InfrastructureException rethrow) {
          throw rethrow;
        } catch (Exception ex) {
          throw new InternalInfrastructureException(ex);
        }
      }
    }
  }
}
