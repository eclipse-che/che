/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.php.zdb.server.variables;

import java.util.List;

/**
 * Common interface for Zend dbg expressions that are describing stack variables.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDbgVariable extends IDbgExpression {

	// Specifies concrete type of children
	@Override
	List<IDbgVariable> getChildren();
	
	/**
	 * Returns name of the variable.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Returns path/chain to variable.
	 * 
	 * @return path/chain to variable
	 */
	List<String> getPath();

}
