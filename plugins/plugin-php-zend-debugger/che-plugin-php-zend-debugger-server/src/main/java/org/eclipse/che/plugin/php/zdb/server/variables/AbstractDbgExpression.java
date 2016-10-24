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

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract implementation of Zend dbg expression.
 * 
 * @author Bartlomiej Laczkowski
 */
public abstract class AbstractDbgExpression implements IDbgExpression {

	private final ZendDbgExpressionResolver resolver;
	private final String statement;
	private final Set<Facet> facets = new HashSet<Facet>();
	private DataType dataType = DataType.PHP_UNINITIALIZED;
	private String value = "null";
	private int childrenCount = 0;
	
	public AbstractDbgExpression(ZendDbgExpressionResolver resolver, String statement, Facet... facets) {
		this.resolver = resolver;
		this.statement = statement;
		addFacets(facets);
	}
	
	@Override
	public boolean hasFacet(Facet facet) {
		return facets.contains(facet);
	}

	@Override
	public void addFacets(Facet... facets) {
		for (Facet facet : facets)
			this.facets.add(facet);
	}

	@Override
	public String getStatement() {
		return statement;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public int getChildrenCount() {
		return childrenCount;
	}
	
	@Override
	public DataType getDataType() {
		return dataType;
	}
	
	@Override
	public void resolve() {
		resolver.resolve(this, 1);
	}
	
	protected ZendDbgExpressionResolver getResolver() {
		return resolver;
	}
	
	protected void setValue(String value) {
		this.value = value;
	}
	
	protected void setChildrenCount(int childrenCount) {
		this.childrenCount = childrenCount;
	}
	
	protected void setDataType(DataType dataType) {
		this.dataType = dataType;
	}
	
	/**
	 * Implementors should create and return child expression element.
	 * 
	 * @param statement
	 * @param facets
	 * @return child expression element
	 */
	protected abstract AbstractDbgExpression createChild(String statement, Facet... facets);
	
}
