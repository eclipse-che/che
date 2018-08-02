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
package org.eclipse.che.plugin.gdb.server;

import static java.lang.Math.min;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.gdb.server.parser.GdbOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anatoliy Bazko */
public abstract class GdbProcess {
  private static final Logger LOG = LoggerFactory.getLogger(GdbProcess.class);
  private static final int MAX_CAPACITY = 1000;
  private static final int MAX_OUTPUT = 4096;

  protected int pid;
  protected final Process process;
  protected final String outputSeparator;
  protected final BlockingQueue<GdbOutput> outputs;
  protected final Thread outputReader;

  public GdbProcess(String outputSeparator, String... commands) throws IOException {
    this.outputSeparator = outputSeparator;
    this.outputs = new ArrayBlockingQueue<>(MAX_CAPACITY);

    ProcessBuilder processBuilder = new ProcessBuilder(commands);
    process = processBuilder.start();

    outputReader = new OutputReader(commands[0] + " output reader");
    outputReader.setDaemon(true);
    outputReader.start();

    try {
      Field pidField =
          Thread.currentThread()
              .getContextClassLoader()
              .loadClass("java.lang.UNIXProcess")
              .getDeclaredField("pid");
      pidField.setAccessible(true);
      pid = ((Number) pidField.get(process)).intValue();
    } catch (Exception e) {
      pid = -1;
      LOG.error(e.getMessage(), e);
    }
  }

  /** Stops process. */
  protected void stop() {
    outputReader.interrupt();
    outputs.clear();
    process.destroy();
  }

  /** Continuously reads process output and store in the {@code #outputs}. */
  private class OutputReader extends Thread {

    public OutputReader(String name) {
      super(name);
    }

    @Override
    public void run() {
      StringBuilder buf = new StringBuilder();

      while (!isInterrupted()) {
        if (!process.isAlive()) {
          outputs.add(GdbOutput.of(buf.toString(), true));
          break;
        }

        try {
          InputStream in = getInput();
          if (in != null) {
            String data = read(in);
            if (!data.isEmpty()) {
              buf.append(data);
              if (buf.length() > MAX_OUTPUT) {
                buf.delete(0, buf.length() - MAX_OUTPUT);
              }
            }
          }
        } catch (IOException e) {
          LOG.error(e.getMessage(), e);
        }

        if (buf.length() > 0) {
          extractOutput(buf);
        }

        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          break;
        }
      }

      LOG.debug(getName() + " has been stopped");
    }

    private InputStream getInput() throws IOException {
      return hasError()
          ? process.getErrorStream()
          : (hasInput()
              ? (hasError() ? process.getErrorStream() : process.getInputStream())
              : null);
    }

    private void extractOutput(StringBuilder buf) {
      int indexOf;
      while ((indexOf = buf.indexOf(outputSeparator)) >= 0) {
        GdbOutput gdbOutput = GdbOutput.of(buf.substring(0, indexOf));
        outputs.add(gdbOutput);

        LOG.debug(gdbOutput.getOutput());

        buf.delete(0, indexOf + outputSeparator.length());
      }
    }

    private boolean hasError() throws IOException {
      return process.getErrorStream().available() != 0;
    }

    private boolean hasInput() throws IOException {
      return process.getInputStream().available() != 0;
    }

    @Nullable
    private String read(InputStream in) throws IOException {
      int available = min(in.available(), MAX_OUTPUT);
      byte[] buf = new byte[available];
      int read = in.read(buf, 0, available);

      return new String(buf, 0, read, StandardCharsets.UTF_8);
    }
  }
}
