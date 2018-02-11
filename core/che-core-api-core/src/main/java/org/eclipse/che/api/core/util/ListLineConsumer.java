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

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of line consumer that stores in the list strings that are passed with method
 * {@link #writeLine}.
 *
 * <p>Implementation is not threadsafe and requires external synchronization if is used in
 * multi-thread environment.
 *
 * @author andrew00x
 */
public class ListLineConsumer implements LineConsumer {
  protected final LinkedList<String> lines;
  private long textSizeLimit = -1;
  private long consumedTextSize;

  public ListLineConsumer() {
    lines = new LinkedList<>();
  }

  /**
   * Limit size of consuming text.
   *
   * @param textSizeLimit maximum number of symbols of consuming text. Don't limit data if {@code
   *     textSizeLimit} < 0. Don't consume if {@code textSizeLimit} = 0.
   */
  public ListLineConsumer(long textSizeLimit) {
    lines = new LinkedList<>();
    this.textSizeLimit = textSizeLimit;
  }

  @Override
  public void writeLine(String line) {
    if (textSizeLimit < 0) {
      lines.add(line);
      return;
    }

    if (consumedTextSize >= textSizeLimit) {
      return;
    }

    if (line != null && (line.length() + consumedTextSize > textSizeLimit)) {
      long lineSizeLimit = textSizeLimit - consumedTextSize;
      lines.add(line.substring(0, (int) lineSizeLimit));
      consumedTextSize += lineSizeLimit;
      return;
    }

    lines.add(line);
    consumedTextSize += line == null ? 0 : line.length();
  }

  @Override
  public void close() {}

  public void clear() {
    lines.clear();
    consumedTextSize = 0;
  }

  public List<String> getLines() {
    return new LinkedList<>(lines);
  }

  public String getText() {
    if (lines.isEmpty()) {
      return "";
    }
    final StringBuilder output = new StringBuilder();
    int n = 0;
    for (String line : lines) {
      if (n > 0) {
        output.append('\n');
      }
      output.append(line);
      n++;
    }
    return output.toString();
  }
}
