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
 * Common Zend debug message interface.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDebugMessage {

	// UTF-8 only for now (preference in the future?)
	public static final String ENCODING = "UTF-8";
	
	/**
	 * Return unique type of this debug message
	 * 
	 * @return message type
	 */
	public int getType();

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
