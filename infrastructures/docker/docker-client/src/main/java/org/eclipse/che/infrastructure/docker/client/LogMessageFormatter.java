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
