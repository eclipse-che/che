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
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Common Zend debug message interface.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDebugMessage extends IDebugMessageType {

	/**
	 * Return unique type of this debug message
	 * 
	 * @return message type
	 */
	public int getType();

	/**
	 * Serialize this debug message to an output stream
	 * 
	 * @param out
	 *            output stream this message is going to be written to
	 */
	public void serialize(DataOutputStream out) throws IOException;

	/**
	 * De-serialize this debug message from an input stream
	 * 
	 * @param in
	 *            input stream this message is going to be read from
	 */
	public void deserialize(DataInputStream in) throws IOException;

	/**
	 * Sets the debug transfer encoding for this message
	 * 
	 * @param String
	 *            transfer encoding
	 */
	public void setTransferEncoding(String encoding);

	/**
	 * Returns current debug transfer encoding for this message
	 * 
	 * @return String transfer encoding
	 */
	public String getTransferEncoding();
	
}
