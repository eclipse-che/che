/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.lang.execution;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asynchronously non blocking read process output. Class use external executor to run read thread
 *
 * @author Evgen Vidolob
 */
public class OutputReader {
  private static final Logger LOG = LoggerFactory.getLogger(OutputReader.class);

  private static final int SLEEP_WHEN_WAS_ACTIVE = 1;
  private static final int SLEEP_WHEN_IDLE = 5;

  private final Executor executor;
  private final Consumer<String> textConsumer;
  private final Reader reader;

  private final char[] buffer = new char[8192];
  private final StringBuilder lineBuilder = new StringBuilder();

  private Future<?> readingFuture;

  private volatile boolean isStopped;

  public OutputReader(Reader reader, Executor executor, Consumer<String> textConsumer) {
    this.executor = executor;
    this.textConsumer = textConsumer;
    this.reader = reader;
  }

  /** Start reading thread */
  public void start() {
    if (readingFuture == null) {
      readingFuture = executor.execute(() -> doStart());
    }
  }

  public void stop() {
    isStopped = true;
  }

  public void waitFor() throws InterruptedException {
    try {
      readingFuture.get();
    } catch (java.util.concurrent.ExecutionException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  private void doStart() {
    try {
      while (true) {
        boolean active = readIfAvailable();
        if (isStopped) {
          break;
        }

        try {
          Thread.sleep(getTimeToSleep(active));
        } catch (InterruptedException ignored) {
        }
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    } finally {
      close();
    }
  }

  private void close() {
    try {
      reader.close();
    } catch (IOException e) {
      LOG.error("Cannot close stream", e);
    }
  }

  private int getTimeToSleep(boolean active) {
    return active ? SLEEP_WHEN_WAS_ACTIVE : SLEEP_WHEN_IDLE;
  }

  private boolean readIfAvailable() throws IOException {
    boolean read = false;
    int charCount;
    while (reader.ready() && (charCount = reader.read(buffer)) > 1) {
      read = true;
      processRead(lineBuilder, buffer, charCount);
    }

    if (lineBuilder.length() > 0) {
      consumeLine(lineBuilder);
    }
    return read;
  }

  private void consumeLine(StringBuilder lineBuilder) {
    textConsumer.accept(lineBuilder.toString());
    lineBuilder.setLength(0);
  }

  private void processRead(StringBuilder lineBuilder, char[] buffer, int charCount) {
    for (int i = 0; i < charCount; i++) {
      char c = buffer[i];
      lineBuilder.append(c);
      if (c == '\n') {
        consumeLine(lineBuilder);
      }
    }
  }
}
