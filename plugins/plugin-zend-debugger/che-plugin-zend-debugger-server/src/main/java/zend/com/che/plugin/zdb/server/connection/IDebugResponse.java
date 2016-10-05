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
 * Interface for Zend debug responses.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDebugResponse extends IDebugMessage {

	/**
	 * Set the response id.
	 */
	public void setID(int id);

	/**
	 * Return the response id.
	 */
	public int getID();

	/**
	 * Set the response status.
	 */
	public void setStatus(int status);

	/**
	 * Return the response status.
	 */
	public int getStatus();
	
}
