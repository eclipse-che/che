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
        try {
            sendCommand("quit");
        } finally {
            stop();
        }
    }

    /**
     * Execute {@code run} command.
     */
    public void run() throws NodeJsDebuggerException {
        NodeJsOutput nodeJsOutput = sendCommand("run", false);
        while (!nodeJsOutput.getOutput().contains("break in")) {
            nodeJsOutput = grabOutput();
        }
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
        String command = "exec " + varName;
        NodeJsOutput nodeJsOutput = sendCommand(command);
        return NodeJsExec.parse(nodeJsOutput);
    }

    /**
     * Execute {@code exec} command to evaluate expression.
     */
    public NodeJsExec evaluate(String expression) throws NodeJsDebuggerException {
        String command = "exec " + expression;
        NodeJsOutput nodeJsOutput = sendCommand(command);
        return NodeJsExec.parse(nodeJsOutput);
    }

    /**
     * Execute {@code script} command.
     */
    public NodeJsScripts findLoadedScripts() throws NodeJsDebuggerException {
        NodeJsOutput nodeJsOutput = sendCommand("scripts");
        return NodeJsScripts.parse(nodeJsOutput);
    }

    /**
     * Execute {@code breakpoints} command.
     */
    public NodeJsBreakpoints getBreakpoints() throws NodeJsDebuggerException {
        NodeJsOutput breakpointsOutput = sendCommand("breakpoints");
        NodeJsOutput scriptsOutput = sendCommand("scripts");

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
        sendCommand("next");
        NodeJsOutput nodeJsOutput = sendCommand("bt");
        return NodeJsBackTrace.parse(nodeJsOutput);
    }

    /**
     * Execute {@code cont} command.
     */
    public NodeJsBackTrace cont() throws NodeJsDebuggerException {
        sendCommand("cont");
        NodeJsOutput nodeJsOutput = sendCommand("bt");
        return NodeJsBackTrace.parse(nodeJsOutput);
    }

    /**
     * Execute {@code step in} command.
     */
    public NodeJsBackTrace stepIn() throws NodeJsDebuggerException {
        sendCommand("step");
        NodeJsOutput nodeJsOutput = sendCommand("bt");
        return NodeJsBackTrace.parse(nodeJsOutput);
    }

    /**
     * Execute {@code step out} command.
     */
    public NodeJsBackTrace stepOut() throws NodeJsDebuggerException {
        sendCommand("out");
        NodeJsOutput nodeJsOutput = sendCommand("bt");
        return NodeJsBackTrace.parse(nodeJsOutput);
    }

    /**
     * Execute {@code cb} command.
     */
    public void clearBreakpoint(String script, int lineNumber) throws NodeJsDebuggerException {
        String command = script != null ? String.format("cb('%s', %d)", script, lineNumber)
                                        : String.format("cb(%d)", lineNumber);
        sendCommand(command);
    }
}
