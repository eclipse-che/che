/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.nodejsdbg.server;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerException;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerTerminatedException;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsOutput;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.lang.Math.min;
import static org.eclipse.che.plugin.nodejsdbg.server.NodeJsDebugProcess.SINGLE_LINE_CONSUMER;

/**
 * NodeJs process is being run.
 * Communication is performed through standard input/output streams.
 *
 * @author Anatoliy Bazko
 */
public abstract class NodeJsProcess {
    private static final Logger LOG          = LoggerFactory.getLogger(NodeJsProcess.class);
    private static final int    MAX_CAPACITY = 1000;
    private static final int    MAX_OUTPUT   = 4096;

    protected static final String NODEJS_COMMAND = detectNodeJsCommand();

    protected final Process                     process;
    protected final String                      outputSeparator;
    protected final BlockingQueue<NodeJsOutput> outputs;
    protected final OutputReader                outputReader;
    protected final BufferedWriter              processWriter;


    public NodeJsProcess(String outputSeparator, String... options) throws NodeJsDebuggerException {
        this.outputSeparator = outputSeparator;
        this.outputs = new ArrayBlockingQueue<>(MAX_CAPACITY);

        List<String> commands = new ArrayList<>(1 + options.length);
        commands.add(NODEJS_COMMAND);
        commands.addAll(Arrays.asList(options));

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new NodeJsDebuggerException("NodeJs process failed to start.", e);
        }

        processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        outputReader = new OutputReader("nodejs output reader");
        outputReader.setDaemon(true);
        outputReader.start();
    }

    /**
     * Stops process.
     */
    protected void stop() {
        outputReader.interrupt();
        outputs.clear();

        process.destroy();
        try {
            if (!process.waitFor(1, TimeUnit.MINUTES)) {
                LOG.error("NodeJs failed to stop");
            }
        } catch (InterruptedException ignored) {
        }

        try {
            processWriter.close();
        } catch (IOException e) {
            LOG.warn("Failed to close NodeJs process output stream", e);
        }
    }

    protected NodeJsOutput grabOutput() throws NodeJsDebuggerException {
        try {
            return outputs.take();
        } catch (InterruptedException e) {
            throw new NodeJsDebuggerException(e.getMessage(), e);
        }
    }

    protected NodeJsOutput sendCommand(String command) throws NodeJsDebuggerException {
        return sendCommand(command, SINGLE_LINE_CONSUMER);
    }

    protected synchronized NodeJsOutput sendCommand(String command,
                                                    Function<BlockingQueue<NodeJsOutput>, NodeJsOutput> outputConsumer) throws
                                                                                                                        NodeJsDebuggerException {
        doSendCommand(command);
        return outputConsumer.apply(outputs);
    }

    private void doSendCommand(String command) throws NodeJsDebuggerTerminatedException {
        LOG.debug("Execute: {}", command);

        outputs.clear();
        if (!process.isAlive()) {
            throw new NodeJsDebuggerTerminatedException("NodeJs process is terminated.");
        }

        try {
            processWriter.write(command);
            processWriter.newLine();
            processWriter.flush();
        } catch (IOException e) {
            LOG.error(String.format("Execution command <%s> failed", command), e);
        }
    }

    /**
     * Continuously reads process output and store in the {@code #outputs}.
     */
    private class OutputReader extends Thread {
        private final StringBuffer buf;

        public OutputReader(String name) {
            super(name);
            this.buf = new StringBuffer();
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    InputStream in = getInput();
                    if (in != null) {
                        String data = read(in);
                        if (!data.isEmpty()) {
                            buf.append(data);
                            if (buf.length() > MAX_OUTPUT) {
                                buf.delete(0, buf.length() - MAX_OUTPUT);
                            }

                            extractOutputs();
                            if (buf.length() == 0) {
                                synchronized (buf) {
                                    buf.notifyAll();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }

                if (!process.isAlive()) {
                    outputs.add(NodeJsOutput.of(buf.toString()));
                    break;
                }
            }

            LOG.debug(getName() + " has been stopped");
        }

        private InputStream getInput() throws IOException {
            return hasError() ? process.getErrorStream()
                              : (hasInput() ? (hasError() ? process.getErrorStream()
                                                          : process.getInputStream())
                                            : null);
        }

        private void extractOutputs() {
            int indexOf;
            while ((indexOf = buf.indexOf(outputSeparator)) >= 0) {
                NodeJsOutput nodeJsOutput = NodeJsOutput.of(buf.substring(0, indexOf));
                buf.delete(0, indexOf + outputSeparator.length());

                LOG.debug("{}{}", outputSeparator, nodeJsOutput.getOutput());
                outputs.add(nodeJsOutput);
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

    private static String detectNodeJsCommand() {
        ProcessBuilder builder =
                new ProcessBuilder("sh", "-c", "if command -v nodejs >/dev/null 2>&1; then echo -n 'nodejs'; else echo -n 'node'; fi");

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
