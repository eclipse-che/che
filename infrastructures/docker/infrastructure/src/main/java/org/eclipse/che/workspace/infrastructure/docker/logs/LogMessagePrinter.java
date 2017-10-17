/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.logs;

import java.io.IOException;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.infrastructure.docker.client.LogMessage;
import org.eclipse.che.infrastructure.docker.client.LogMessageFormatter;
import org.eclipse.che.infrastructure.docker.client.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author andrew00x */
public class LogMessagePrinter implements MessageProcessor<LogMessage> {
  private static final Logger LOG = LoggerFactory.getLogger(LogMessagePrinter.class);

  private final LineConsumer output;
  private final LogMessageFormatter formatter;

  public LogMessagePrinter(LineConsumer output, LogMessageFormatter formatter) {
    this.output = output;
    this.formatter = formatter;
  }

  public LogMessagePrinter(LineConsumer output) {
    this(output, LogMessageFormatter.DEFAULT);
  }

  @Override
  public void process(LogMessage logMessage) {
    try {
      output.writeLine(formatter.format(logMessage));
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
  }
}
