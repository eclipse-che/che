/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author andrew00x */
class LogMessagePumper extends MessagePumper<LogMessage> {
  private static final Logger LOG = LoggerFactory.getLogger(LogMessagePumper.class);

  private static final int STREAM_HEADER_LENGTH = 8;
  private static final int MAX_LINE_LENGTH = 1024;

  private final InputStream source;
  private final MessageProcessor<LogMessage> target;

  LogMessagePumper(InputStream source, MessageProcessor<LogMessage> target) {
    super(null, null);
    this.source = source;
    this.target = target;
  }

  @Override
  void start() throws IOException {
    final byte[] buf = new byte[MAX_LINE_LENGTH];
    StringBuilder lineBuf = null;
    boolean endOfLine = false;
    LogMessage.Type logMessageType = LogMessage.Type.DOCKER;
    for (; ; ) {
      int r = ByteStreams.read(source, buf, 0, STREAM_HEADER_LENGTH);
      if (r != 8) {
        if (r != -1) {
          LOG.debug(
              "Invalid stream, can't read header. Header of each frame must contain 8 bytes but got {}",
              r);
        }
        if (lineBuf != null && lineBuf.length() > 0) {
          target.process(new LogMessage(logMessageType, lineBuf.toString()));
          lineBuf.setLength(0);
        }
        break;
      }
      logMessageType = getLogMessageType(buf);
      int remaining = getPayloadLength(buf);
      while (remaining > 0) {
        r = source.read(buf, 0, Math.min(remaining, buf.length));
        int offset = 0;
        int lineLength = lineBuf != null ? lineBuf.length() : 0;
        for (int i = 0; i < r; i++, lineLength++) {
          endOfLine = false;
          if (buf[i] == '\n' || buf[i] == '\r' || lineLength > MAX_LINE_LENGTH) {
            int length = i - offset;
            boolean isLineFeedFollowed = false;
            if (buf[i] == '\r') {
              int nextIndex = i + 1;
              isLineFeedFollowed =
                  nextIndex < MAX_LINE_LENGTH && nextIndex < r && buf[nextIndex] == '\n';
              if (!isLineFeedFollowed) {
                length += 1; // include <CR> char in log message
              }
            }
            if (lineBuf != null && lineBuf.length() > 0) {
              lineBuf.append(new String(buf, offset, length));
              target.process(new LogMessage(logMessageType, lineBuf.toString()));
              lineBuf.setLength(0);
            } else {
              target.process(new LogMessage(logMessageType, new String(buf, offset, length)));
            }

            if (isLineFeedFollowed) {
              i++;
            }
            offset = i + 1;
            lineLength = 0;
            endOfLine = true;
          }
        }
        if (!endOfLine) {
          if (lineBuf == null) {
            lineBuf = new StringBuilder(MAX_LINE_LENGTH);
          }
          lineBuf.append(new String(buf, offset, r - offset));
        }
        remaining -= r;
      }
    }
  }

  private int getPayloadLength(byte[] header) {
    return (header[7] & 0xFF)
        + ((header[6] & 0xFF) << 8)
        + ((header[5] & 0xFF) << 16)
        + ((header[4] & 0xFF) << 24);
  }

  private LogMessage.Type getLogMessageType(byte[] header) {
    switch (header[0]) {
      case 0:
        return LogMessage.Type.STDIN;
      case 1:
        return LogMessage.Type.STDOUT;
      case 2:
        return LogMessage.Type.STDERR;
      default:
        throw new IllegalArgumentException(
            String.format("Invalid docker stream type %d", header[0]));
    }
  }
}
