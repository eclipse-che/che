/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.resources;

import org.eclipse.core.runtime.IPath;

/**
 * Describes a change in a path variable. The change may denote that a
 * variable has been created, deleted or had its value changed.
 * 
 * @since 2.1
 * @see IPathVariableChangeListener
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IPathVariableChangeEvent {

	/** Event type constant (value = 1) that denotes a value change . */
	public final static int VARIABLE_CHANGED = 1;

	/** Event type constant (value = 2) that denotes a variable creation. */
	public final static int VARIABLE_CREATED = 2;

	/** Event type constant (value = 3) that denotes a variable deletion. */
	public final static int VARIABLE_DELETED = 3;

	/**
	 * Returns the variable's current value. If the event type is
	 * <code>VARIABLE_CHANGED</code> then it is the new value, if the event
	 * type is <code>VARIABLE_CREATED</code> then it is the new value, or
	 * if the event type is <code>VARIABLE_DELETED</code> then it will
	 * be <code>null</code>.
	 * 
	 * @return the variable's current value, or <code>null</code>
	 */
	public IPath getValue();

	/**
	 * Returns the affected variable's name.
	 * 
	 * @return the affected variable's name
	 */
	public String getVariableName();

	/**
	 * Returns an object identifying the source of this event.
	 *
	 * @return an object identifying the source of this event 
	 * @see java.util.EventObject
	 */
	public Object getSource();

	/**
	 * Returns the type of event being reported.
	 *
	 * @return one of the event type constants
	 * @see #VARIABLE_CHANGED
	 * @see #VARIABLE_CREATED
	 * @see #VARIABLE_DELETED
	 */
	public int getType();

}
