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
import java.io.Writer;

/**
 * Line consumer that consume lines to provided Writer.
 *
 * @author Sergii Kabashniuk
 */
public class WritableLineConsumer implements LineConsumer {

  private final Writer writer;

  public WritableLineConsumer(Writer writer) {
    this.writer = writer;
  }

  @Override
  public void writeLine(String line) throws IOException {
    writer.write(line);
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
