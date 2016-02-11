/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.template.java;

import org.eclipse.jdt.internal.corext.template.java.CompilationUnitCompletion.Variable;

/**
 * Resolves template variables to a local variable that is assignment-compatible with the variable
 * instance' class parameter.
 *
 * @since 3.3
 */
public class LocalVarResolver extends AbstractVariableResolver {

	/**
	 * Default ctor for instantiation by the extension point.
	 */
	public LocalVarResolver() {
		this("java.lang.Object"); //$NON-NLS-1$
	}

	LocalVarResolver(String defaultType) {
		super(defaultType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.template.java.AbstractVariableResolver#getVisibleVariables(java.lang.String, org.eclipse.jdt.internal.corext.template.java.JavaContext)
	 */
	@Override
	protected Variable[] getVisibleVariables(String type, JavaContext context) {
		return context.getLocalVariables(type);
	}

}
