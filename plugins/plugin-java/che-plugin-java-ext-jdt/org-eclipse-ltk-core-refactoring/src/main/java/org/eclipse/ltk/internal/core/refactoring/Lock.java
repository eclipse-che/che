/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

/**
 * A simple lock object with a <em>done</em> flag.
 * 
 * @since 3.5
 */
public class Lock {
	/**
	 * <code>true</code> iff the operation is done.
	 */
	public boolean fDone;
}
