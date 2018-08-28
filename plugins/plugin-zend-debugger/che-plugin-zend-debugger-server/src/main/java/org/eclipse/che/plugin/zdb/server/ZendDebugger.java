/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.NOTIFICATION_READY;
import static org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.NOTIFICATION_SESSION_STARTED;
import static org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.NOTIFICATION_SRIPT_ENDED;
import static org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.NOTIFICATION_START_PROCESS_FILE;
import static org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.REQUEST_GET_LOCAL_FILE_CONTENT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.eclipse.che.api.debug.shared.model.impl.StackFrameDumpImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.BreakpointActivatedEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.SuspendEventImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.AddBreakpointRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.AddFilesRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.CloseSessionNotification;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.ContinueProcessFileNotification;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.DeleteAllBreakpointsRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.DeleteBreakpointRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.GetLocalFileContentResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.GoRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.IDbgClientResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.SetProtocolRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.StartRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.StepIntoRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.StepOutRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.StepOverRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgConnection;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgConnection.IEngineMessageHandler;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.AddBreakpointResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.GetLocalFileContentRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.IDbgEngineNotification;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.IDbgEngineRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.IDbgEngineResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.ReadyNotification;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.ScriptEndedNotification;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.SessionStartedNotification;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.SetProtocolResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.StartProcessFileNotification;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgSettings;
import org.eclipse.che.plugin.zdb.server.expressions.IDbgExpression;
import org.eclipse.che.plugin.zdb.server.expressions.ZendDbgExpression;
import org.eclipse.che.plugin.zdb.server.expressions.ZendDbgExpressionEvaluator;
import org.eclipse.che.plugin.zdb.server.utils.ZendDbgConnectionUtils;
import org.eclipse.che.plugin.zdb.server.utils.ZendDbgVariableUtils;
import org.eclipse.che.plugin.zdb.server.variables.IDbgVariable;
import org.eclipse.che.plugin.zdb.server.variables.ZendDbgVariable;
import org.eclipse.che.plugin.zdb.server.variables.ZendDbgVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zend Debugger for PHP.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugger implements Debugger, IEngineMessageHandler {

  public static final Logger LOG = LoggerFactory.getLogger(ZendDebugger.class);
  private static final int SUPPORTED_PROTOCOL_ID = 2012121702;
  private final DebuggerCallback debugCallback;
  private final ZendDbgSettings debugSettings;
  private final ZendDbgLocationHandler debugLocationHandler;
  private final ZendDbgConnection debugConnection;
  private final FsManager fsManager;
  private final ZendDbgExpressionEvaluator debugExpressionEvaluator;
  private VariablesStorage debugVariableStorage;
  private String debugStartFile;
  private Map<Breakpoint, ZendDbgBreakpoint> breakpoints = new LinkedHashMap<>();
  private Map<ZendDbgBreakpoint, Integer> breakpointIds = new LinkedHashMap<>();
  private Integer breakpointAflId = null;

  public ZendDebugger(
      ZendDbgSettings debugSettings,
      ZendDbgLocationHandler debugLocationHandler,
      DebuggerCallback debugCallback,
      FsManager fsManager)
      throws DebuggerException {
    this.debugCallback = debugCallback;
    this.debugSettings = debugSettings;
    this.debugLocationHandler = debugLocationHandler;
    this.debugConnection = new ZendDbgConnection(this, debugSettings);
    this.fsManager = fsManager;
    this.debugExpressionEvaluator = new ZendDbgExpressionEvaluator(debugConnection);
    this.debugVariableStorage = new VariablesStorage(Collections.emptyList());
  }

  @Override
  public void handleNotification(IDbgEngineNotification notification) {
    switch (notification.getType()) {
      case NOTIFICATION_SESSION_STARTED:
        {
          handleSessionStarted((SessionStartedNotification) notification);
          break;
        }
      case NOTIFICATION_START_PROCESS_FILE:
        {
          handleStartProcessFile((StartProcessFileNotification) notification);
          break;
        }
      case NOTIFICATION_READY:
        {
          handleReady((ReadyNotification) notification);
          break;
        }
      case NOTIFICATION_SRIPT_ENDED:
        {
          handleScriptEnded((ScriptEndedNotification) notification);
          break;
        }
      default:
        break;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IDbgClientResponse> T handleRequest(IDbgEngineRequest<T> request) {
    switch (request.getType()) {
      case REQUEST_GET_LOCAL_FILE_CONTENT:
        return (T) handleGetLocalFileContent((GetLocalFileContentRequest) request);
    }
    return null;
  }

  @Override
  public void start(StartAction action) throws DebuggerException {
    // Initialize connection daemon thread
    debugConnection.connect();
    for (Breakpoint breakpoint : action.getBreakpoints()) {
      breakpoints.put(breakpoint, ZendDbgBreakpoint.create(breakpoint, debugLocationHandler));
    }
    LOG.debug("Connect {}:{}", debugSettings.getClientHostIP(), debugSettings.getDebugPort());
  }

  @Override
  public void disconnect() throws DebuggerException {
    // Stop connection daemon thread
    debugConnection.disconnect();
  }

  @Override
  public DebuggerInfo getInfo() throws DebuggerException {
    return new DebuggerInfoImpl(
        debugSettings.getClientHostIP(),
        debugSettings.getDebugPort(),
        "Zend Debugger",
        "",
        0,
        null);
  }

  @Override
  public StackFrameDump dumpStackFrame() {
    sendGetVariables();
    return new StackFrameDumpImpl(Collections.emptyList(), debugVariableStorage.getVariables());
  }

  @Override
  public SimpleValue getValue(VariablePath variablePath) {
    IDbgVariable matchingVariable = debugVariableStorage.findVariable(variablePath);
    matchingVariable.makeComplete();
    return matchingVariable.getValue();
  }

  @Override
  public void addBreakpoint(Breakpoint breakpoint) throws DebuggerException {
    ZendDbgBreakpoint dbgBreakpoint = ZendDbgBreakpoint.create(breakpoint, debugLocationHandler);
    breakpoints.put(breakpoint, dbgBreakpoint);
    sendAddBreakpoint(dbgBreakpoint);
  }

  @Override
  public void deleteBreakpoint(Location location) throws DebuggerException {
    Breakpoint matchingBreakpoint = null;
    for (Breakpoint breakpoint : breakpoints.keySet()) {
      if (breakpoint.getLocation().equals(location)) {
        matchingBreakpoint = breakpoint;
        break;
      }
    }
    if (matchingBreakpoint == null) {
      return;
    }
    ZendDbgBreakpoint dbgBreakpoint = breakpoints.remove(matchingBreakpoint);
    // Unregister breakpoint if it was registered in active session
    if (breakpointIds.containsKey(dbgBreakpoint)) {
      int breakpointId = breakpointIds.remove(dbgBreakpoint);
      sendDeleteBreakpoint(breakpointId);
    }
  }

  @Override
  public void deleteAllBreakpoints() throws DebuggerException {
    breakpoints.clear();
    breakpointIds.clear();
    sendDeleteAllBreakpoints();
  }

  @Override
  public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
    return new ArrayList<>(breakpoints.keySet());
  }

  @Override
  public void stepOver(StepOverAction action) throws DebuggerException {
    sendStepOver();
  }

  @Override
  public void stepInto(StepIntoAction action) throws DebuggerException {
    sendStepInto();
  }

  @Override
  public void stepOut(StepOutAction action) throws DebuggerException {
    sendStepOut();
  }

  @Override
  public void resume(ResumeAction action) throws DebuggerException {
    sendGo();
  }

  @Override
  public void setValue(Variable variable) throws DebuggerException {
    Variable matchingVariable = debugVariableStorage.findVariable(variable.getVariablePath());
    ((ZendDbgVariable) matchingVariable).setValue(variable.getValue().getString());
  }

  @Override
  public String evaluate(String expression) throws DebuggerException {
    ZendDbgExpression zendDbgExpression =
        new ZendDbgExpression(debugExpressionEvaluator, expression, Collections.emptyList());
    zendDbgExpression.evaluate();
    return zendDbgExpression.getValue();
  }

  @Override
  public String toString() {
    return "ZendDebugger [clientHostIP="
        + debugSettings.getClientHostIP()
        + ", debugPort="
        + debugSettings.getDebugPort()
        + ", useSsslEncryption="
        + debugSettings.isUseSsslEncryption()
        + "]";
  }

  private void handleSessionStarted(SessionStartedNotification notification) {
    if (!sendSetProtocol()) {
      sendCloseSession();
      LOG.error(
          "Unsupported protocol version: "
              + notification.getServerProtocolID()
              + ", only most recent protocol version: "
              + SUPPORTED_PROTOCOL_ID
              + " is supported.");
    }
    debugStartFile = notification.getFileName();
    if (debugSettings.isBreakAtFirstLine()) {
      AddBreakpointResponse response =
          debugConnection.sendRequest(new AddBreakpointRequest(1, 1, -1, debugStartFile));
      if (isOK(response)) {
        breakpointAflId = response.getBreakpointID();
      }
    }
    sendAddBreakpointFiles();
    sendStartSession();
  }

  private void handleStartProcessFile(StartProcessFileNotification notification) {
    sendAddBreakpoints(notification.getFileName());
    sendContinueProcessFile();
  }

  private void handleReady(ReadyNotification notification) {
    String remoteFilePath = notification.getFileName();
    if (breakpointAflId != null && remoteFilePath.equals(debugStartFile)) {
      debugConnection.sendRequest(new DeleteBreakpointRequest(breakpointAflId));
      breakpointAflId = null;
    }
    int lineNumber = notification.getLineNumber();
    Location dbgLocation = ZendDbgLocationHandler.createDBG(remoteFilePath, lineNumber);
    // Convert DBG location from engine to VFS location
    Location vfsLocation = debugLocationHandler.convertToVFS(dbgLocation);
    // Send suspend event
    debugCallback.onEvent(new SuspendEventImpl(vfsLocation, SuspendPolicy.ALL));
  }

  private void handleScriptEnded(ScriptEndedNotification notification) {
    sendCloseSession();
  }

  private GetLocalFileContentResponse handleGetLocalFileContent(
      GetLocalFileContentRequest request) {
    String remoteFilePath = request.getFileName();

    String wsPath = absolutize(remoteFilePath);
    if (!fsManager.exists(wsPath)) {
      LOG.error("Could not found corresponding local file for: " + remoteFilePath);
      return new GetLocalFileContentResponse(
          request.getID(), GetLocalFileContentResponse.STATUS_FAILURE, null);
    }
    try {
      byte[] localFileContent = fsManager.readAsString(wsPath).getBytes();
      // Check if remote content is equal to corresponding local one
      if (ZendDbgConnectionUtils.isRemoteContentEqual(
          request.getSize(), request.getCheckSum(), localFileContent)) {
        // Remote and local contents are identical
        return new GetLocalFileContentResponse(
            request.getID(), GetLocalFileContentResponse.STATUS_FILES_IDENTICAL, null);
      }
      // Remote and local contents are different, send local content to the engine
      return new GetLocalFileContentResponse(
          request.getID(), GetLocalFileContentResponse.STATUS_SUCCESS, localFileContent);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return new GetLocalFileContentResponse(
        request.getID(), GetLocalFileContentResponse.STATUS_FAILURE, null);
  }

  private void sendStartSession() {
    debugConnection.sendRequest(new StartRequest());
  }

  private boolean sendSetProtocol() {
    SetProtocolResponse response =
        debugConnection.sendRequest(new SetProtocolRequest(SUPPORTED_PROTOCOL_ID));
    if (isOK(response)) {
      return response.getProtocolID() >= SUPPORTED_PROTOCOL_ID;
    }
    return false;
  }

  private void sendContinueProcessFile() {
    debugConnection.sendNotification(new ContinueProcessFileNotification());
  }

  private void sendGetVariables() {
    ZendDbgVariables zendVariablesExpression = new ZendDbgVariables(debugExpressionEvaluator);
    zendVariablesExpression.evaluate();
    List<IDbgVariable> variables = new ArrayList<>();
    int variableId = 0;
    for (IDbgExpression zendVariableExpression : zendVariablesExpression.getChildren()) {
      if (VariablesStorage.GLOBALS_VARIABLE.equalsIgnoreCase(
          zendVariableExpression.getExpression())) {
        continue;
      }
      IDbgVariable variable =
          new ZendDbgVariable(
              new VariablePathImpl(String.valueOf(variableId++)), zendVariableExpression);
      if (ZendDbgVariableUtils.isThis(zendVariableExpression.getExpression())) {
        // $this always on top
        variables.add(0, variable);
      } else {
        variables.add(variable);
      }
    }
    debugVariableStorage = new VariablesStorage(variables);
  }

  private void sendAddBreakpointFiles() {
    Set<String> breakpointFiles = new HashSet<>();
    for (ZendDbgBreakpoint dbgBreakpoint : breakpoints.values()) {
      breakpointFiles.add(dbgBreakpoint.getLocation().getTarget());
    }
    debugConnection.sendRequest(new AddFilesRequest(breakpointFiles));
  }

  private void sendAddBreakpoints(String remoteFilePath) {
    List<ZendDbgBreakpoint> fileBreakpoints = new ArrayList<>();
    for (ZendDbgBreakpoint dbgBreakpoint : breakpoints.values()) {
      if (dbgBreakpoint.getLocation().getTarget().equals(remoteFilePath)) {
        fileBreakpoints.add(dbgBreakpoint);
      }
    }
    for (ZendDbgBreakpoint dbgBreakpoint : fileBreakpoints) {
      AddBreakpointResponse response =
          debugConnection.sendRequest(
              new AddBreakpointRequest(
                  1, 2, dbgBreakpoint.getLocation().getLineNumber(), remoteFilePath));
      if (isOK(response)) {
        // Breakpoint was successfully registered in active session, send breakpoint activated event
        breakpointIds.put(dbgBreakpoint, response.getBreakpointID());
        debugCallback.onEvent(new BreakpointActivatedEventImpl(dbgBreakpoint.getVfsBreakpoint()));
      }
    }
  }

  private void sendAddBreakpoint(ZendDbgBreakpoint dbgBreakpoint) {
    AddBreakpointResponse response =
        debugConnection.sendRequest(
            new AddBreakpointRequest(
                1,
                2,
                dbgBreakpoint.getLocation().getLineNumber(),
                dbgBreakpoint.getLocation().getTarget()));
    if (isOK(response)) {
      // Breakpoint was successfully registered in active session, send breakpoint activated event
      breakpointIds.put(dbgBreakpoint, response.getBreakpointID());
      debugCallback.onEvent(new BreakpointActivatedEventImpl(dbgBreakpoint.getVfsBreakpoint()));
    }
  }

  private void sendDeleteBreakpoint(int breakpointId) {
    debugConnection.sendRequest(new DeleteBreakpointRequest(breakpointId));
  }

  private void sendDeleteAllBreakpoints() {
    debugConnection.sendRequest(new DeleteAllBreakpointsRequest());
  }

  private void sendStepOver() {
    debugConnection.sendRequest(new StepOverRequest());
  }

  private void sendStepInto() {
    debugConnection.sendRequest(new StepIntoRequest());
  }

  private void sendStepOut() {
    debugConnection.sendRequest(new StepOutRequest());
  }

  private void sendGo() {
    debugConnection.sendRequest(new GoRequest());
  }

  private void sendCloseSession() {
    debugConnection.sendNotification(new CloseSessionNotification());
  }

  private boolean isOK(IDbgEngineResponse response) {
    return response != null && response.getStatus() == 0;
  }

  private static final class VariablesStorage {

    private static final String GLOBALS_VARIABLE = "$GLOBALS";

    private final List<IDbgVariable> variables;

    public VariablesStorage(List<IDbgVariable> variables) {
      this.variables = variables;
    }

    List<IDbgVariable> getVariables() {
      return variables;
    }

    IDbgVariable findVariable(VariablePath variablePath) {
      List<IDbgVariable> currentVariables = variables;
      IDbgVariable matchingVariable = null;
      Iterator<String> pathIterator = variablePath.getPath().iterator();
      while (pathIterator.hasNext()) {
        String pathElement = pathIterator.next();
        for (IDbgVariable currentVariable : currentVariables) {
          List<String> currentVariablePath = currentVariable.getVariablePath().getPath();
          String currentVariablePathElement =
              currentVariablePath.get(currentVariablePath.size() - 1);
          if (currentVariablePathElement.equals(pathElement)) {
            matchingVariable = currentVariable;
            if (pathIterator.hasNext()) {
              currentVariables =
                  currentVariable
                      .getValue()
                      .getVariables()
                      .stream()
                      .map(v -> (IDbgVariable) v)
                      .collect(Collectors.toList());
            }
            break;
          }
        }
      }
      return matchingVariable;
    }
  }

  private static final class ZendDbgBreakpoint {

    private Location dbgLocation;
    private Breakpoint vfsBreakpoint;

    private ZendDbgBreakpoint(Location dbgLocation, Breakpoint vfsBreakpoint) {
      this.dbgLocation = dbgLocation;
      this.vfsBreakpoint = vfsBreakpoint;
    }

    public static ZendDbgBreakpoint create(
        Breakpoint vfsBreakpoint, ZendDbgLocationHandler debugLocationHandler) {
      Location dbgLocation = debugLocationHandler.convertToDBG(vfsBreakpoint.getLocation());
      return new ZendDbgBreakpoint(dbgLocation, vfsBreakpoint);
    }

    public Location getLocation() {
      return dbgLocation;
    }

    public Breakpoint getVfsBreakpoint() {
      return vfsBreakpoint;
    }
  }
}
