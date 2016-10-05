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

import java.net.Socket;

import zend.com.che.plugin.zdb.server.ZendDebuggerFactory;

/**
 * Zend debug daemon thread.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugDaemon extends AbstractDebugDaemon {

	private int debugPort;
	private ZendDebugSession debugSession;

	public ZendDebugDaemon(ZendDebugSession debugSession, int debugPort) {
		this.debugSession = debugSession;
		this.debugPort = debugPort;
		init();
	}

	@Override
	public int getReceiverPort() {
		return debugPort;
	}

	@Override
	public String getDebuggerID() {
		return ZendDebuggerFactory.TYPE;
	}

	public boolean isDebuggerDaemon() {
		return true;
	}

	@Override
	protected synchronized void startConnection(Socket socket) {
		debugSession.connect(socket);
	}

}
