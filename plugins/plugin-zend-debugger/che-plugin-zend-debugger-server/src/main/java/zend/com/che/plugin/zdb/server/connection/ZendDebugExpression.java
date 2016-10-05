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

import static zend.com.che.plugin.zdb.server.connection.IDebugDataFacet.Facet.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Zend debug expression implementation.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugExpression implements IDebugExpression {

	private String[] name;
	private String fullName;
	private IDebugExpressionValue expressionValue;
	private Set<Facet> facets = new HashSet<Facet>();

	/**
	 * Creates new default expression.
	 * 
	 * @param expression
	 */
	public ZendDebugExpression(String expression) {
		name = new String[] { expression.trim() };
		fullName = expression;
		setValue(null);
	}

	/**
	 * Creates new default expression.
	 * 
	 * @param expression
	 * @param facets
	 */
	public ZendDebugExpression(String expression, Facet... facets) {
		name = new String[] { expression.trim() };
		fullName = expression;
		setValue(null);
		addFacets(facets);
	}

	/**
	 * Creates new default expression.
	 * 
	 * @param parent
	 * @param name
	 * @param representation
	 * @param facets
	 */
	protected ZendDebugExpression(IDebugExpression parent, String name, String representation, Facet... facets) {
		String[] parentPath = parent.getPath();
		this.name = new String[parentPath.length + 1];
		System.arraycopy(parentPath, 0, this.name, 0, parentPath.length);
		this.name[parentPath.length] = name;
		fullName = parent.getFullName() + representation;
		setValue(null);
		setChildFacets(name, facets);
		addFacets(facets);
	}

	protected void setChildFacets(String name, Facet... facets) {
		for (Facet facet : facets) {
			if (facet == KIND_OBJECT_MEMBER) {
				if (name.startsWith("*::")) { //$NON-NLS-1$
					addFacets(MOD_PROTECTED);
				} else if (name.contains("::")) { //$NON-NLS-1$
					addFacets(MOD_PRIVATE);
				} else {
					addFacets(MOD_PUBLIC);
				}
			}
		}
	}

	@Override
	public void addFacets(Facet... facets) {
		for (Facet facet : facets)
			this.facets.add(facet);
	}

	@Override
	public IDebugExpression createChildExpression(String endName, String endRepresentation, Facet... facets) {
		return new ZendDebugExpression(this, endName, endRepresentation, facets);
	}

	@Override
	public String[] getPath() {
		return name;
	}

	@Override
	public String getName() {
		return name[name.length - 1];
	}

	@Override
	public String getFullName() {
		return fullName;
	}

	@Override
	public IDebugExpressionValue getValue() {
		return expressionValue;
	}

	@Override
	public boolean hasFacet(Facet facet) {
		return facets.contains(facet);
	}

	@Override
	public void setValue(IDebugExpressionValue value) {
		if (value == null) {
			value = ZendDebugExpressionValue.NULL_VALUE;
		}
		this.expressionValue = value;
	}

	@Override
	public String toString() {
		return getName() + " = " + getValue().getValue(); //$NON-NLS-1$
	}

}