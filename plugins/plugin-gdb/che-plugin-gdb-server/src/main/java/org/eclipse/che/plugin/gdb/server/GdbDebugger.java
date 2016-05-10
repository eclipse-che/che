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
package org.eclipse.che.plugin.gdb.server;

import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.api.debugger.shared.model.Breakpoint;
import org.eclipse.che.api.debugger.shared.model.DebuggerInfo;
import org.eclipse.che.api.debugger.shared.model.Location;
import org.eclipse.che.api.debugger.shared.model.StackFrameDump;
import org.eclipse.che.api.debugger.shared.model.Value;
import org.eclipse.che.api.debugger.shared.model.Variable;
import org.eclipse.che.api.debugger.shared.model.VariablePath;
import org.eclipse.che.api.debugger.shared.model.action.ResumeAction;
import org.eclipse.che.api.debugger.shared.model.action.StartAction;
import org.eclipse.che.api.debugger.shared.model.action.StepIntoAction;
import org.eclipse.che.api.debugger.shared.model.action.StepOutAction;
import org.eclipse.che.api.debugger.shared.model.action.StepOverAction;
import org.eclipse.che.api.debugger.shared.model.impl.DebuggerInfoImpl;
import org.eclipse.che.api.debugger.shared.model.impl.StackFrameDumpImpl;
import org.eclipse.che.api.debugger.shared.model.impl.ValueImpl;
import org.eclipse.che.api.debugger.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debugger.shared.model.impl.VariablePathImpl;
import org.eclipse.che.api.debugger.shared.model.impl.event.BreakpointActivatedEventImpl;
import org.eclipse.che.api.debugger.shared.model.impl.event.DisconnectEventImpl;
import org.eclipse.che.api.debugger.shared.model.impl.event.SuspendEventImpl;
import org.eclipse.che.plugin.gdb.server.parser.GdbContinue;
import org.eclipse.che.plugin.gdb.server.parser.GdbDirectory;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoBreak;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoLine;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoProgram;
import org.eclipse.che.plugin.gdb.server.parser.GdbParseException;
import org.eclipse.che.plugin.gdb.server.parser.GdbPrint;
import org.eclipse.che.plugin.gdb.server.parser.GdbRun;
import org.eclipse.che.plugin.gdb.server.parser.GdbVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * Connects to GDB.
 *
 * @author Anatoliy Bazko
 */
public class GdbDebugger implements Debugger {
    private static final Logger LOG = LoggerFactory.getLogger(GdbDebugger.class);

    private final String host;
    private final int    port;
    private final String name;
    private final String version;
    private final String file;

    private final Gdb              gdb;
    private final DebuggerCallback debuggerCallback;

    GdbDebugger(String host,
                int port,
                String name,
                String version,
                String file,
                Gdb gdb,
                DebuggerCallback debuggerCallback) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.version = version;
        this.file = file;
        this.gdb = gdb;
        this.debuggerCallback = debuggerCallback;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getFile() {
        return file;
    }

    public static GdbDebugger newInstance(String host,
                                          int port,
                                          String file,
                                          String srcDirectory,
                                          DebuggerCallback debuggerCallback) throws DebuggerException {
        Gdb gdb;
        try {
            gdb = Gdb.start();
            GdbDirectory directory = gdb.directory(srcDirectory);
            LOG.debug("Source directories: " + directory.getDirectories());

            gdb.file(file);
            if (port > 0) {
                gdb.targetRemote(host, port);
            }
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Can't initialize GDB", e);
        }

        GdbVersion gdbVersion = gdb.getGdbVersion();
        return new GdbDebugger(host,
                               port,
                               gdbVersion.getVersion(),
                               gdbVersion.getName(),
                               file,
                               gdb,
                               debuggerCallback);
    }

    @Override
    public DebuggerInfo getInfo() throws DebuggerException {
        return new DebuggerInfoImpl(host, port, name, version, 0, file);
    }

    @Override
    public void disconnect() throws DebuggerException {
        debuggerCallback.onEvent(new DisconnectEventImpl());

        try {
            gdb.quit();
        } catch (IOException e) {
            throw new DebuggerException("quit failed", e);
        }
    }

    @Override
    public void addBreakpoint(Breakpoint breakpoint) throws DebuggerException {
        try {
            Location location = breakpoint.getLocation();
            if (location.getTarget() == null) {
                gdb.breakpoint(location.getLineNumber());
            } else {
                gdb.breakpoint(location.getTarget(), location.getLineNumber());
            }

            debuggerCallback.onEvent(new BreakpointActivatedEventImpl(breakpoint));
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Can't add breakpoint: " + breakpoint, e);
        }
    }

    @Override
    public void deleteBreakpoint(Location location) throws DebuggerException {
        try {
            if (location.getTarget() == null) {
                gdb.clear(location.getLineNumber());
            } else {
                gdb.clear(location.getTarget(), location.getLineNumber());
            }
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Can't delete breakpoint: " + location, e);
        }
    }

    @Override
    public void deleteAllBreakpoints() throws DebuggerException {
        try {
            gdb.delete();
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Can't delete all breakpoints", e);
        }
    }

    @Override
    public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
        try {
            GdbInfoBreak gdbInfoBreak = gdb.infoBreak();
            return gdbInfoBreak.getBreakpoints();
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Can't get all breakpoints", e);
        }
    }

    @Override
    public void start(StartAction action) throws DebuggerException {
        try {
            Breakpoint breakpoint;

            if (isRemoteConnection()) {
                GdbContinue gdbContinue = gdb.cont();
                breakpoint = gdbContinue.getBreakpoint();
            } else {
                GdbRun gdbRun = gdb.run();
                breakpoint = gdbRun.getBreakpoint();
            }

            if (breakpoint != null) {
                debuggerCallback.onEvent(new SuspendEventImpl(breakpoint.getLocation()));
            } else {
                GdbInfoProgram gdbInfoProgram = gdb.infoProgram();
                if (gdbInfoProgram.getStoppedAddress() == null) {
                    disconnect();
                }
            }
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Error during running.", e);
        }
    }

    private boolean isRemoteConnection() {
        return getPort() > 0;
    }

    @Override
    public void stepOver(StepOverAction action) throws DebuggerException {
        try {
            GdbInfoLine gdbInfoLine = gdb.next();
            if (gdbInfoLine == null) {
                disconnect();
                return;
            }

            debuggerCallback.onEvent(new SuspendEventImpl(gdbInfoLine.getLocation()));
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Step into error.", e);
        }
    }

    @Override
    public void stepInto(StepIntoAction action) throws DebuggerException {
        try {
            GdbInfoLine gdbInfoLine = gdb.step();
            if (gdbInfoLine == null) {
                disconnect();
                return;
            }

            debuggerCallback.onEvent(new SuspendEventImpl(gdbInfoLine.getLocation()));
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Step into error.", e);
        }
    }

    @Override
    public void stepOut(StepOutAction action) throws DebuggerException {
        try {
            GdbInfoLine gdbInfoLine = gdb.finish();
            if (gdbInfoLine == null) {
                disconnect();
                return;
            }

            debuggerCallback.onEvent(new SuspendEventImpl(gdbInfoLine.getLocation()));
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Step out error.", e);
        }
    }

    @Override
    public void resume(ResumeAction action) throws DebuggerException {
        try {
            GdbContinue gdbContinue = gdb.cont();
            Breakpoint breakpoint = gdbContinue.getBreakpoint();

            if (breakpoint != null) {
                debuggerCallback.onEvent(new SuspendEventImpl(breakpoint.getLocation()));
            } else {
                GdbInfoProgram gdbInfoProgram = gdb.infoProgram();
                if (gdbInfoProgram.getStoppedAddress() == null) {
                    disconnect();
                }
            }
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Resume error.", e);
        }
    }

    @Override
    public void setValue(Variable variable) throws DebuggerException {
        try {
            List<String> path = variable.getVariablePath().getPath();
            if (path.isEmpty()) {
                throw new DebuggerException("Variable path is empty");
            }
            gdb.setVar(path.get(0), variable.getValue());
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Can't set value for " + variable.getName(), e);
        }
    }

    @Override
    public Value getValue(VariablePath variablePath) throws DebuggerException {
        try {
            List<String> path = variablePath.getPath();
            if (path.isEmpty()) {
                throw new DebuggerException("Variable path is empty");
            }

            GdbPrint gdbPrint = gdb.print(path.get(0));
            return new ValueImpl(Collections.emptyList(), gdbPrint.getValue());
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Can't get value for " + variablePath, e);
        }
    }

    @Override
    public String evaluate(String expression) throws DebuggerException {
        try {
            GdbPrint gdbPrint = gdb.print(expression);
            return gdbPrint.getValue();
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Can't evaluate '" + expression + "'", e);
        }
    }

    /**
     * Dump frame.
     */
    @Override
    public StackFrameDump dumpStackFrame() throws DebuggerException {

        try {
            Map<String, String> locals = gdb.infoLocals().getVariables();
            locals.putAll(gdb.infoArgs().getVariables());

            List<Variable> variables = new ArrayList<>(locals.size());
            for (Map.Entry<String, String> e : locals.entrySet()) {
                String varName = e.getKey();
                String varValue = e.getValue();
                String varType;
                try {
                    varType = gdb.ptype(varName).getType();
                } catch (GdbParseException pe) {
                    LOG.warn(pe.getMessage(), pe);
                    varType = "";
                }

                VariablePath variablePath = new VariablePathImpl(singletonList(varName));
                VariableImpl variable = new VariableImpl(varType, varName, varValue, true, variablePath, Collections.emptyList(), true);
                variables.add(variable);
            }

            return new StackFrameDumpImpl(Collections.emptyList(), variables);
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new DebuggerException("Can't dump stack frame", e);
        }
    }
}
