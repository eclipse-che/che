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
 * Interface for Zend debug expressions.
 * 
 * @author Bartlomiej Laczkowski
 */
public interface IDebugExpression extends IDebugDataFacet {

	/**
	 * Returns path to expression element.
	 * 
	 * @return path to expression element
	 */
	public String[] getPath();

	/**
	 * Returns expression element name.
	 * 
	 * @return expression element name
	 */
	public String getName();

	/**
	 * Returns expression element full name.
	 * 
	 * @return expression element full name
	 */
	public String getFullName();

	/**
	 * Sets expression element value.
	 * 
	 * @param value
	 */
	public void setValue(IDebugExpressionValue value);

	/**
	 * Returns expression element value.
	 * 
	 * @return expression element value
	 */
	public IDebugExpressionValue getValue();

	/**
	 * Creates expression child element.
	 * 
	 * @param name
	 * @param representation
	 * @param facet
	 * @return created expression child element.
	 */
	public IDebugExpression createChildExpression(String name, String representation, Facet... facet);

}