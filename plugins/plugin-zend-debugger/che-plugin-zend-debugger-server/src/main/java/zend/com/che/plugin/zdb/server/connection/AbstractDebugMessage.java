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

/**
 * Abstract Zend debug message.
 * 
 * @author Bartlomiej Laczkowski
 */
public abstract class AbstractDebugMessage implements IDebugMessage {

	private String fEncoding;

	public AbstractDebugMessage() {}
	
	@Override
	public String getTransferEncoding() {
		return fEncoding;
	}

	@Override
	public void setTransferEncoding(String encoding) {
		fEncoding = encoding;
	}

	@Override
	public String toString() {
		return new StringBuilder(this.getClass().getName().replaceFirst(".*\\.", "")).append(" [ID=").append(getType()) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.append(']')
				.toString();
	}
	
}
