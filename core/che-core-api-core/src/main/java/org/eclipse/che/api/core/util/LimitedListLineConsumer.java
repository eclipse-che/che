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

/**
 * Extension of {@link ListLineConsumer} which consumes limited amount of text {@link #writeLine}.
 *
 * <p>Implementation is not threadsafe and requires external synchronization if is used in
 * multi-thread environment.
 *
 * @author Dmytro Nochevnov
 */
public class LimitedListLineConsumer extends ListLineConsumer {
  private final long textSizeLimit;
  private long consumedTextSize;

  /**
   * Limits size of consuming text.
   *
   * @param textSizeLimit maximum number of symbols of consuming text.
   * Don't limit text if {@code textSizeLimit} < 0.
   * Don't consume if {@code textSizeLimit} = 0.
   */
  public LimitedListLineConsumer(long textSizeLimit) {
    super();
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
  public void clear() {
    lines.clear();
    consumedTextSize = 0;
  }
}
