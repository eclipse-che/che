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
package org.eclipse.che.infrastructure.docker.client;

/**
 * Format/beautify string representation of log messages returned by docker.
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public interface LogMessageFormatter extends MessageFormatter<LogMessage> {
  String format(LogMessage logMessage);

  LogMessageFormatter DEFAULT =
      new LogMessageFormatter() {
        @Override
        public String format(LogMessage logMessage) {
          final StringBuilder sb = new StringBuilder();
          final LogMessage.Type type = logMessage.getType();
          switch (type) {
            case STDOUT:
              sb.append("[STDOUT]");
              break;
            case STDERR:
              sb.append("[STDERR]");
              break;
            case DOCKER:
              sb.append("[DOCKER]");
              break;
            default:
          }
          final String content = logMessage.getContent();
          if (content != null) {
            sb.append(' ');
            sb.append(content);
          }
          return sb.toString();
        }
      };
}
