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

import static org.eclipse.che.api.core.util.ErrorFilteredConsumer.ErrorIndicator.DEFAULT_ERROR_INDICATOR;

import java.io.IOException;

/**
 * Consumes all output and redirect to another consumer only those lines which contain errors.
 *
 * @author Anatolii Bazko
 */
public class ErrorFilteredConsumer implements LineConsumer {

  private final ErrorIndicator errorIndicator;
  private final LineConsumer lineConsumer;

  public ErrorFilteredConsumer(ErrorIndicator errorIndicator, LineConsumer lineConsumer) {
    this.errorIndicator = errorIndicator;
    this.lineConsumer = lineConsumer;
  }

  public ErrorFilteredConsumer(LineConsumer lineConsumer) {
    this(DEFAULT_ERROR_INDICATOR, lineConsumer);
  }

  @Override
  public void writeLine(String line) throws IOException {
    if (errorIndicator.isError(line)) {
      lineConsumer.writeLine(line);
    }
  }

  @Override
  public void close() throws IOException {
    lineConsumer.close();
  }

  /** Indicates if line contains a error message. */
  @FunctionalInterface
  public interface ErrorIndicator {
    boolean isError(String line);

    ErrorIndicator DEFAULT_ERROR_INDICATOR = line -> line.startsWith("[STDERR]");
  }
}
