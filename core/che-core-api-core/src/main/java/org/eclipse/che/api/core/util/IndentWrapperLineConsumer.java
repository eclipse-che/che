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

import java.io.IOException;

/**
 * Line consumer for adding system indent in the end of the line
 *
 * @author Andrienko Alexander
 */
public class IndentWrapperLineConsumer implements LineConsumer {

  private final LineConsumer lineConsumer;

  public IndentWrapperLineConsumer(LineConsumer lineConsumer) {
    this.lineConsumer = lineConsumer;
  }

  /** {@inheritDoc} */
  @Override
  public void writeLine(String line) throws IOException {
    lineConsumer.writeLine(line + System.lineSeparator());
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws IOException {
    lineConsumer.close();
  }
}
