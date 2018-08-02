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
package org.eclipse.che.plugin.nodejsdbg.server.command;

import static java.lang.String.format;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsDebugProcess;
import org.eclipse.che.plugin.nodejsdbg.server.NodeJsDebugger;
import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerException;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsBackTraceParser;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsBreakpointsParser;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsBreakpointsParser.Breakpoints;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsOutputParser;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsOutputParser.NodeJsOutputRegExpParser;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsScriptsParser;
import org.eclipse.che.plugin.nodejsdbg.server.parser.NodeJsScriptsParser.Scripts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Library of the NodeJs debug commands: https://nodejs.org/api/debugger.html
 *
 * @author Anatolii Bazko
 */
public class NodeJsDebugCommandsLibrary {
  private static final Logger LOG = LoggerFactory.getLogger(NodeJsDebugger.class);
  private static final Pattern PROCESS_TITLE_COMMAND_OUTPUT_PATTERN =
      Pattern.compile("^'?(node|nodejs)'?$");
  private static final Pattern PROCESS_VERSION_COMMAND_OUTPUT_PATTERN =
      Pattern.compile("^'?(v|)[0-9\\.]+'?$");
  private static final Pattern PROCESS_PID_COMMAND_OUTPUT_PATTERN = Pattern.compile("^[0-9]+$");
  private static final Pattern RUN_COMMAND_OUTPUT_PATTERN = Pattern.compile("Debugger attached.");

  private final NodeJsDebugProcess process;
  private final String name;
  private final String version;
  private final int pid;

  public NodeJsDebugCommandsLibrary(NodeJsDebugProcess process) throws NodeJsDebuggerException {
    this.process = process;
    run();

    this.name = detectName();
    this.version = detectVersion();
    this.pid = detectPid();
  }

  /** Execute {@code bt} command. */
  public Void backtrace() throws NodeJsDebuggerException {
    doExecute(createCommand("bt", NodeJsOutputParser.VOID));
    return null;
  }

  /** Execute {@code sb} command. */
  public Void setBreakpoint(String scriptPath, int lineNumber) throws NodeJsDebuggerException {
    String scriptName = Paths.get(scriptPath).getFileName().toString();
    String input = format("sb('%s', %d)", scriptName, lineNumber);
    NodeJsDebugCommand<Void> command = createCommand(input, NodeJsOutputParser.VOID);
    return doExecute(command);
  }

  /**
   * Execute {@code breakpoints} command.
   *
   * @see NodeJsBackTraceParser
   */
  public List<Breakpoint> getBreakpoints() throws NodeJsDebuggerException {
    NodeJsDebugCommand<Breakpoints> breakpointsCommand =
        createCommand("breakpoints", NodeJsBreakpointsParser.INSTANCE);
    List<Breakpoint> breakpoints = doExecute(breakpointsCommand).getAll();

    NodeJsDebugCommand<Scripts> scriptsCommand =
        createCommand("scripts", NodeJsScriptsParser.INSTANCE);
    Map<Integer, String> scripts = doExecute(scriptsCommand).getAll();

    for (int i = 0; i < breakpoints.size(); i++) {
      Breakpoint breakpoint = breakpoints.get(i);
      Location location = breakpoint.getLocation();

      String newTarget;
      String[] target = location.getTarget().split(":");
      if (target.length != 2) {
        LOG.error(format("Illegal breakpoint location format %s", target[0]));
        continue;
      }

      if (target[0].equals("scriptId")) {
        newTarget = scripts.get((int) Double.parseDouble(target[1]));
      } else {
        newTarget = target[1];
      }

      Location newLocation = new LocationImpl(newTarget, location.getLineNumber());
      Breakpoint newBreakpoint =
          new BreakpointImpl(
              newLocation, breakpoint.isEnabled(), breakpoint.getBreakpointConfiguration());
      breakpoints.set(i, newBreakpoint);
    }

    return breakpoints;
  }

  /** Execute {@code cb} command. */
  public Void clearBreakpoint(String script, int lineNumber) throws NodeJsDebuggerException {
    String input = format("cb('%s', %d)", script, lineNumber);
    NodeJsDebugCommand<Void> command = createCommand(input, NodeJsOutputParser.VOID);
    return doExecute(command);
  }

  /** Execute {@code run} command. */
  public String run() throws NodeJsDebuggerException {
    NodeJsDebugCommand<String> nextCommand =
        createCommand("run", new NodeJsOutputRegExpParser(RUN_COMMAND_OUTPUT_PATTERN));
    return doExecute(nextCommand);
  }

  /** Execute {@code next} command. */
  public Void next() throws NodeJsDebuggerException {
    doExecute(createCommand("next", NodeJsOutputParser.VOID));
    return null;
  }

  /** Execute {@code cont} command. */
  public Void cont() throws NodeJsDebuggerException {
    doExecute(createCommand("cont", NodeJsOutputParser.VOID));
    return null;
  }

  /** Execute {@code step in} command. */
  public Void stepIn() throws NodeJsDebuggerException {
    doExecute(createCommand("step", NodeJsOutputParser.VOID));
    return null;
  }

  /** Execute {@code step out} command. */
  public Void stepOut() throws NodeJsDebuggerException {
    doExecute(createCommand("out", NodeJsOutputParser.VOID));
    return null;
  }

  /** Execute {@code exec} command to set a new value for the giving variable. */
  public Void setVar(String varName, String newValue) throws NodeJsDebuggerException {
    String input = format("exec %s=%s", varName, newValue);
    NodeJsDebugCommand<Void> command = createCommand(input, NodeJsOutputParser.VOID);
    return doExecute(command);
  }

  /** Execute {@code exec} command to get value for the giving variable. */
  public String getVar(String varName) throws NodeJsDebuggerException {
    String line = format("exec %s", varName);
    NodeJsDebugCommand<String> command = createCommand(line, NodeJsOutputParser.DEFAULT);
    return doExecute(command);
  }

  /** Execute {@code exec} command to evaluate expression. */
  public String evaluate(String expression) throws NodeJsDebuggerException {
    String line = format("exec %s", expression);
    NodeJsDebugCommand<String> command = createCommand(line, NodeJsOutputParser.DEFAULT);
    return doExecute(command);
  }

  /** Returns NodeJs version. */
  public String getVersion() {
    return version;
  }

  /** Returns NodeJs title. */
  public String getName() {
    return name;
  }

  /** Returns NodeJs pid. */
  public int getPid() {
    return pid;
  }

  /** Returns NodeJs version. */
  private String detectVersion() throws NodeJsDebuggerException {
    NodeJsDebugCommand<String> command =
        createCommand(
            "process.version",
            new NodeJsOutputRegExpParser(PROCESS_VERSION_COMMAND_OUTPUT_PATTERN));
    return doExecute(command);
  }

  /** Returns NodeJs pid. */
  private int detectPid() throws NodeJsDebuggerException {
    NodeJsDebugCommand<String> command =
        createCommand(
            "process.pid", new NodeJsOutputRegExpParser(PROCESS_PID_COMMAND_OUTPUT_PATTERN));
    return Integer.parseInt(doExecute(command));
  }

  /** Returns NodeJs title. */
  private String detectName() throws NodeJsDebuggerException {
    NodeJsDebugCommand<String> command =
        createCommand(
            "process.title", new NodeJsOutputRegExpParser(PROCESS_TITLE_COMMAND_OUTPUT_PATTERN));
    return doExecute(command);
  }

  /**
   * Executes {@link NodeJsDebugCommand} and waits result.
   *
   * @throws NodeJsDebuggerException if execution failed
   */
  private <V> V doExecute(NodeJsDebugCommand<V> command) throws NodeJsDebuggerException {
    process.addObserver(command);
    try {
      Future<V> result = command.execute(process);
      return result.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new NodeJsDebuggerException(e.getMessage(), e);
    } finally {
      process.removeObserver(command);
    }
  }

  private <T> NodeJsDebugCommand<T> createCommand(
      String input, NodeJsOutputParser<T> outputParser) {
    return new NodeJsDebugCommandImpl<>(outputParser, input);
  }
}
