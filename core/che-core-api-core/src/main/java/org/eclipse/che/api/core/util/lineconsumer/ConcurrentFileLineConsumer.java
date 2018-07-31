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
package org.eclipse.che.api.core.util.lineconsumer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.eclipse.che.api.core.util.LineConsumer;

/**
 * Consumes logs and writes them into file. This implementation is thread safe.
 *
 * @author andrew00x
 * @author Mykola Morhun
 */
public class ConcurrentFileLineConsumer implements LineConsumer {
  private final File file;
  private final Writer writer;
  private final ReentrantReadWriteLock lock;

  private volatile boolean isOpen;

  public ConcurrentFileLineConsumer(File file) throws IOException {
    this.file = file;
    writer = Files.newBufferedWriter(file.toPath(), Charset.defaultCharset());
    isOpen = true;
    lock = new ReentrantReadWriteLock();
  }

  public File getFile() {
    return file;
  }

  public boolean isOpen() {
    return isOpen;
  }

  @Override
  public void writeLine(String line) throws IOException {
    if (isOpen && lock.readLock().tryLock()) {
      try {
        if (line != null) {
          writer.write(line);
        }
        writer.write('\n');
        writer.flush();
      } catch (IOException e) {
        if ("Stream closed".equals(e.getMessage())) {
          throw new ConsumerAlreadyClosedException(e.getMessage());
        }
        throw e;
      } finally {
        lock.readLock().unlock();
      }
    }
  }

  @Override
  public void close() throws IOException {
    if (isOpen) {
      lock.writeLock().lock();
      try {
        isOpen = false;
        writer.close();
      } finally {
        lock.writeLock().unlock();
      }
    }
  }
}
