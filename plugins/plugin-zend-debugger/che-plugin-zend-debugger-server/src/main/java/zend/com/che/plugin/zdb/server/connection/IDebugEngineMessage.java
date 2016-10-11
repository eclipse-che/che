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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Zend debug engine engine side message.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDebugEngineMessage extends IDebugMessage {

	/**
	 * De-serialize this debug message from an input stream
	 * 
	 * @param in
	 *            input stream this message is going to be read from
	 */
	public void deserialize(DataInputStream in) throws IOException;
	
}
