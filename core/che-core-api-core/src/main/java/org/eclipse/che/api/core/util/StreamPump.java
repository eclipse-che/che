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
package org.eclipse.che.api.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** @author andrew00x */
public final class StreamPump implements Runnable {

  private BufferedReader bufferedReader;
  private LineConsumer lineConsumer;

  private Exception exception;
  private boolean done;

  public synchronized void start(Process process, LineConsumer lineConsumer) {
    this.lineConsumer = lineConsumer;
    bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    final Thread t = new Thread(this, "StreamPump");
    t.setDaemon(true);
    t.start();
  }

  public synchronized void stop() {
    // Not clear do we need close original stream, but since it was wrapped by BufferedReader close
    // it anyway.
    try {
      bufferedReader.close();
    } catch (IOException ignored) {
    }
  }

  public synchronized void await() throws InterruptedException {
    while (!done) {
      wait();
    }
  }

  public synchronized boolean isDone() {
    return done;
  }

  public boolean hasError() {
    return null != exception;
  }

  public Exception getException() {
    return exception;
  }

  @Override
  public void run() {
    String line;
    try {
      while ((line = bufferedReader.readLine()) != null) {
        lineConsumer.writeLine(line);
      }
    } catch (IOException e) {
      exception = e;
    } finally {
      synchronized (this) {
        done = true;
        notifyAll();
      }
    }
  }
}
