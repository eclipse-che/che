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
package org.eclipse.che.ide.gdb.server;

import org.eclipse.che.ide.ext.debugger.shared.Breakpoint;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointActivatedEvent;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointEvent;
import org.eclipse.che.ide.ext.debugger.shared.BreakpointList;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerEvent;
import org.eclipse.che.ide.ext.debugger.shared.DebuggerEventList;
import org.eclipse.che.ide.ext.debugger.shared.DisconnectEvent;
import org.eclipse.che.ide.ext.debugger.shared.Location;
import org.eclipse.che.ide.ext.debugger.shared.StackFrameDump;
import org.eclipse.che.ide.ext.debugger.shared.StepEvent;
import org.eclipse.che.ide.ext.debugger.shared.Value;
import org.eclipse.che.ide.ext.debugger.shared.Variable;
import org.eclipse.che.ide.ext.debugger.shared.VariablePath;
import org.eclipse.che.ide.gdb.server.parser.GdbContinue;
import org.eclipse.che.ide.gdb.server.parser.GdbDirectory;
import org.eclipse.che.ide.gdb.server.parser.GdbInfoBreak;
import org.eclipse.che.ide.gdb.server.parser.GdbInfoLine;
import org.eclipse.che.ide.gdb.server.parser.GdbInfoProgram;
import org.eclipse.che.ide.gdb.server.parser.GdbParseException;
import org.eclipse.che.ide.gdb.server.parser.GdbPrint;
import org.eclipse.che.ide.gdb.server.parser.GdbRun;
import org.eclipse.che.ide.gdb.server.parser.GdbVersion;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.eclipse.che.commons.json.JsonHelper.toJson;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Connects to GDB.
 * All methods of this class throws {@link GdbDebuggerException}.
 *
 * @author Anatoliy Bazko
 */
public class GdbDebugger {
    private static final Logger                             LOG       = LoggerFactory.getLogger(GdbDebugger.class);
    private static final AtomicLong                         counter   = new AtomicLong(1);
    private static final ConcurrentMap<String, GdbDebugger> instances = new ConcurrentHashMap<>();

    private static final String EVENTS_CHANNEL = "gdbdebugger:events:";

    private final String host;
    private final int    port;
    private final String name;
    private final String version;
    private final String id;
    private final String file;

    private final Gdb gdb;

    GdbDebugger(String host, int port, String name, String version, String id, String file, Gdb gdb) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.version = version;
        this.id = id;
        this.file = file;
        this.gdb = gdb;

        instances.put(id, this);
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

    public String getId() {
        return id;
    }

    public String getFile() {
        return file;
    }

    /**
     * Factory.
     */
    public static GdbDebugger newInstance(String host, int port, String file, String srcDirectory) throws GdbDebuggerException {
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
            throw new GdbDebuggerException("Can't initialize GDB", e);
        }

        GdbVersion gdbVersion = gdb.getGdbVersion();
        long instanceId = counter.incrementAndGet();

        return new GdbDebugger(host,
                               port,
                               gdbVersion.getVersion(),
                               gdbVersion.getName(),
                               String.valueOf(instanceId),
                               file,
                               gdb);
    }

    public static GdbDebugger getInstance(String id) throws GdbDebuggerException {
        GdbDebugger gdbDebugger = instances.get(id);
        if (gdbDebugger == null) {
            throw new GdbDebuggerNotFoundException("Instance " + id + " not found");
        }

        return gdbDebugger;
    }

    /**
     * Disconnects from GDB.
     *
     * @throws GdbDebuggerException
     */
    public void disconnect() throws GdbDebuggerException {
        instances.remove(id);
        DisconnectEvent disconnectEvent = newDto(DisconnectEvent.class);
        disconnectEvent.setType(DebuggerEvent.DISCONNECTED);

        DebuggerEventList debuggerEventList = newDto(DebuggerEventList.class);
        debuggerEventList.withEvents(Collections.singletonList(disconnectEvent));
        publishWebSocketMessage(debuggerEventList, EVENTS_CHANNEL + id);

        try {
            gdb.quit();
        } catch (IOException e) {
            throw new GdbDebuggerException("quit failed", e);
        }
    }

    /**
     * Adds breakpoint to the server.
     */
    public void addBreakpoint(Breakpoint breakpoint) throws GdbDebuggerException {
        try {
            Location location = breakpoint.getLocation();
            if (location.getClassName() == null) {
                gdb.breakpoint(location.getLineNumber());
            } else {
                gdb.breakpoint(location.getClassName(), location.getLineNumber());
            }

            BreakpointActivatedEvent breakpointActivatedEvent = newDto(BreakpointActivatedEvent.class);
            breakpointActivatedEvent.setType(DebuggerEvent.BREAKPOINT_ACTIVATED);
            breakpointActivatedEvent.setBreakpoint(breakpoint);

            DebuggerEventList debuggerEventList = newDto(DebuggerEventList.class);
            debuggerEventList.withEvents(Collections.singletonList(breakpointActivatedEvent));
            publishWebSocketMessage(debuggerEventList, EVENTS_CHANNEL + id);
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Can't add breakpoint: " + breakpoint, e);
        }
    }

    /**
     * Deletes breakpoint from the server.
     */
    public void deleteBreakpoint(Breakpoint breakpoint) throws GdbDebuggerException {
        try {
            Location location = breakpoint.getLocation();
            if (location.getClassName() == null) {
                gdb.clear(location.getLineNumber());
            } else {
                gdb.clear(location.getClassName(), location.getLineNumber());
            }
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Can't delete breakpoint: " + breakpoint, e);
        }
    }

    /**
     * Deletes all breakpoints.
     */
    public void deleteAllBreakPoints() throws GdbDebuggerException {
        try {
            gdb.delete();
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Can't delete all breakpoints", e);
        }
    }

    /**
     * Gets list of all breakpoints.
     */
    public BreakpointList getBreakpoints() throws GdbDebuggerException {
        try {
            GdbInfoBreak gdbInfoBreak = gdb.infoBreak();

            BreakpointList breakpointList = newDto(BreakpointList.class);
            return breakpointList.withBreakpoints(gdbInfoBreak.getBreakpoints());
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Can't get all breakpoints", e);
        }
    }

    /**
     * Starts debugger.
     */
    public void start() throws GdbDebuggerException {
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
                BreakpointEvent breakpointEvent = newDto(BreakpointEvent.class);
                breakpointEvent.setBreakpoint(breakpoint);
                breakpointEvent.setType(DebuggerEvent.BREAKPOINT);

                DebuggerEventList debuggerEventList = newDto(DebuggerEventList.class);
                debuggerEventList.withEvents(Collections.singletonList(breakpointEvent));
                publishWebSocketMessage(debuggerEventList, EVENTS_CHANNEL + id);
            } else {
                GdbInfoProgram gdbInfoProgram = gdb.infoProgram();
                if (gdbInfoProgram.getStoppedAddress() == null) {
                    disconnect();
                }
            }
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Error during running.", e);
        }
    }

    private boolean isRemoteConnection() {
        return getPort() > 0;
    }

    /**
     * Does step over.
     */
    public void stepOver() throws GdbDebuggerException {
        try {
            GdbInfoLine gdbInfoLine = gdb.next();
            if (gdbInfoLine == null) {
                disconnect();
                return;
            }

            StepEvent stepEvent = newDto(StepEvent.class);
            stepEvent.setType(DebuggerEvent.STEP);
            stepEvent.setLocation(gdbInfoLine.getLocation());

            DebuggerEventList debuggerEventList = newDto(DebuggerEventList.class);
            debuggerEventList.withEvents(Collections.singletonList(stepEvent));
            publishWebSocketMessage(debuggerEventList, EVENTS_CHANNEL + id);
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Step into error.", e);
        }
    }

    /**
     * Does step into.
     */
    public void stepInto() throws GdbDebuggerException {
        try {
            GdbInfoLine gdbInfoLine = gdb.step();
            if (gdbInfoLine == null) {
                disconnect();
                return;
            }

            StepEvent stepEvent = newDto(StepEvent.class);
            stepEvent.setType(DebuggerEvent.STEP);
            stepEvent.setLocation(gdbInfoLine.getLocation());

            DebuggerEventList debuggerEventList = newDto(DebuggerEventList.class);
            debuggerEventList.withEvents(Collections.singletonList(stepEvent));
            publishWebSocketMessage(debuggerEventList, EVENTS_CHANNEL + id);
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Step into error.", e);
        }
    }

    /**
     * Does stop out.
     */
    public void stepOut() throws GdbDebuggerException {
        try {
            GdbInfoLine gdbInfoLine = gdb.finish();
            if (gdbInfoLine == null) {
                disconnect();
                return;
            }

            StepEvent stepEvent = newDto(StepEvent.class);
            stepEvent.setType(DebuggerEvent.STEP);
            stepEvent.setLocation(gdbInfoLine.getLocation());

            DebuggerEventList debuggerEventList = newDto(DebuggerEventList.class);
            debuggerEventList.withEvents(Collections.singletonList(stepEvent));
            publishWebSocketMessage(debuggerEventList, EVENTS_CHANNEL + id);
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Step out error.", e);
        }
    }

    /**
     * Does resume.
     */
    public void resume() throws GdbDebuggerException {
        try {
            GdbContinue gdbContinue = gdb.cont();
            Breakpoint breakpoint = gdbContinue.getBreakpoint();

            if (breakpoint != null) {
                BreakpointEvent breakpointEvent = newDto(BreakpointEvent.class);
                breakpointEvent.setBreakpoint(breakpoint);
                breakpointEvent.setType(DebuggerEvent.BREAKPOINT);

                DebuggerEventList debuggerEventList = newDto(DebuggerEventList.class);
                debuggerEventList.withEvents(Collections.singletonList(breakpointEvent));
                publishWebSocketMessage(debuggerEventList, EVENTS_CHANNEL + id);
            } else {
                GdbInfoProgram gdbInfoProgram = gdb.infoProgram();
                if (gdbInfoProgram.getStoppedAddress() == null) {
                    disconnect();
                }
            }
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Resume error.", e);
        }
    }

    /**
     * Sets new variable value.
     */
    public void setValue(String variable, String value) throws GdbDebuggerException {
        try {
            gdb.setVar(variable, value);
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Can't set '" + variable + "=" + value + "'", e);
        }
    }

    /**
     * Gets variable value.
     */
    public Value getValue(String variable) throws GdbDebuggerException {
        try {
            GdbPrint gdbPrint = gdb.print(variable);

            Value value = newDto(Value.class);
            value.setValue(gdbPrint.getValue());
            return value;
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Can't get '" + variable + "'", e);
        }
    }

    /**
     * Evaluate expression.
     */
    public String expression(String expression) throws GdbDebuggerException {
        try {
            GdbPrint gdbPrint = gdb.print(expression);
            return gdbPrint.getValue();
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Can't evaluate '" + expression + "'", e);
        }
    }

    /**
     * Dump frame.
     */
    public StackFrameDump dumpStackFrame() throws GdbDebuggerException {
        StackFrameDump gdbStackFrameDump = newDto(StackFrameDump.class);

        try {
            Map<String, String> locals = gdb.infoLocals().getVariables();
            locals.putAll(gdb.infoArgs().getVariables());

            List<Variable> variables = new ArrayList<>();
            for (Map.Entry<String, String> e : locals.entrySet()) {
                String varName = e.getKey();
                String varValue = e.getValue();
                String varType = gdb.ptype(varName).getType();

                VariablePath variablePath = newDto(VariablePath.class);
                variablePath.setPath(Collections.singletonList(varName));

                Variable variable = newDto(Variable.class);
                variable.setName(varName);
                variable.setValue(varValue);
                variable.setType(varType);
                variable.setVariablePath(variablePath);
                variable.setExistInformation(true);
                variable.setPrimitive(true);

                variables.add(variable);
            }

            gdbStackFrameDump.setFields(Collections.emptyList());
            gdbStackFrameDump.setLocalVariables(variables);
        } catch (IOException | GdbParseException | InterruptedException e) {
            throw new GdbDebuggerException("Can't dump stack frame", e);
        }

        return gdbStackFrameDump;
    }

    /**
     * Publishes the message over WebSocket connection.
     *
     * @param eventList
     *         the data to be sent to the client
     * @param channelID
     *         channel identifier
     */
    protected void publishWebSocketMessage(DebuggerEventList eventList, String channelID) {
        ChannelBroadcastMessage message = new ChannelBroadcastMessage();
        message.setChannel(channelID);
        message.setType(ChannelBroadcastMessage.Type.NONE);
        message.setBody(toJson(eventList));

        try {
            WSConnectionContext.sendMessage(message);
        } catch (Exception e) {
            LOG.error("Failed to send message over WebSocket.", e);
        }
    }
}
