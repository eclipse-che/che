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
package org.eclipse.che.api.core.util;

import java.io.IOException;

/**
 * Wrapper of {@link LineConsumer} which limits total size of consuming lines.
 *
 * @author Dmytro Nochevnov
 * @author Anatolii Bazko
 */
public class LimitedSizeLineConsumerWrapper implements LineConsumer {
  private final int limit;
  private final LineConsumer lineConsumer;
  private int totalLength;

  /**
   * @param limit maximum total size of consuming lines.
   * @throws IllegalArgumentException if the {@code limit} is negative.
   */
  public LimitedSizeLineConsumerWrapper(LineConsumer lineConsumer, int limit) {
    if (limit < 0) {
      throw new IllegalArgumentException("Limit number shouldn't be negative.");
    }

    this.lineConsumer = lineConsumer;
    this.limit = limit;
  }

  /**
   * Consumes line and ensures that total size of consumed lines doesn't exceed {@link
   * LimitedSizeLineConsumerWrapper#limit}. Doesn't write line if limit has been already succeeded
   * before. Trims {@code line} to prevent exceeding of {@link
   * LimitedSizeLineConsumerWrapper#limit}.
   *
   * @param line line to consume.
   */
  @Override
  public void writeLine(String line) throws IOException {
    if (line == null) {
      return;
    }

    if (totalLength >= limit) {
      return;
    }

    int allowedLineLength = Math.min(limit - totalLength, line.length());
    lineConsumer.writeLine(line.substring(0, allowedLineLength));
    totalLength += allowedLineLength;
  }

  @Override
  public void close() throws IOException {
    lineConsumer.close();
  }
}
