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

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerException;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsBackTrace;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsBreakpoints;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsExec;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsOutput;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsScripts;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * NodeJs debug process.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsDebugProcess extends NodeJsProcess {
    private NodeJsDebugProcess(String... options) throws NodeJsDebuggerException {
        super("debug> ", options);
    }

    public static NodeJsDebugProcess start(int pid) throws NodeJsDebuggerException {
        return new NodeJsDebugProcess("debug", "-p", String.valueOf(pid));
    }

    public static NodeJsDebugProcess start(URI uri) throws NodeJsDebuggerException {
        return new NodeJsDebugProcess("debug", uri.toString());
    }

    public static NodeJsDebugProcess start(String file) throws NodeJsDebuggerException {
        NodeJsDebugProcess nodeJsDebugProcess = new NodeJsDebugProcess("debug", "--debug-brk", file);
        nodeJsDebugProcess.run();
        return nodeJsDebugProcess;
    }

    /**
     * Execute {@code quit} command.
     */
    public void quit() throws NodeJsDebuggerException {
        stop();
    }

    /**
     * Execute {@code run} command.
     */
    public void run() throws NodeJsDebuggerException {
        sendCommand("run", RUN_COMMAND_LINE_CONSUMER);
    }

    /**
     * Execute {@code exec} command to set a new value for the giving variable.
     */
    public void setVar(String varName, String value) throws NodeJsDebuggerException {
        String command = "exec " + varName + "=" + value;
        sendCommand(command);
    }

    /**
     * Execute {@code exec} command to get value for the giving variable.
     */
    public NodeJsExec getVar(String varName) throws NodeJsDebuggerException {
        NodeJsOutput nodeJsOutput = sendCommand("exec " + varName);
        return NodeJsExec.parse(nodeJsOutput);
    }

    /**
     * Execute {@code exec} command to evaluate expression.
     */
    public NodeJsExec evaluate(String expression) throws NodeJsDebuggerException {
        NodeJsOutput nodeJsOutput = sendCommand("exec " + expression);
        return NodeJsExec.parse(nodeJsOutput);
    }

    /**
     * Execute {@code script} command.
     */
    public NodeJsScripts findLoadedScripts() throws NodeJsDebuggerException {
        NodeJsOutput nodeJsOutput = sendCommand("scripts", DOUBLE_LINE_CONSUMER);
        return NodeJsScripts.parse(nodeJsOutput);
    }

    /**
     * Execute {@code breakpoints} command.
     */
    public NodeJsBreakpoints getBreakpoints() throws NodeJsDebuggerException {
        NodeJsOutput breakpointsOutput = sendCommand("breakpoints", DOUBLE_LINE_CONSUMER);
        NodeJsOutput scriptsOutput = sendCommand("scripts", DOUBLE_LINE_CONSUMER);

        return NodeJsBreakpoints.parse(scriptsOutput, breakpointsOutput);
    }

    /**
     * Execute {@code sb} command.
     */
    public void setBreakpoint(@Nullable String script, int lineNumber) throws NodeJsDebuggerException {
        String command = script != null ? String.format("sb('%s', %d)", script, lineNumber)
                                        : String.format("sb(%d)", lineNumber);
        sendCommand(command);
    }

    /**
     * Execute {@code back trace} command.
     */
    public NodeJsBackTrace backtrace() throws NodeJsDebuggerException {
        NodeJsOutput nodeJsOutput = sendCommand("bt");
        return NodeJsBackTrace.parse(nodeJsOutput);
    }

    /**
     * Execute {@code next} command.
     */
    public NodeJsBackTrace next() throws NodeJsDebuggerException {
        sendCommand("next", STEP_COMMAND_LINE_CONSUMER);
        return backtrace();
    }

    /**
     * Execute {@code cont} command.
     */
    public NodeJsBackTrace cont() throws NodeJsDebuggerException {
        sendCommand("cont", STEP_COMMAND_LINE_CONSUMER);
        return backtrace();
    }

    /**
     * Execute {@code step in} command.
     */
    public NodeJsBackTrace stepIn() throws NodeJsDebuggerException {
        sendCommand("step", STEP_COMMAND_LINE_CONSUMER);
        return backtrace();
    }

    /**
     * Execute {@code step out} command.
     */
    public NodeJsBackTrace stepOut() throws NodeJsDebuggerException {
        sendCommand("out", STEP_COMMAND_LINE_CONSUMER);
        return backtrace();
    }

    /**
     * Returns NodeJs version.
     */
    public String getVersion() throws NodeJsDebuggerException {
        return sendCommand("process.version").getOutput();
    }

    /**
     * Returns NodeJs title.
     */
    public String getName() throws NodeJsDebuggerException {
        return sendCommand("process.title").getOutput();
    }

    /**
     * Execute {@code cb} command.
     */
    public void clearBreakpoint(String script, int lineNumber) throws NodeJsDebuggerException {
        String command = script != null ? String.format("cb('%s', %d)", script, lineNumber)
                                        : String.format("cb(%d)", lineNumber);
        sendCommand(command);
    }

    static Function<BlockingQueue<NodeJsOutput>, NodeJsOutput> SINGLE_LINE_CONSUMER = outputs -> {
        try {
            return outputs.take();
        } catch (InterruptedException e) {
            return NodeJsOutput.EMPTY;
        }
    };

    static Function<BlockingQueue<NodeJsOutput>, NodeJsOutput> DOUBLE_LINE_CONSUMER = outputs -> {
        try {
            NodeJsOutput nodeJsOutput1 = outputs.take();
            NodeJsOutput nodeJsOutput2 = outputs.take();
            return nodeJsOutput1.isEmpty() ? nodeJsOutput2 : nodeJsOutput1;
        } catch (InterruptedException e) {
            return NodeJsOutput.EMPTY;
        }
    };

    static Function<BlockingQueue<NodeJsOutput>, NodeJsOutput> STEP_COMMAND_LINE_CONSUMER = outputs -> {
        try {
            while (!outputs.take().getOutput().contains("break in")) {
            }
        } catch (InterruptedException ignored) {
        }

        return NodeJsOutput.EMPTY;
    };

    static Function<BlockingQueue<NodeJsOutput>, NodeJsOutput> RUN_COMMAND_LINE_CONSUMER = outputs -> {
        try {
            for (; ; ) {
                String output = outputs.take().getOutput();
                if (output.contains("break in") || output.contains("App is already running")) {
                    break;
                }
            }
        } catch (InterruptedException ignored) {
        }

        return NodeJsOutput.EMPTY;
    };

    static Function<BlockingQueue<NodeJsOutput>, NodeJsOutput> QUIT_COMMAND_LINE_CONSUMER = outputs -> {
        try {
            outputs.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }

        return NodeJsOutput.EMPTY;
    };
}
