/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.zdb.server;

import static org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.StackFrameDumpImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.BreakpointActivatedEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.SuspendEventImpl;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.api.project.server.VirtualFileEntry;
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
import org.eclipse.che.plugin.zdb.server.utils.ZendDbgConnectionUtils;
import org.eclipse.che.plugin.zdb.server.utils.ZendDbgUtils;
import org.eclipse.che.plugin.zdb.server.variables.IDbgVariable;
import org.eclipse.che.plugin.zdb.server.variables.ZendDbgExpressionResolver;
import org.eclipse.che.plugin.zdb.server.variables.ZendDbgVariables;
import org.eclipse.che.plugin.zdb.server.variables.ZendDebuggerVariable;
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
    private final ZendDbgConnection debugConnection;

    private final ZendDbgExpressionResolver debugExpressionResolver;
    private String debugStartFile;
    private List<Variable> debugVariables;
    private List<Breakpoint> breakpoints;
    private Map<Breakpoint, Integer> breakpointIds = new HashMap<>();
    private Integer breakpointAflId = null;

    public ZendDebugger(ZendDbgSettings debugSettings, DebuggerCallback debugCallback) throws DebuggerException {
        this.debugCallback = debugCallback;
        this.debugSettings = debugSettings;
        this.debugVariables = new ArrayList<>();
        this.debugConnection = new ZendDbgConnection(this, debugSettings);
        this.debugExpressionResolver = new ZendDbgExpressionResolver(debugConnection);
    }

    @Override
    public void handleNotification(IDbgEngineNotification notification) {
        switch (notification.getType()) {
        case NOTIFICATION_SESSION_STARTED: {
            handleSessionStarted((SessionStartedNotification) notification);
            break;
        }
        case NOTIFICATION_START_PROCESS_FILE: {
            handleStartProcessFile((StartProcessFileNotification) notification);
            break;
        }
        case NOTIFICATION_READY: {
            handleReady((ReadyNotification) notification);
            break;
        }
        case NOTIFICATION_SRIPT_ENDED: {
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
        breakpoints = new ArrayList<>(action.getBreakpoints());
        LOG.debug("Connect {}:{}", debugSettings.getClientHostIP(), debugSettings.getDebugPort());
    }

    @Override
    public void disconnect() throws DebuggerException {
        // Stop connection daemon thread
        debugConnection.disconnect();
    }

    @Override
    public DebuggerInfo getInfo() throws DebuggerException {
        return new DebuggerInfoImpl("", debugSettings.getDebugPort(), "Zend Debugger", "", 0, null);
    }

    @Override
    public StackFrameDump dumpStackFrame() {
        sendGetVariables();
        return new StackFrameDumpImpl(Collections.emptyList(), debugVariables);
    }

    @Override
    public SimpleValue getValue(VariablePath variablePath) {
        List<Variable> currentVariables = debugVariables;
        Variable matchingVariable = null;
        for (String variableName : variablePath.getPath()) {
            for (Variable currentVariable : currentVariables) {
                if (currentVariable.getName().equals(variableName)) {
                    matchingVariable = currentVariable;
                    currentVariables = new ArrayList<>(currentVariable.getVariables());
                    break;
                }
            }
        }
        return new SimpleValueImpl(currentVariables, matchingVariable.getValue());
    }

    @Override
    public void addBreakpoint(Breakpoint breakpoint) throws DebuggerException {
        breakpoints.add(breakpoint);
        sendAddBreakpoint(breakpoint);
    }

    @Override
    public void deleteBreakpoint(Location location) throws DebuggerException {
        int breakpointLine = location.getLineNumber();
        String breakpointTarget = location.getTarget();
        Breakpoint matchingBreakpoint = null;
        for (Breakpoint breakpoint : breakpoints) {
            if (breakpointLine == breakpoint.getLocation().getLineNumber()
                    && breakpointTarget.equals(breakpoint.getLocation().getTarget())) {
                matchingBreakpoint = breakpoint;
                break;
            }
        }
        if (matchingBreakpoint == null) {
            return;
        }
        breakpoints.remove(matchingBreakpoint);
        int breakpointId = breakpointIds.remove(matchingBreakpoint);
        sendDeleteBreakpoint(breakpointId);
    }

    @Override
    public void deleteAllBreakpoints() throws DebuggerException {
        breakpoints.clear();
        breakpointIds.clear();
        sendDeleteAllBreakpoints();
    }

    @Override
    public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
        return breakpoints;
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
        // TODO - not supported yet...
    }

    @Override
    public String evaluate(String expression) throws DebuggerException {
        // TODO - not supported yet...
        return null;
    }

    @Override
    public String toString() {
        return "ZendDebugger [clientHostIP=" + debugSettings.getClientHostIP() + ", debugPort="
                + debugSettings.getDebugPort() + ", useSsslEncryption=" + debugSettings.isUseSsslEncryption() + "]";
    }

    private void handleSessionStarted(SessionStartedNotification notification) {
        if (!sendSetProtocol()) {
            sendCloseSession();
            LOG.error("Unsupported protocol version: " + notification.getServerProtocolID()
                    + ", only most recent protocol version: " + SUPPORTED_PROTOCOL_ID + " is supported.");
        }
        debugStartFile = notification.getFileName();
        if (debugSettings.isBreakAtFirstLine()) {
            AddBreakpointResponse response = debugConnection
                    .sendRequest(new AddBreakpointRequest(1, 1, -1, debugStartFile));
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
        String localFilePath = ZendDbgUtils.getLocalPath(remoteFilePath);
        VirtualFileEntry localFileEntry = ZendDbgUtils.getVirtualFileEntry(localFilePath);
        if (localFileEntry == null) {
            sendCloseSession();
            LOG.error("Could not found corresponding local file for: " + remoteFilePath);
            return;
        }
        String projectPath = localFileEntry.getProject();
        String fileName = localFileEntry.getName();
        String filePath = localFileEntry.getPath().toString();
        int lineNumber = notification.getLineNumber();
        Location location = new LocationImpl(fileName, lineNumber, filePath, false, -1, projectPath);
        // Send suspend event
        debugCallback.onEvent(new SuspendEventImpl(location));
    }

    private void handleScriptEnded(ScriptEndedNotification notification) {
        sendCloseSession();
    }

    private GetLocalFileContentResponse handleGetLocalFileContent(GetLocalFileContentRequest request) {
        String remoteFilePath = request.getFileName();
        String localFilePath = ZendDbgUtils.getLocalPath(remoteFilePath);
        if (localFilePath == null) {
            LOG.error("Could not found corresponding local file for: " + remoteFilePath);
            return new GetLocalFileContentResponse(request.getID(), GetLocalFileContentResponse.STATUS_FAILURE, null);
        }
        try {
            VirtualFileEntry localFileEntry = ZendDbgUtils.getVirtualFileEntry(localFilePath);
            byte[] localFileContent = localFileEntry.getVirtualFile().getContentAsBytes();
            // Check if remote content is equal to corresponding local one
            if (ZendDbgConnectionUtils.isRemoteContentEqual(request.getSize(), request.getCheckSum(),
                    localFileContent)) {
                // Remote and local contents are identical
                return new GetLocalFileContentResponse(request.getID(),
                        GetLocalFileContentResponse.STATUS_FILES_IDENTICAL, null);
            }
            // Remote and local contents are different, send local content to
            // the engine
            return new GetLocalFileContentResponse(request.getID(), GetLocalFileContentResponse.STATUS_SUCCESS,
                    localFileContent);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return new GetLocalFileContentResponse(request.getID(), GetLocalFileContentResponse.STATUS_FAILURE, null);
    }

    private void sendStartSession() {
        debugConnection.sendRequest(new StartRequest());
    }

    private boolean sendSetProtocol() {
        SetProtocolResponse response = debugConnection.sendRequest(new SetProtocolRequest(SUPPORTED_PROTOCOL_ID));
        if (isOK(response)) {
            return response.getProtocolID() >= SUPPORTED_PROTOCOL_ID;
        }
        return false;
    }

    private void sendContinueProcessFile() {
        debugConnection.sendNotification(new ContinueProcessFileNotification());
    }

    private void sendGetVariables() {
        List<IDbgVariable> variables = new ArrayList<>();
        ZendDbgVariables zendVariablesExpression = new ZendDbgVariables(debugExpressionResolver);
        zendVariablesExpression.resolve();
        variables = zendVariablesExpression.getChildren();
        debugVariables.clear();
        for (IDbgVariable variable : variables) {
            debugVariables.add(new ZendDebuggerVariable(new VariablePathImpl(variable.getName()), variable));
        }
    }

    private void sendAddBreakpointFiles() {
        Set<String> breakpointFiles = new HashSet<>();
        for (Breakpoint breakpoint : breakpoints) {
            String absoluteRemotePath = ZendDbgUtils.getAbsolutePath(breakpoint.getLocation().getResourcePath());
            breakpointFiles.add(absoluteRemotePath);
        }
        debugConnection.sendRequest(new AddFilesRequest(breakpointFiles));
    }

    private void sendAddBreakpoints(String remoteFilePath) {
        String localFilePath = ZendDbgUtils.getLocalPath(remoteFilePath);
        List<Breakpoint> fileBreakpoints = new ArrayList<>();
        for (Breakpoint breakpoint : breakpoints) {
            if (breakpoint.getLocation().getResourcePath().equals(localFilePath)) {
                fileBreakpoints.add(breakpoint);
            }
        }
        for (Breakpoint breakpoint : fileBreakpoints) {
            AddBreakpointResponse response = debugConnection.sendRequest(
                    new AddBreakpointRequest(1, 2, breakpoint.getLocation().getLineNumber(), remoteFilePath));
            if (isOK(response)) {
                breakpointIds.put(breakpoint, response.getBreakpointID());
                // Send breakpoint activated event
                debugCallback.onEvent(new BreakpointActivatedEventImpl(breakpoint));
            }
        }
    }

    private void sendAddBreakpoint(Breakpoint breakpoint) {
        String remoteFilePath = ZendDbgUtils.getAbsolutePath(breakpoint.getLocation().getResourcePath());
        AddBreakpointResponse response = debugConnection
                .sendRequest(new AddBreakpointRequest(1, 2, breakpoint.getLocation().getLineNumber(), remoteFilePath));
        if (isOK(response)) {
            breakpointIds.put(breakpoint, response.getBreakpointID());
            debugCallback.onEvent(new BreakpointActivatedEventImpl(breakpoint));
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

}
