/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

/**
 * Descriptor of a change object. These descriptor objects may be used to
 * describe the effect of a {@link Change}. Subclasses may provide more
 * specific information about the represented change.
 * <p>
 * Note: this class is indented to be subclassed by clients to provide
 * specialized descriptors for particular changes.
 * </p>
 *
 * @since 3.2
 */
public abstract class ChangeDescriptor {

	/**
	 * Creates a new change descriptor.
	 */
	protected ChangeDescriptor() {
		// Do nothing
	}
}