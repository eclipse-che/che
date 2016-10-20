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
package zend.com.che.plugin.zdb.server;

import java.util.List;

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
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zend.com.che.plugin.zdb.server.connection.ZendDbgSession;
import zend.com.che.plugin.zdb.server.connection.ZendDbgSessionSettings;

/**
 * Zend Debugger implementation.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugger implements Debugger {
	public static final Logger LOG = LoggerFactory.getLogger(ZendDebugger.class);
	private final ZendDbgSessionSettings debuggerSettings;
	private final DebuggerCallback debuggerCallback;
	private IDbgDelegate debugDelegate;

	ZendDebugger(ZendDbgSessionSettings debuggerSettings, DebuggerCallback debuggerCallback) throws DebuggerException {
		this.debuggerSettings = debuggerSettings;
		this.debuggerCallback = debuggerCallback;
		connect();
	}

	private void connect() throws DebuggerException {
		this.debugDelegate = new ZendDbgSession(debuggerSettings, debuggerCallback);
		LOG.debug("Connect {}:{}", debuggerSettings.getClientHostIP(), debuggerSettings.getDebugPort());
	}

	@Override
	public DebuggerInfo getInfo() throws DebuggerException {
		return new DebuggerInfoImpl("", debuggerSettings.getDebugPort(), "Zend Debugger", "", 0, null);
	}

	@Override
	public void start(StartAction action) throws DebuggerException {
		debugDelegate.start(action);
	}

	@Override
	public void addBreakpoint(Breakpoint breakpoint) throws DebuggerException {
		debugDelegate.addBreakpoint(breakpoint);
	}

	@Override
	public void deleteBreakpoint(Location location) throws DebuggerException {
		debugDelegate.deleteBreakpoint(location);
	}

	@Override
	public void deleteAllBreakpoints() throws DebuggerException {
		debugDelegate.deleteAllBreakpoints();
	}

	@Override
	public List<Breakpoint> getAllBreakpoints() throws DebuggerException {
		return debugDelegate.getAllBreakpoints();
	}

	@Override
	public SimpleValue getValue(VariablePath variablePath) throws DebuggerException {
		return debugDelegate.getValue(variablePath);
	}

	@Override
	public void setValue(Variable variable) throws DebuggerException {
		debugDelegate.setValue(variable);
	}

	@Override
	public String evaluate(String expression) throws DebuggerException {
		return debugDelegate.evaluate(expression);
	}

	@Override
	public void stepOver(StepOverAction action) throws DebuggerException {
		debugDelegate.stepOver(action);
	}

	@Override
	public void stepInto(StepIntoAction action) throws DebuggerException {
		debugDelegate.stepInto(action);
	}

	@Override
	public void stepOut(StepOutAction action) throws DebuggerException {
		debugDelegate.stepOut(action);
	}

	@Override
	public void resume(ResumeAction action) throws DebuggerException {
		debugDelegate.resume(action);
	}

	@Override
	public StackFrameDump dumpStackFrame() throws DebuggerException {
		return debugDelegate.dumpStackFrame();
	}

	@Override
	public void disconnect() throws DebuggerException {
		debugDelegate.disconnect();
	}

	@Override
	public String toString() {
		return "ZendDebugger [clientHostIP=" + debuggerSettings.getClientHostIP() + ", debugPort="
				+ debuggerSettings.getDebugPort() + ", useSsslEncryption=" + debuggerSettings.isUseSsslEncryption()
				+ "]";
	}

}
