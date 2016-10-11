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

import static zend.com.che.plugin.zdb.server.connection.IDebugDataFacet.Facet.KIND_LOCAL;
import static zend.com.che.plugin.zdb.server.connection.IDebugDataFacet.Facet.KIND_SUPER_GLOBAL;
import static zend.com.che.plugin.zdb.server.connection.IDebugDataFacet.Facet.KIND_THIS;

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

import zend.com.che.plugin.zdb.server.IDebuggerDelegate;
import zend.com.che.plugin.zdb.server.connection.ZendClientMessages.*;
import zend.com.che.plugin.zdb.server.connection.ZendDebugConnection.IEngineNotificationHandler;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.*;
import zend.com.che.plugin.zdb.server.utils.ZendDebugUtils;
import zend.com.che.plugin.zdb.server.variables.ZendDebugVariable;

import static zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.*;

/**
 * Zend debug session.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugSession implements IDebuggerDelegate, IEngineNotificationHandler {

	private static class StackVariablesExpression extends ZendDebugExpression {

		private final static String DUMP_VARIABLES_EXPRESSION = "eval('if (isset($this)) {$this;}; return get_defined_vars();')";

		/**
		 * Creates new current context expression.
		 */
		private StackVariablesExpression() {
			super(DUMP_VARIABLES_EXPRESSION);
		}

		@Override
		public IDebugExpression createChildExpression(String endName, String endRepresentation, Facet... facets) {
			endName = '$' + endName;
			if (ZendDebugUtils.isThis(endName))
				return new ZendDebugExpression(endName, KIND_THIS);
			else if (ZendDebugUtils.isSuperGlobal(endName))
				return new ZendDebugExpression(endName, KIND_SUPER_GLOBAL);
			else
				return new ZendDebugExpression(endName, KIND_LOCAL);
		}

	}

	private static final int SUPPORTED_PROTOCOL_ID = 2012121702;

	private final DebuggerCallback debuggerCallback;
	private final ZendDebugConnection connection;

	private final ZendDebugExpressionResolver expressionResolver;
	private List<Variable> stackVariables;
	private List<Breakpoint> breakpoints;
	private Map<Breakpoint, Integer> breakpointIds = new HashMap<>();

	public ZendDebugSession(int debugPort, DebuggerCallback debuggerCallback) {
		this.debuggerCallback = debuggerCallback;
		this.stackVariables = new ArrayList<>();
		this.connection = new ZendDebugConnection(this, debugPort);
		this.expressionResolver = new ZendDebugExpressionResolver(connection);
	}

	@Override
	public void handle(IDebugEngineNotification notification) {
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

	@Override
	public void start(StartAction action) throws DebuggerException {
		breakpoints = new ArrayList<>(action.getBreakpoints());
		connection.connect();
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
		connection.sendRequest(new StepOverRequest());
	}

	@Override
	public void stepInto(StepIntoAction action) throws DebuggerException {
		connection.sendRequest(new StepIntoRequest());
	}

	@Override
	public void stepOut(StepOutAction action) throws DebuggerException {
		connection.sendRequest(new StepOutRequest());
	}

	@Override
	public void resume(ResumeAction action) throws DebuggerException {
		connection.sendRequest(new GoRequest());
	}

	@Override
	public void disconnect() throws DebuggerException {
		connection.disconnect();
	}

	private void handleSessionStarted(SessionStartedNotification notification) {
		if (!sendSetProtocol()) {
			sendCloseSession();
			// TODO - log info here about unsupported protocol
		}
		sendAddBreakpointFiles();
		sendStartSession();
	}

	private void handleStartProcessFile(StartProcessFileNotification notification) {
		sendAddBreakpoints(notification.getFileName());
		sendContinueProcessFile();
	}

	private void handleReady(ReadyNotification notification) {
		String localFilePath = ZendDebugUtils.getLocalPath(notification.getFileName());
		VirtualFileEntry localFileEntry = ZendDebugUtils.getVirtualFileEntry(localFilePath);
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

	private boolean sendSetProtocol() {
		SetProtocolResponse response = connection.sendRequest(new SetProtocolRequest(SUPPORTED_PROTOCOL_ID));
		if (response != null && response.getProtocolID() < SUPPORTED_PROTOCOL_ID) {
			return false;
		}
		return true;
	}

	private void sendContinueProcessFile() {
		connection.sendNotification(new ContinueProcessFileNotification());
	}

	private void sendStartSession() {
		connection.sendRequest(new StartRequest());
	}

	private void sendGetVariables() {
		List<IDebugExpression> variables = new ArrayList<>();
		IDebugExpression stackVariablesExpression = new StackVariablesExpression();
		expressionResolver.resolve(stackVariablesExpression, 1);
		variables = stackVariablesExpression.getValue().getChildren();
		stackVariables.clear();
		for (IDebugExpression variable : variables) {
			stackVariables
					.add(new ZendDebugVariable(new VariablePathImpl(variable.getName()), variable, expressionResolver));
		}
	}

	private void sendAddBreakpointFiles() {
		Set<String> breakpointFiles = new HashSet<>();
		for (Breakpoint breakpoint : breakpoints) {
			String absolutePath = ZendDebugUtils.getAbsolutePath(breakpoint.getLocation().getResourcePath());
			breakpointFiles.add(absolutePath);
		}
		connection.sendRequest(new AddFilesRequest(breakpointFiles));
	}

	private void sendAddBreakpoints(String remoteFilePath) {
		String localFilePath = ZendDebugUtils.getLocalPath(remoteFilePath);
		List<Breakpoint> fileBreakpoints = new ArrayList<>();
		for (Breakpoint breakpoint : breakpoints) {
			if (breakpoint.getLocation().getResourcePath().equals(localFilePath)) {
				fileBreakpoints.add(breakpoint);
			}
		}
		for (Breakpoint breakpoint : fileBreakpoints) {
			AddBreakpointResponse response = connection.sendRequest(
					new AddBreakpointRequest(1, 2, breakpoint.getLocation().getLineNumber(), remoteFilePath));
			if (response != null && response.getStatus() == 0) {
				breakpointIds.put(breakpoint, response.getBreakpointID());
				// Send breakpoint activated event
				debuggerCallback.onEvent(new BreakpointActivatedEventImpl(breakpoint));
			}
		}
	}

	private void sendAddBreakpoint(Breakpoint breakpoint) {
		String remoteFilePath = ZendDebugUtils.getAbsolutePath(breakpoint.getLocation().getResourcePath());
		AddBreakpointResponse response = connection
				.sendRequest(new AddBreakpointRequest(1, 2, breakpoint.getLocation().getLineNumber(), remoteFilePath));
		if (response != null && response.getStatus() == 0) {
			breakpointIds.put(breakpoint, response.getBreakpointID());
			debuggerCallback.onEvent(new BreakpointActivatedEventImpl(breakpoint));
		}
	}

	private void sendDeleteBreakpoint(int breakpointId) {
		connection.sendRequest(new DeleteBreakpointRequest(breakpointId));
	}

	private void sendDeleteAllBreakpoints() {
		connection.sendRequest(new DeleteAllBreakpointsRequest());
	}

	private void sendCloseSession() {
		connection.sendRequest(new CloseSessionRequest());
	}

}
