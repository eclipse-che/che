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
public abstract class AbstractMessage implements IDebugMessage {

	@Override
	public String getTransferEncoding() {
		return ENCODING;
	}

	@Override
	public void setTransferEncoding(String encoding) {
		// TODO - support user preferred encoding
	}

}
