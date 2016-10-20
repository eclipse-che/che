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

public class ZendDbgSessionSettings {

	private final int debugPort;
	private final String clientHostIP;
	private final boolean breakAtFirstLine;
	private final boolean useSsslEncryption;
	
	public ZendDbgSessionSettings(int debugPort, String clientHostIP, boolean breakAtFirstLine,  boolean useSsslEncryption) {
		super();
		this.debugPort = debugPort;
		this.clientHostIP = clientHostIP;
		this.breakAtFirstLine = breakAtFirstLine;
		this.useSsslEncryption = useSsslEncryption;
	}

	public int getDebugPort() {
		return debugPort;
	}

	public String getClientHostIP() {
		return clientHostIP;
	}
	
	public boolean isBreakAtFirstLine() {
		return breakAtFirstLine;
	}

	public boolean isUseSsslEncryption() {
		return useSsslEncryption;
	}
	
}
