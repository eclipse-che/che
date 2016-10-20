/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.server.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
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
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.StackFrameDumpImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.BreakpointActivatedEventImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.SuspendEventImpl;
import org.eclipse.che.api.debugger.server.Debugger.DebuggerCallback;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.api.project.server.VirtualFileEntry;

import zend.com.che.plugin.zdb.server.IDbgDelegate;
import zend.com.che.plugin.zdb.server.connection.ZendDbgClientMessages.*;
import zend.com.che.plugin.zdb.server.connection.ZendDbgConnection.IEngineMessageHandler;
import zend.com.che.plugin.zdb.server.connection.ZendDbgEngineMessages.*;
import zend.com.che.plugin.zdb.server.utils.ZendDbgUtils;
import zend.com.che.plugin.zdb.server.variables.ZendDbgVariables;
import zend.com.che.plugin.zdb.server.variables.IDbgVariable;
import zend.com.che.plugin.zdb.server.variables.ZendDbgExpressionResolver;
import zend.com.che.plugin.zdb.server.variables.ZendDebuggerVariable;

import static zend.com.che.plugin.zdb.server.connection.ZendDbgEngineMessages.*;

/**
 * Zend debug session.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgSession implements IDbgDelegate, IEngineMessageHandler {

	private static final int SUPPORTED_PROTOCOL_ID = 2012121702;

	private final DebuggerCallback debuggerCallback;
	private final ZendDbgSessionSettings debugSettings;
	private final ZendDbgConnection debugConnection;

	private final ZendDbgExpressionResolver expressionResolver;
	private List<Variable> stackVariables;
	private List<Breakpoint> breakpoints;
	private Map<Breakpoint, Integer> breakpointIds = new HashMap<>();

	public ZendDbgSession(ZendDbgSessionSettings debugSettings, DebuggerCallback debuggerCallback) {
		this.debuggerCallback = debuggerCallback;
		this.debugSettings = debugSettings;
		this.stackVariables = new ArrayList<>();
		this.debugConnection = new ZendDbgConnection(this, debugSettings);
		this.expressionResolver = new ZendDbgExpressionResolver(debugConnection);
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
		breakpoints = new ArrayList<>(action.getBreakpoints());
		debugConnection.connect();
	}

	@Override
	public StackFrameDump dumpStackFrame() {
		sendGetVariables();
		return new StackFrameDumpImpl(Collections.emptyList(), stackVariables);
	}

	@Override
	public SimpleValue getValue(VariablePath variablePath) {
		List<Variable> currentVariables = stackVariables;
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
	public void setValue(Variable variable) throws DebuggerException {
		// TODO - not supported yet...
	}

	@Override
	public String evaluate(String expression) throws DebuggerException {
		// TODO - not supported yet...
		return null;
	}

	@Override
	public void stepOver(StepOverAction action) throws DebuggerException {
		debugConnection.sendRequest(new StepOverRequest());
	}

	@Override
	public void stepInto(StepIntoAction action) throws DebuggerException {
		debugConnection.sendRequest(new StepIntoRequest());
	}

	@Override
	public void stepOut(StepOutAction action) throws DebuggerException {
		debugConnection.sendRequest(new StepOutRequest());
	}

	@Override
	public void resume(ResumeAction action) throws DebuggerException {
		debugConnection.sendRequest(new GoRequest());
	}

	@Override
	public void disconnect() throws DebuggerException {
		debugConnection.disconnect();
	}

	private void handleSessionStarted(SessionStartedNotification notification) {
		if (!sendSetProtocol()) {
			sendCloseSession();
			// TODO - log info here about unsupported protocol
		}
		sendBreakAtFirstLine(notification);
		sendAddBreakpointFiles();
		sendStartSession();
	}

	private void handleStartProcessFile(StartProcessFileNotification notification) {
		sendAddBreakpoints(notification.getFileName());
		sendContinueProcessFile();
	}

	private void handleReady(ReadyNotification notification) {
		String localFilePath = ZendDbgUtils.getLocalPath(notification.getFileName());
		VirtualFileEntry localFileEntry = ZendDbgUtils.getVirtualFileEntry(localFilePath);
		if (localFileEntry == null) {
			// TODO - no local corresponding file found, log info
			sendCloseSession();
			return;
		}
		String projectPath = localFileEntry.getProject();
		String fileName = localFileEntry.getName();
		String filePath = localFileEntry.getPath().toString();
		int lineNumber = notification.getLineNumber();
		Location location = new LocationImpl(fileName, lineNumber, filePath, false, -1, projectPath);
		// Send suspend event
		debuggerCallback.onEvent(new SuspendEventImpl(location));
	}

	private void handleScriptEnded(ScriptEndedNotification notification) {
		sendCloseSession();
	}

	private GetLocalFileContentResponse handleGetLocalFileContent(GetLocalFileContentRequest request) {
		// Assume that files are identical for now..
		return new GetLocalFileContentResponse(request.getID(), GetLocalFileContentResponse.STATUS_FILES_IDENTICAL,
				null);
	}

	private boolean sendSetProtocol() {
		SetProtocolResponse response = debugConnection.sendRequest(new SetProtocolRequest(SUPPORTED_PROTOCOL_ID));
		if (response != null && response.getProtocolID() < SUPPORTED_PROTOCOL_ID) {
			return false;
		}
		return true;
	}

	private void sendContinueProcessFile() {
		debugConnection.sendNotification(new ContinueProcessFileNotification());
	}

	private void sendStartSession() {
		debugConnection.sendRequest(new StartRequest());
	}

	private void sendGetVariables() {
		List<IDbgVariable> variables = new ArrayList<>();
		ZendDbgVariables stackVariablesExpression = new ZendDbgVariables(expressionResolver);
		stackVariablesExpression.resolve();
		variables = stackVariablesExpression.getChildren();
		stackVariables.clear();
		for (IDbgVariable variable : variables) {
			stackVariables.add(new ZendDebuggerVariable(new VariablePathImpl(variable.getName()), variable));
		}
	}

	private void sendBreakAtFirstLine(SessionStartedNotification notification) {
		if (!debugSettings.isBreakAtFirstLine() || notification.getQuery().contains("debug_stop=1")) {
			return;
		}
		debugConnection.sendRequest(
				new AddBreakpointRequest(1, 1, -1, notification.getFileName()));
	}

	private void sendAddBreakpointFiles() {
		Set<String> breakpointFiles = new HashSet<>();
		for (Breakpoint breakpoint : breakpoints) {
			String absolutePath = ZendDbgUtils.getAbsolutePath(breakpoint.getLocation().getResourcePath());
			breakpointFiles.add(absolutePath);
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
			if (response != null && response.getStatus() == 0) {
				breakpointIds.put(breakpoint, response.getBreakpointID());
				// Send breakpoint activated event
				debuggerCallback.onEvent(new BreakpointActivatedEventImpl(breakpoint));
			}
		}
	}

	private void sendAddBreakpoint(Breakpoint breakpoint) {
		String remoteFilePath = ZendDbgUtils.getAbsolutePath(breakpoint.getLocation().getResourcePath());
		AddBreakpointResponse response = debugConnection
				.sendRequest(new AddBreakpointRequest(1, 2, breakpoint.getLocation().getLineNumber(), remoteFilePath));
		if (response != null && response.getStatus() == 0) {
			breakpointIds.put(breakpoint, response.getBreakpointID());
			debuggerCallback.onEvent(new BreakpointActivatedEventImpl(breakpoint));
		}
	}

	private void sendDeleteBreakpoint(int breakpointId) {
		debugConnection.sendRequest(new DeleteBreakpointRequest(breakpointId));
	}

	private void sendDeleteAllBreakpoints() {
		debugConnection.sendRequest(new DeleteAllBreakpointsRequest());
	}

	private void sendCloseSession() {
		debugConnection.sendNotification(new CloseSessionNotification());
	}

}
