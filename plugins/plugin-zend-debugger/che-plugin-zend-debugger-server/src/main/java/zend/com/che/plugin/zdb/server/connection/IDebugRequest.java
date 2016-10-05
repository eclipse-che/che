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
 * Interface for Zend debug requests.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDebugRequest extends IDebugMessage {

	/**
	 * Set the request id.
	 */
	public void setID(int id);

	/**
	 * Return the request id.
	 */
	public int getID();
	
}
