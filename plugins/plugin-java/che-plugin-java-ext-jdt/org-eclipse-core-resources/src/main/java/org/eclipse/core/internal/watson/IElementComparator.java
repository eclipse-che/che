/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.watson;

import org.eclipse.core.internal.dtree.IComparator;

/**
 * This interface allows clients of the element tree to specify
 * how element infos are compared, and thus how element tree deltas
 * are created.
 */
public interface IElementComparator extends IComparator {
	/**
	 * The kinds of changes
	 */
	public int K_NO_CHANGE = 0;
}
