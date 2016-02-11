/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * A participant descriptor filter allows clients to provide
 * additional filters on participant selection.
 *
 * @since 3.2
 */
public interface IParticipantDescriptorFilter {

	/** A constant indicating a param element (value: <code>param</code>) */
	public static final String PARAM= "param"; //$NON-NLS-1$

	/** A constant indicating a name attribute (value: <code>name</code>) */
	public static final String NAME= "name"; //$NON-NLS-1$

	/** A constant indicating a value attribute (value: <code>value</code>) */
	public static final String VALUE= "value";  //$NON-NLS-1$

	/**
     * Returns whether the given element makes it through this filter.
	 *
     * @param element the configuration element describing the refactoring
     *  participant
	 * @param status a RefactoringStatus to optionally add warning messages if the participant
	 * 	was not selected
     *
     * @return <code>true</code> if element is included, and
     *  <code>false</code> if excluded
	 */
	public boolean select(IConfigurationElement element, RefactoringStatus status);
}
