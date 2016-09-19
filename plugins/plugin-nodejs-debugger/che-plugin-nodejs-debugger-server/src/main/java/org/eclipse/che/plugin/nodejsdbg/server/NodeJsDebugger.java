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

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.action.ResumeAction;
import org.eclipse.che.api.debug.shared.model.action.StartAction;
import org.eclipse.che.api.debug.shared.model.action.StepIntoAction;
import org.eclipse.che.api.debug.shared.model.action.StepOutAction;
import org.eclipse.che.api.debug.shared.model.action.StepOverAction;
import org.eclipse.che.api.debug.shared.model.impl.DebuggerInfoImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.StackFrameDumpImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.BreakpointActivatedEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.DisconnectEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.SuspendEventImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerException;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerTerminatedException;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsBackTrace;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsBreakpoints;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsExec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Server side NodeJs debugger.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsDebugger implements Debugger {
    private static final Logger LOG = LoggerFactory.getLogger(NodeJsDebugger.class);

    private final Integer pid;
    private final URI     uri;
    private final String  name;
    private final String  version;
    private final String  file;

    private final NodeJsDebugProcess nodeJsDebugProcess;
    private final DebuggerCallback   debuggerCallback;

    NodeJsDebugger(@Nullable Integer pid,
                   @Nullable URI uri,
                   @Nullable String file,
                   String name,
                   String version,
                   NodeJsDebugProcess nodeJsDebugProcess,
                   DebuggerCallback debuggerCallback) {
        this.pid = pid;
        this.uri = uri;
        this.file = file;
        this.name = name;
        this.version = version;
        this.nodeJsDebugProcess = nodeJsDebugProcess;
        this.debuggerCallback = debuggerCallback;
    }

    public static NodeJsDebugger newInstance(@Nullable Integer pid,
                                             @Nullable URI uri,
                                             @Nullable String file,
                                             DebuggerCallback debuggerCallback) throws DebuggerException {
        NodeJsVersionProcess nodeJsVersionProcess = new NodeJsVersionProcess();
        nodeJsVersionProcess.stop();

        NodeJsDebugProcess nodeJsDebugProcess;
        if (pid != null) {
            nodeJsDebugProcess = NodeJsDebugProcess.start(pid);
        } else if (uri != null) {
            nodeJsDebugProcess = NodeJsDebugProcess.start(uri);
        } else {
            nodeJsDebugProcess = NodeJsDebugProcess.start(file);
        }

        return new NodeJsDebugger(pid,
                                  uri,
                                  file,
                                  nodeJsVersionProcess.getName(),
                                  nodeJsVersionProcess.getVersion(),
                                  nodeJsDebugProcess,
                                  debuggerCallback);
    }

    @Override
    public DebuggerInfo getInfo() throws DebuggerException {
        return new DebuggerInfoImpl(uri == null ? "" : uri.getHost(),
                                    uri == null ? -1 : uri.getPort(),
                                    name,
                                    version,
                                    pid,
                                    file);
    }

    @Override
    public void disconnect() {
        debuggerCallback.onEvent(new DisconnectEventImpl());

        try {
            nodeJsDebugProcess.quit();
        } catch (NodeJsDebuggerException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void addBreakpoint(Breakpoint breakpoint) throws DebuggerException {
        try {
            Location location = breakpoint.getLocation();
            nodeJsDebugProcess.setBreakpoint(location.getTarget(), location.getLineNumber());

            debuggerCallback.onEvent(new BreakpointActivatedEventImpl(breakpoint));
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Can't add breakpoint: " + breakpoint + ". " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteBreakpoint(Location location) throws DebuggerException {
        try {
            nodeJsDebugProcess.clearBreakpoint(location.getTarget(), location.getLineNumber());
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Can't delete breakpoint: " + location + ". " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAllBreakpoints() throws DebuggerException {
        try {
            NodeJsBreakpoints nodeJsBreakpoints = nodeJsDebugProcess.getBreakpoints();
            for (Breakpoint breakpoint : nodeJsBreakpoints.getBreakpoints()) {
                Location location = breakpoint.getLocation();
                try {
                    nodeJsDebugProcess.clearBreakpoint(location.getTarget(), location.getLineNumber());
                } catch (NodeJsDebuggerException e) {
                    LOG.error("Can't delete breakpoint: {}", location, e);
                }
            }
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Can't delete all breakpoints. " + e.getMessage(), e);
        }
    }

    @Override
    public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
        try {
            NodeJsBreakpoints nodeJsBreakpoints = nodeJsDebugProcess.getBreakpoints();
            return nodeJsBreakpoints.getBreakpoints();
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Can't get all breakpoints. " + e.getMessage(), e);
        }
    }

    @Override
    public void start(StartAction action) throws DebuggerException {
        try {
            for (Breakpoint breakpoint : action.getBreakpoints()) {
                Location location = breakpoint.getLocation();
                try {
                    nodeJsDebugProcess.setBreakpoint(location.getTarget(), location.getLineNumber());
                } catch (NodeJsDebuggerException e) {
                    LOG.warn("Breakpoint skipped {}", location);
                }
            }

            NodeJsBackTrace nodeJsBackTrace = nodeJsDebugProcess.backtrace();
            debuggerCallback.onEvent(new SuspendEventImpl(nodeJsBackTrace.getLocation()));
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Error during running. " + e.getMessage(), e);
        }
    }

    @Override
    public void stepOver(StepOverAction action) throws DebuggerException {
        try {
            NodeJsBackTrace nodeJsBackTrace = nodeJsDebugProcess.next();
            debuggerCallback.onEvent(new SuspendEventImpl(nodeJsBackTrace.getLocation()));
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Step over error. " + e.getMessage(), e);
        }
    }

    @Override
    public void stepInto(StepIntoAction action) throws DebuggerException {
        try {
            NodeJsBackTrace nodeJsBackTrace = nodeJsDebugProcess.stepIn();
            debuggerCallback.onEvent(new SuspendEventImpl(nodeJsBackTrace.getLocation()));
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Step into error. " + e.getMessage(), e);
        }
    }

    @Override
    public void stepOut(StepOutAction action) throws DebuggerException {
        try {
            NodeJsBackTrace nodeJsBackTrace = nodeJsDebugProcess.stepOut();
            debuggerCallback.onEvent(new SuspendEventImpl(nodeJsBackTrace.getLocation()));
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Step out error. " + e.getMessage(), e);
        }
    }

    @Override
    public void resume(ResumeAction action) throws DebuggerException {
        try {
            NodeJsBackTrace nodeJsBackTrace = nodeJsDebugProcess.cont();
            debuggerCallback.onEvent(new SuspendEventImpl(nodeJsBackTrace.getLocation()));
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Resume error. " + e.getMessage(), e);
        }
    }

    @Override
    public void setValue(Variable variable) throws DebuggerException {
        try {
            List<String> path = variable.getVariablePath().getPath();
            if (path.isEmpty()) {
                throw new DebuggerException("Variable path is empty");
            }
            nodeJsDebugProcess.setVar(path.get(0), variable.getValue());
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Can't set value for " + variable.getName() + ". " + e.getMessage(), e);
        }
    }

    @Override
    public SimpleValue getValue(VariablePath variablePath) throws DebuggerException {
        try {
            List<String> path = variablePath.getPath();
            if (path.isEmpty()) {
                throw new DebuggerException("Variable path is empty");
            }

            NodeJsExec nodeJsExec = nodeJsDebugProcess.getVar(path.get(0));
            return new SimpleValueImpl(Collections.emptyList(), nodeJsExec.getValue());
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Can't get value for " + variablePath + ". " + e.getMessage(), e);
        }
    }

    @Override
    public String evaluate(String expression) throws DebuggerException {
        try {
            NodeJsExec nodeJsExec = nodeJsDebugProcess.evaluate(expression);
            return nodeJsExec.getValue();
        } catch (NodeJsDebuggerTerminatedException e) {
            disconnect();
            throw e;
        } catch (NodeJsDebuggerException e) {
            throw new DebuggerException("Can't evaluate '" + expression + "'. " + e.getMessage(), e);
        }
    }

    @Override
    public StackFrameDump dumpStackFrame() throws DebuggerException {
        return new StackFrameDumpImpl(Collections.emptyList(), Collections.emptyList());
    }
}
