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
import static zend.com.che.plugin.zdb.server.connection.IDebugMessageType.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.action.ResumeAction;
import org.eclipse.che.api.debug.shared.model.action.StepIntoAction;
import org.eclipse.che.api.debug.shared.model.action.StepOutAction;
import org.eclipse.che.api.debug.shared.model.action.StepOverAction;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.StackFrameDumpImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.api.debug.shared.model.impl.event.SuspendEventImpl;
import org.eclipse.che.api.debugger.server.Debugger.DebuggerCallback;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.api.vfs.Path;

import zend.com.che.plugin.zdb.server.IDebuggerDelegate;
import zend.com.che.plugin.zdb.server.connection.ZendDebugNotifications.ReadyNotification;
import zend.com.che.plugin.zdb.server.connection.ZendDebugNotifications.SessionStartedNotification;
import zend.com.che.plugin.zdb.server.connection.ZendDebugNotifications.StartProcessFileNotification;
import zend.com.che.plugin.zdb.server.connection.ZendDebugRequests.CloseSessionRequest;
import zend.com.che.plugin.zdb.server.connection.ZendDebugRequests.ContinueProcessFileRequest;
import zend.com.che.plugin.zdb.server.connection.ZendDebugRequests.GoRequest;
import zend.com.che.plugin.zdb.server.connection.ZendDebugRequests.SetProtocolRequest;
import zend.com.che.plugin.zdb.server.connection.ZendDebugRequests.StartRequest;
import zend.com.che.plugin.zdb.server.connection.ZendDebugRequests.StepIntoRequest;
import zend.com.che.plugin.zdb.server.connection.ZendDebugRequests.StepOutRequest;
import zend.com.che.plugin.zdb.server.connection.ZendDebugRequests.StepOverRequest;
import zend.com.che.plugin.zdb.server.connection.ZendDebugResponses.SetProtocolResponse;
import zend.com.che.plugin.zdb.server.utils.ZendDebugUtils;
import zend.com.che.plugin.zdb.server.variables.ZendDebugVariable;

/**
 * Zend debug session.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugSession implements IDebuggerDelegate {

	private static class DumpVariablesExpression extends ZendDebugExpression {

		private final static String DUMP_VARIABLES_EXPRESSION = "eval('if (isset($this)) {$this;}; return get_defined_vars();')";

		/**
		 * Creates new current context expression.
		 */
		private DumpVariablesExpression() {
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
	private ZendDebugExpressionResolver debugExpressionResolver;
	private ZendDebugConnection debugConnection;
	private List<Variable> debugStackVariables;

	public ZendDebugSession(int debugPort, DebuggerCallback debuggerCallback) {
		this.debuggerCallback = debuggerCallback;
		this.debugStackVariables = new ArrayList<>();
		(new ZendDebugDaemon(this, debugPort)).startListen();
	}

	void connect(Socket socket) {
		this.debugConnection = new ZendDebugConnection(this, socket);
		this.debugExpressionResolver = new ZendDebugExpressionResolver(debugConnection);
	}

	void handle(IDebugMessage message) {
		switch (message.getType()) {
		case NOTIFICATION_SESSION_STARTED: {
			handleSessionStarted((SessionStartedNotification) message);
			break;
		}
		case NOTIFICATION_START_PROCESS_FILE: {
			handleStartProcessFile((StartProcessFileNotification) message);
			break;
		}
		case NOTIFICATION_READY: {
			handleReady((ReadyNotification) message);
			break;
		}
		default:
			break;
		}
	}

	@Override
	public StackFrameDump dumpStackFrame() {
		fetchVariables();
		return new StackFrameDumpImpl(Collections.emptyList(), debugStackVariables);
	}

	@Override
	public SimpleValue getValue(VariablePath variablePath) {
		List<Variable> currentVariables = debugStackVariables;
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
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteBreakpoint(Location location) throws DebuggerException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAllBreakpoints() throws DebuggerException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(Variable variable) throws DebuggerException {
		// TODO Auto-generated method stub
	}

	@Override
	public String evaluate(String expression) throws DebuggerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stepOver(StepOverAction action) throws DebuggerException {
		StepOverRequest stepOverRequest = (StepOverRequest) ZendDebugMessageFactory.create(REQUEST_STEP_OVER);
		debugConnection.syncRequest(stepOverRequest);
	}

	@Override
	public void stepInto(StepIntoAction action) throws DebuggerException {
		StepIntoRequest stepIntoRequest = (StepIntoRequest) ZendDebugMessageFactory.create(REQUEST_STEP_INTO);
		debugConnection.syncRequest(stepIntoRequest);
	}

	@Override
	public void stepOut(StepOutAction action) throws DebuggerException {
		StepOutRequest stepOutRequest = (StepOutRequest) ZendDebugMessageFactory.create(REQUEST_STEP_OUT);
		debugConnection.syncRequest(stepOutRequest);
	}

	@Override
	public void resume(ResumeAction action) throws DebuggerException {
		GoRequest goRequest = (GoRequest) ZendDebugMessageFactory.create(REQUEST_GO);
		debugConnection.syncRequest(goRequest);
	}

	@Override
	public void disconnect() throws DebuggerException {
		CloseSessionRequest closeSessionRequest = (CloseSessionRequest) ZendDebugMessageFactory.create(REQUEST_CLOSE_SESSION);
		debugConnection.asyncRequest(closeSessionRequest);
	}

	private void handleSessionStarted(SessionStartedNotification notification) {
		// Nothing special to do with session started notification for now...
		// Check if debugger protocol can be supported
		SetProtocolRequest setProtocolRequest = (SetProtocolRequest) ZendDebugMessageFactory
				.create(REQUEST_SET_PROTOCOL);
		setProtocolRequest.setProtocolID(SUPPORTED_PROTOCOL_ID);
		IDebugResponse setProtocolResponse = debugConnection.syncRequest(setProtocolRequest);
		if (setProtocolResponse != null && setProtocolResponse instanceof SetProtocolResponse) {
			int responseProtocolId = ((SetProtocolResponse) setProtocolResponse).getProtocolID();
			if (responseProtocolId < SUPPORTED_PROTOCOL_ID) {
				// TODO - throw exception here...
			}
		}
		StartRequest startRequest = (StartRequest) ZendDebugMessageFactory.create(REQUEST_START);
		debugConnection.asyncRequest(startRequest);
	}

	private void handleStartProcessFile(StartProcessFileNotification notification) {
		// TODO - Find breakpoints in corresponding local files and send to
		// engine
		// Send continue file processing request
		ContinueProcessFileRequest continueProcessFileNotification = (ContinueProcessFileRequest) ZendDebugMessageFactory
				.create(REQUEST_CONTINUE_PROCESS_FILE);
		debugConnection.asyncRequest(continueProcessFileNotification);
	}

	private void handleReady(ReadyNotification notification) {
		Path localFilePath = ZendDebugUtils.getLocalFilePath(notification.getFileName());
		String projectPath = localFilePath.element(0);
		String target = localFilePath.getName();
		int lineNumber = notification.getLineNumber();
		Location location = new LocationImpl(target, lineNumber, localFilePath.toString(), false, -1, projectPath);
		// Send suspend event
		debuggerCallback.onEvent(new SuspendEventImpl(location));
	}

	private void fetchVariables() {
		List<IDebugExpression> zendVariables = new ArrayList<>();
		IDebugExpression zendVariablesExpression = new DumpVariablesExpression();
		debugExpressionResolver.resolve(zendVariablesExpression, 1);
		zendVariables = zendVariablesExpression.getValue().getChildren();
		debugStackVariables.clear();
		for (IDebugExpression zendVariable : zendVariables) {
			debugStackVariables.add(new ZendDebugVariable(new VariablePathImpl(zendVariable.getName()), zendVariable,
					debugExpressionResolver));
		}
	}

}
