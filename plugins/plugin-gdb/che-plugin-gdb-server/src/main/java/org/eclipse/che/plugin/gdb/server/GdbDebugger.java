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

import static java.nio.file.Files.exists;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.DebuggerInfo;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.action.ResumeAction;
import org.eclipse.che.api.debug.shared.model.action.StartAction;
import org.eclipse.che.api.debug.shared.model.action.StepIntoAction;
import org.eclipse.che.api.debug.shared.model.action.StepOutAction;
import org.eclipse.che.api.debug.shared.model.action.StepOverAction;
import org.eclipse.che.api.debug.shared.model.impl.DebuggerInfoImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.StackFrameDumpImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.BreakpointActivatedEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.DisconnectEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.SuspendEventImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.gdb.server.exception.GdbParseException;
import org.eclipse.che.plugin.gdb.server.exception.GdbTerminatedException;
import org.eclipse.che.plugin.gdb.server.parser.GdbContinue;
import org.eclipse.che.plugin.gdb.server.parser.GdbDirectory;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoBreak;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoLine;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoProgram;
import org.eclipse.che.plugin.gdb.server.parser.GdbPrint;
import org.eclipse.che.plugin.gdb.server.parser.GdbRun;
import org.eclipse.che.plugin.gdb.server.parser.GdbVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects to GDB.
 *
 * @author Anatoliy Bazko
 */
public class GdbDebugger implements Debugger {
  private static final Logger LOG = LoggerFactory.getLogger(GdbDebugger.class);
  private static final int CONNECTION_ATTEMPTS = 5;

  private final String host;
  private final int port;
  private final String name;
  private final String version;
  private final String file;

  private Location currentLocation;

  private final Gdb gdb;
  private final DebuggerCallback debuggerCallback;

  GdbDebugger(
      String host,
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

  public static GdbDebugger newInstance(
      String host, int port, String file, String srcDirectory, DebuggerCallback debuggerCallback)
      throws DebuggerException {
    if (!exists(Paths.get(file))) {
      throw new DebuggerException("Can't start GDB: binary " + file + " not found");
    }

    if (!exists(Paths.get(srcDirectory))) {
      throw new DebuggerException(
          "Can't start GDB: source directory " + srcDirectory + " does not exist");
    }

    for (int i = 0; i < CONNECTION_ATTEMPTS - 1; i++) {
      try {
        return init(host, port, file, srcDirectory, debuggerCallback);
      } catch (DebuggerException e) {
        LOG.error("Connection attempt " + i + ": " + e.getMessage(), e);
      }
    }

    return init(host, port, file, srcDirectory, debuggerCallback);
  }

  private static GdbDebugger init(
      String host, int port, String file, String srcDirectory, DebuggerCallback debuggerCallback)
      throws DebuggerException {

    Gdb gdb;
    try {
      gdb = Gdb.start();
    } catch (IOException e) {
      throw new DebuggerException("Can't start GDB: " + e.getMessage(), e);
    }

    try {
      GdbDirectory directory = gdb.directory(srcDirectory);
      LOG.debug("Source directories: " + directory.getDirectories());

      gdb.file(file);
      if (port > 0) {
        gdb.targetRemote(host, port);
      }
    } catch (DebuggerException | IOException | InterruptedException e) {
      gdb.stop();
      throw new DebuggerException("Can't initialize GDB: " + e.getMessage(), e);
    }

    GdbVersion gdbVersion = gdb.getGdbVersion();
    return new GdbDebugger(
        host, port, gdbVersion.getVersion(), gdbVersion.getName(), file, gdb, debuggerCallback);
  }

  @Override
  public DebuggerInfo getInfo() throws DebuggerException {
    return new DebuggerInfoImpl(host, port, name, version, 0, file);
  }

  @Override
  public void disconnect() {
    currentLocation = null;
    debuggerCallback.onEvent(new DisconnectEventImpl());

    gdb.stop();
  }

  @Override
  public void addBreakpoint(Breakpoint breakpoint) throws DebuggerException {
    try {
      Location location = relativeToWorkDir(breakpoint.getLocation());
      if (location.getTarget() == null) {
        gdb.breakpoint(location.getLineNumber());
      } else {
        gdb.breakpoint(location.getTarget(), location.getLineNumber());
      }

      debuggerCallback.onEvent(new BreakpointActivatedEventImpl(breakpoint));
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException("Can't add breakpoint: " + breakpoint + ". " + e.getMessage(), e);
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
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException(
          "Can't delete breakpoint: " + location + ". " + e.getMessage(), e);
    }
  }

  @Override
  public void deleteAllBreakpoints() throws DebuggerException {
    try {
      gdb.delete();
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException("Can't delete all breakpoints. " + e.getMessage(), e);
    }
  }

  @Override
  public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
    try {
      GdbInfoBreak gdbInfoBreak = gdb.infoBreak();
      return gdbInfoBreak.getBreakpoints();
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException("Can't get all breakpoints. " + e.getMessage(), e);
    }
  }

  @Override
  public void start(StartAction action) throws DebuggerException {
    try {
      for (Breakpoint b : action.getBreakpoints()) {
        try {
          addBreakpoint(b);
        } catch (DebuggerException e) {
          // can't add breakpoint, skip it
        }
      }

      Breakpoint breakpoint;
      if (isRemoteConnection()) {
        GdbContinue gdbContinue = gdb.cont();
        breakpoint = gdbContinue.getBreakpoint();
      } else {
        GdbRun gdbRun = gdb.run();
        breakpoint = gdbRun.getBreakpoint();
      }

      if (breakpoint != null) {
        currentLocation = breakpoint.getLocation();
        debuggerCallback.onEvent(new SuspendEventImpl(breakpoint.getLocation(), SuspendPolicy.ALL));
      } else {
        GdbInfoProgram gdbInfoProgram = gdb.infoProgram();
        if (gdbInfoProgram.getStoppedAddress() == null) {
          disconnect();
        }
      }
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException("Error during running. " + e.getMessage(), e);
    }
  }

  @Override
  public void suspend() throws DebuggerException {
    try {
      currentLocation = gdb.suspend(file, isRemoteConnection());
      debuggerCallback.onEvent(new SuspendEventImpl(currentLocation, SuspendPolicy.ALL));
    } catch (IOException | InterruptedException e) {
      throw new DebuggerException("Can not suspend debugger session. " + e.getMessage(), e);
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

      currentLocation = gdbInfoLine.getLocation();
      debuggerCallback.onEvent(new SuspendEventImpl(gdbInfoLine.getLocation(), SuspendPolicy.ALL));
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException("Step into error. " + e.getMessage(), e);
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

      currentLocation = gdbInfoLine.getLocation();
      debuggerCallback.onEvent(new SuspendEventImpl(gdbInfoLine.getLocation(), SuspendPolicy.ALL));
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException("Step into error. " + e.getMessage(), e);
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

      currentLocation = gdbInfoLine.getLocation();
      debuggerCallback.onEvent(new SuspendEventImpl(gdbInfoLine.getLocation(), SuspendPolicy.ALL));
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException("Step out error. " + e.getMessage(), e);
    }
  }

  @Override
  public void resume(ResumeAction action) throws DebuggerException {
    try {
      GdbContinue gdbContinue = gdb.cont();
      Breakpoint breakpoint = gdbContinue.getBreakpoint();

      if (breakpoint != null) {
        currentLocation = breakpoint.getLocation();
        debuggerCallback.onEvent(new SuspendEventImpl(breakpoint.getLocation(), SuspendPolicy.ALL));
      } else {
        GdbInfoProgram gdbInfoProgram = gdb.infoProgram();
        if (gdbInfoProgram.getStoppedAddress() == null) {
          disconnect();
        }
      }
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
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
      gdb.setVar(path.get(0), variable.getValue().getString());
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException(
          "Can't set value for " + variable.getName() + ". " + e.getMessage(), e);
    }
  }

  @Override
  public SimpleValue getValue(VariablePath variablePath) throws DebuggerException {
    try {
      List<String> path = variablePath.getPath();
      if (path.isEmpty()) {
        throw new DebuggerException("Variable path is empty");
      }

      GdbPrint gdbPrint = gdb.print(path.get(0));
      return new SimpleValueImpl(Collections.emptyList(), gdbPrint.getValue());
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException("Can't get value for " + variablePath + ". " + e.getMessage(), e);
    }
  }

  @Override
  public String evaluate(String expression) throws DebuggerException {
    try {
      GdbPrint gdbPrint = gdb.print(expression);
      return gdbPrint.getValue();
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException("Can't evaluate '" + expression + "'. " + e.getMessage(), e);
    }
  }

  /** Dump frame. */
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
        VariableImpl variable =
            new VariableImpl(varType, varName, new SimpleValueImpl(varValue), true, variablePath);
        variables.add(variable);
      }

      return new StackFrameDumpImpl(Collections.emptyList(), variables);
    } catch (GdbTerminatedException e) {
      disconnect();
      throw e;
    } catch (IOException | GdbParseException | InterruptedException e) {
      throw new DebuggerException("Can't dump stack frame. " + e.getMessage(), e);
    }
  }

  private Location relativeToWorkDir(Location location)
      throws InterruptedException, GdbParseException, GdbTerminatedException, IOException {
    String targetFilePath = location.getTarget();
    if (targetFilePath.startsWith("/")) {
      return new LocationImpl(
          targetFilePath.substring(1),
          location.getLineNumber(),
          location.isExternalResource(),
          location.getExternalResourceId(),
          location.getResourceProjectPath(),
          location.getMethod(),
          location.getThreadId());
    } else {
      return location;
    }
  }
}
