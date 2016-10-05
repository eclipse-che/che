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

import java.util.List;

/**
 * Interface for Zend debug expression value.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDebugExpressionValue extends IDebugDataType {

	/**
	 * Returns expression value string.
	 * 
	 * @return expression value string
	 */
	public String getValue();

	/**
	 * Returns expression value children.
	 * 
	 * @return expression value children
	 */
	public List<IDebugExpression> getChildren();

	/**
	 * Returns number of existing children.
	 * 
	 * @return number of existing children
	 */
	public int getChildrenCount();
	
}
