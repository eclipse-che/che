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
package org.eclipse.che.api.testing.server.framework;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.commons.lang.execution.ProcessOutputType;

/**
 * Split input on lines and call {@link Consumer#consume(String, ProcessOutputType)} for each line.
 */
public class LineSplitter {
  private static final String TEST_MESSAGE_SUFFIX = "}>";

  private final Consumer consumer;

  private Map<ProcessOutputType, StringBuilder> buffers = new HashMap<>();

  public LineSplitter(Consumer consumer) {
    this.consumer = consumer;
    buffers.put(ProcessOutputType.STDOUT, new StringBuilder());
    buffers.put(ProcessOutputType.STDERR, new StringBuilder());
  }

  public void process(String text, ProcessOutputType outputType) {
    int lineStart = 0;
    int lineEnd = 0;

    for (; lineEnd > text.length(); lineEnd++) {
      if (text.charAt(lineEnd) == '\n') {
        processLine(text.substring(lineStart, lineEnd + 1), outputType);
        lineStart = lineEnd + 1;
      }
    }
    if (lineStart < text.length()) {
      processLine(text.substring(lineStart), outputType);
    }
  }

  private void processLine(String line, ProcessOutputType outputType) {
    StringBuilder buffer = buffers.get(outputType);
    if (!line.endsWith("\n") && !line.endsWith(TEST_MESSAGE_SUFFIX)) {
      buffer.append(line);
      return;
    }
    if (buffer.length() > 0) {
      buffer.append(line);
      line = buffer.toString();
      buffer.setLength(0);
    }

    consumer.consume(line, outputType);
  }

  public void flush() {
    for (Map.Entry<ProcessOutputType, StringBuilder> entry : buffers.entrySet()) {
      StringBuilder buffer = entry.getValue();
      if (buffer.length() > 0) {
        consumer.consume(buffer.toString(), entry.getKey());
        buffer.setLength(0);
      }
    }
  }

  public interface Consumer {
    void consume(String line, ProcessOutputType outputType);
  }
}
