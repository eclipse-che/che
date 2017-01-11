/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.nodejsdbg.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerException;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerTerminatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;

/**
 * Wrapper over NodeJs process is being run.
 * Communication is performed through standard input/output streams.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsDebugProcess implements NodeJsProcessObservable {
    private static final Logger LOG        = LoggerFactory.getLogger(NodeJsDebugProcess.class);
    private static final int    MAX_OUTPUT = 4096;

    private static final String NODEJS_COMMAND = detectNodeJsCommand();

    private final Process                  process;
    private final String                   outputSeparator;
    private final ScheduledExecutorService executor;
    private final BufferedWriter           processWriter;

    private final List<NodeJsProcessObserver> observers;

    private NodeJsDebugProcess(String outputSeparator, String... options) throws NodeJsDebuggerException {
        this.observers = new CopyOnWriteArrayList<>();
        this.outputSeparator = outputSeparator;

        List<String> commands = new ArrayList<>(1 + options.length);
        commands.add(NODEJS_COMMAND);
        commands.addAll(Arrays.asList(options));

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new NodeJsDebuggerException("NodeJs process failed.", e);
        }

        processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        executor = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("nodejs-debugger-%d")
                                                                                 .setUncaughtExceptionHandler(
                                                                                         LoggingUncaughtExceptionHandler.getInstance())
                                                                                 .setDaemon(true)
                                                                                 .build());
        executor.scheduleWithFixedDelay(new OutputReader(), 0, 100, TimeUnit.MILLISECONDS);
    }

    public static NodeJsDebugProcess start(String file) throws NodeJsDebuggerException {
        return new NodeJsDebugProcess("debug> ", "debug", "--debug-brk", file);
    }

    @Override
    public void addObserver(NodeJsProcessObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(NodeJsProcessObserver observer) {
        observers.remove(observer);
    }

    /**
     * Stops process.
     */
    void stop() {
        try {
            send("quit");
        } catch (NodeJsDebuggerException e) {
            LOG.warn(e.getMessage());
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOG.warn("Unable to terminate main pool");
                }
            }
        } catch (InterruptedException e) {
            LOG.warn(e.getMessage());
        }


        process.destroy();
        try {
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                LOG.error("Unable to terminate NodeJs");
            }
        } catch (InterruptedException e) {
            LOG.warn(e.getMessage());
        }

        try {
            processWriter.close();
        } catch (IOException e) {
            LOG.warn("Failed to close NodeJs process output stream", e);
        }
        observers.clear();
    }

    public synchronized void send(String command) throws NodeJsDebuggerException {
        LOG.debug("Execute: {}", command);

        if (!process.isAlive()) {
            throw new NodeJsDebuggerTerminatedException("NodeJs process is terminated.");
        }

        try {
            processWriter.write(command);
            processWriter.newLine();
            processWriter.flush();
        } catch (IOException e) {
            LOG.error(String.format("Command execution <%s> failed", command), e);
        }
    }


    /**
     * Continuously reads process output and store in the {@code #outputs}.
     */
    private class OutputReader implements Runnable {
        private final StringBuffer outputBuffer;

        public OutputReader() {
            this.outputBuffer = new StringBuffer();
        }

        @Override
        public void run() {
            try {
                InputStream in = getInput();
                if (in != null) {
                    String outputData = read(in);
                    if (!outputData.isEmpty()) {
                        outputBuffer.append(outputData);
                        if (outputBuffer.length() > MAX_OUTPUT) {
                            outputBuffer.delete(0, outputBuffer.length() - MAX_OUTPUT);
                        }

                        extractOutputs();
                    }
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        private InputStream getInput() throws IOException {
            return hasError() ? process.getErrorStream()
                              : (hasInput() ? (hasError() ? process.getErrorStream()
                                                          : process.getInputStream())
                                            : null);
        }

        private void extractOutputs() {
            int indexOf;
            while ((indexOf = outputBuffer.indexOf(outputSeparator)) >= 0) {
                NodeJsOutput nodeJsOutput = NodeJsOutput.of(outputBuffer.substring(0, indexOf));
                outputBuffer.delete(0, indexOf + outputSeparator.length());

                notifyObservers(nodeJsOutput);
            }
        }

        private boolean hasError() throws IOException {
            return process.getErrorStream().available() != 0;
        }

        private boolean hasInput() throws IOException {
            return process.getInputStream().available() != 0;
        }

        private String read(InputStream in) throws IOException {
            int available = min(in.available(), MAX_OUTPUT);
            byte[] buf = new byte[available];
            int read = in.read(buf, 0, available);

            return new String(buf, 0, read, StandardCharsets.UTF_8);
        }
    }

    private void notifyObservers(NodeJsOutput nodeJsOutput) {
        LOG.debug("{}{}", outputSeparator, nodeJsOutput.getOutput());

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

    /**
     * Returns NodeJs command to run: either {@code nodejs} or {@code node}.
     */
    private static String detectNodeJsCommand() {
        String detectionCommand = "if command -v nodejs >/dev/null 2>&1; then echo -n 'nodejs'; else echo -n 'node'; fi";
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

}
