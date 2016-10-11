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

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Zend debug client side message.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDebugClientMessage extends IDebugMessage {

	/**
	 * Serialize this debug message to an output stream
	 * 
	 * @param out
	 *            output stream this message is going to be written to
	 */
	public void serialize(DataOutputStream out) throws IOException;
	
}
