/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids: sdavids@gmx.de - see bug 25376
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.template.java;

import org.eclipse.jdt.internal.corext.template.java.CompilationUnitCompletion.Variable;


public class VarResolver extends AbstractVariableResolver {

	/**
	 * Default ctor for instantiation by the extension point.
	 */
	public VarResolver() {
		this("java.lang.Object"); //$NON-NLS-1$
	}

	VarResolver(String defaultType) {
		super(defaultType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.template.java.AbstractVariableResolver#getVisibleVariables(java.lang.String, org.eclipse.jdt.internal.corext.template.java.JavaContext)
	 */
	@Override
	protected Variable[] getVisibleVariables(String type, JavaContext context) {
		Variable[] localVariables= context.getLocalVariables(type);
		Variable[] fields= context.getFields(type);

		Variable[] result= new Variable[localVariables.length + fields.length];

		System.arraycopy(localVariables, 0, result, 0, localVariables.length);
		System.arraycopy(fields, 0, result, localVariables.length, fields.length);

		return result;
	}
}
