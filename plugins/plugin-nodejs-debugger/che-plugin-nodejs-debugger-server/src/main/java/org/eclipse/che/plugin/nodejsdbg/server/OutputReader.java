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
package org.eclipse.che.plugin.nodejsdbg.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Continuously reads process output and store in the {@code #outputs}.
 *
 * @author Anatolii Bazko
 */
public class OutputReader implements Runnable {
  public static final String CONNECTIVITY_TEST_NEEDED_MSG = "_CONNECTIVITY_TEST_NEEDED_";
  public static final String DEBUG_TIMED_OUT_MSG = "_DEBUG_TIMED_OUT_";
  public static final int CONNECTIVITY_TEST_NEEDED = 10;
  public static final int DEBUG_TIMED_OUT = 20;

  private static final Logger LOG = LoggerFactory.getLogger(OutputReader.class);
  private static final int MAX_OUTPUT = 4096;

  private final StringBuffer outputBuffer;
  private final Process process;
  private final String outputSeparator;
  private final ProcessCallback callback;

  private int noResponseTimes;

  public OutputReader(Process process, String outputSeparator, ProcessCallback callback) {
    this.process = process;
    this.outputBuffer = new StringBuffer();
    this.outputSeparator = outputSeparator;
    this.callback = callback;
  }

  @Override
  public void run() {
    try {
      InputStream in = getInputStream();
      if (in != null) {
        noResponseTimes = 0;
        proceedResponse(in);
      } else {
        noResponseTimes++;
        checkConnectivityState();
      }
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  private void proceedResponse(InputStream in) throws IOException {
    String outputData = read(in);
    if (!outputData.isEmpty()) {
      outputBuffer.append(outputData);
      if (outputBuffer.length() > MAX_OUTPUT) {
        outputBuffer.delete(0, outputBuffer.length() - MAX_OUTPUT);
      }
      extractOutputs();
    }
  }

  private String read(InputStream in) throws IOException {
    int available = Math.min(in.available(), MAX_OUTPUT);
    byte[] buf = new byte[available];
    int read = in.read(buf, 0, available);

    return new String(buf, 0, read, StandardCharsets.UTF_8);
  }

  private InputStream getInputStream() throws IOException {
    return hasError()
        ? process.getErrorStream()
        : (hasInput() ? (hasError() ? process.getErrorStream() : process.getInputStream()) : null);
  }

  private void extractOutputs() {
    int indexOf;
    while ((indexOf = outputBuffer.indexOf(outputSeparator)) >= 0) {
      NodeJsOutput nodeJsOutput = NodeJsOutput.of(outputBuffer.substring(0, indexOf));
      outputBuffer.delete(0, indexOf + outputSeparator.length());

      callback.onOutputProduced(nodeJsOutput);
    }
  }

  private boolean hasError() throws IOException {
    return process.getErrorStream().available() != 0;
  }

  private boolean hasInput() throws IOException {
    return process.getInputStream().available() != 0;
  }

  private void checkConnectivityState() {
    if (noResponseTimes == CONNECTIVITY_TEST_NEEDED) {
      callback.onOutputProduced(NodeJsOutput.of(CONNECTIVITY_TEST_NEEDED_MSG));
    } else if (noResponseTimes == DEBUG_TIMED_OUT) {
      callback.onOutputProduced(NodeJsOutput.of(DEBUG_TIMED_OUT_MSG));
    }
  }

  public interface ProcessCallback {
    void onOutputProduced(NodeJsOutput nodeJsOutput);
  }
}
