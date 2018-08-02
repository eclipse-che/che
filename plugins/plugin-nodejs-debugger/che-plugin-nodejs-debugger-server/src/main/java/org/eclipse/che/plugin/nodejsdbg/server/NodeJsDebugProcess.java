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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerException;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerTerminatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper over NodeJs process is being run. Communication is performed through standard
 * input/output streams.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsDebugProcess implements NodeJsProcessObservable {
  private static final Logger LOG = LoggerFactory.getLogger(NodeJsDebugProcess.class);
  private static final String NODEJS_COMMAND = detectNodeJsCommand();

  private final Process process;
  private final String outputSeparator;
  private final ScheduledExecutorService executor;
  private final BufferedWriter processWriter;
  private final List<NodeJsProcessObserver> observers;

  private NodeJsDebugProcess(String outputSeparator, String... options)
      throws NodeJsDebuggerException {
    this.observers = new CopyOnWriteArrayList<>();
    this.outputSeparator = outputSeparator;

    process = initializeNodeJsDebugProcess(options);
    processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

    executor =
        Executors.newScheduledThreadPool(
            1,
            new ThreadFactoryBuilder()
                .setNameFormat("nodejs-debugger-%d")
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .setDaemon(true)
                .build());

    OutputReader outputReader = new OutputReader(process, outputSeparator, this::notifyObservers);
    executor.scheduleWithFixedDelay(outputReader, 0, 100, TimeUnit.MILLISECONDS);
  }

  public static NodeJsDebugProcess start(String file) throws NodeJsDebuggerException {
    return new NodeJsDebugProcess("debug> ", "inspect", "--inspect-brk", file);
  }

  private Process initializeNodeJsDebugProcess(String[] options) throws NodeJsDebuggerException {
    List<String> commands = new ArrayList<>(1 + options.length);
    commands.add(NODEJS_COMMAND);
    commands.addAll(Arrays.asList(options));

    ProcessBuilder processBuilder = new ProcessBuilder(commands);
    try {
      return processBuilder.start();
    } catch (IOException e) {
      throw new NodeJsDebuggerException("NodeJs process initialization failed.", e);
    }
  }

  /** Stops nodejs process. */
  public void stop() {
    boolean interrupted = false;
    observers.clear();

    try {
      send("quit");
    } catch (NodeJsDebuggerException e) {
      LOG.warn("Failed to execute 'quit' command. " + e.getMessage());
    }

    try {
      processWriter.close();
    } catch (IOException ignored) {
      // ignore
    }

    executor.shutdown();
    try {
      if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
        executor.shutdownNow();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
          LOG.error("Unable to terminate pool of NodeJs debugger tasks.");
        }
      }
    } catch (InterruptedException e) {
      interrupted = true;
      if (!executor.isShutdown()) {
        LOG.error("Unable to terminate pool of NodeJs debugger tasks.");
      }
    }

    process.destroy();
    try {
      if (!process.waitFor(10, TimeUnit.SECONDS)) {
        LOG.error("Unable to terminate NodeJs process.");
      }
    } catch (InterruptedException e) {
      interrupted = true;
      if (!process.isAlive()) {
        LOG.error("Unable to terminate NodeJs process.");
      }
    }

    if (interrupted) {
      Thread.currentThread().interrupt();
    }
  }

  /** Synchronizes sending commands. */
  public synchronized void send(String command) throws NodeJsDebuggerException {
    LOG.debug("Execute: {}", command);
    if (!process.isAlive()) {
      throw new NodeJsDebuggerTerminatedException("NodeJs process has been terminated.");
    }
    try {
      processWriter.write(command);
      processWriter.newLine();
      processWriter.flush();
    } catch (IOException e) {
      throw new NodeJsDebuggerException(e.getMessage(), e);
    }
  }

  /** Returns NodeJs command to run: either {@code nodejs} or {@code node}. */
  private static String detectNodeJsCommand() {
    String detectionCommand =
        "if command -v nodejs >/dev/null 2>&1; then echo -n 'nodejs'; else echo -n 'node'; fi";
    ProcessBuilder builder = new ProcessBuilder("sh", "-c", detectionCommand);

    try {
      Process process = builder.start();
      int resultCode = process.waitFor();
      if (resultCode != 0) {
        String errMsg = IoUtil.readAndCloseQuietly(process.getErrorStream());
        throw new IllegalStateException("NodeJs not found. " + errMsg);
      }

      return IoUtil.readAndCloseQuietly(process.getInputStream());
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException("NodeJs not found", e);
    }
  }

  private void notifyObservers(NodeJsOutput nodeJsOutput) {
    LOG.debug("{}{}", outputSeparator, nodeJsOutput.getOutput());

    if (OutputReader.CONNECTIVITY_TEST_NEEDED_MSG.equals(nodeJsOutput.getOutput())) {
      testConnectivity();
      return;
    }

    for (NodeJsProcessObserver observer : observers) {
      try {
        if (observer.onOutputProduced(nodeJsOutput)) {
          break;
        }
      } catch (NodeJsDebuggerException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  private void testConnectivity() {
    try {
      send("version");
    } catch (NodeJsDebuggerException ignored) {
      // ignore
    }
  }

  @Override
  public void addObserver(NodeJsProcessObserver observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(NodeJsProcessObserver observer) {
    observers.remove(observer);
  }
}
