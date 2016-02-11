/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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


/**
 * The context type for templates inside Java code.
 * The same class is used for several context types:
 * <dl>
 * <li>templates for all Java code locations</li>
 * <li>templates for member locations</li>
 * <li>templates for statement locations</li>
 * </dl>
 */
public class JavaContextType extends AbstractJavaContextType {

	/**
	 * The context type id for templates working on all Java code locations
	 */
	public static final String ID_ALL= "java"; //$NON-NLS-1$

	/**
	 * The context type id for templates working on member locations
	 */
	public static final String ID_MEMBERS= "java-members"; //$NON-NLS-1$

	/**
	 * The context type id for templates working on statement locations
	 */
	public static final String ID_STATEMENTS= "java-statements"; //$NON-NLS-1$


	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.template.java.AbstractJavaContextType#initializeContext(org.eclipse.jdt.internal.corext.template.java.JavaContext)
	 */
	@Override
	protected void initializeContext(JavaContext context) {
		if (!getId().equals(JavaContextType.ID_ALL)) { // a specific context must also allow the templates that work everywhere
			context.addCompatibleContextType(JavaContextType.ID_ALL);
		}
	}

}
