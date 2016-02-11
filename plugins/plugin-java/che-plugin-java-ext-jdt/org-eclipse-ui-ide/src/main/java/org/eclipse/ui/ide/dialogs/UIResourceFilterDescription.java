/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor)- initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.ide.dialogs;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.runtime.IPath;

/**
 * @since 3.6
 */
public abstract class UIResourceFilterDescription {
	/**
	 * @return the filter path
	 */
	abstract public IPath getPath();
	/**
	 * @return the project
	 */
	abstract public IProject getProject();
	/**
	 * @return the filter type
	 */
	abstract public int getType();
	/**
	 * @return the description
	 */
	abstract public FileInfoMatcherDescription getFileInfoMatcherDescription();
	
	/**
	 * @param iResourceFilterDescription
	 * @return a UIResourceFilterDescription
	 */
	public static UIResourceFilterDescription wrap(
			final IResourceFilterDescription iResourceFilterDescription) {
		return new UIResourceFilterDescription() {
			public FileInfoMatcherDescription getFileInfoMatcherDescription() {
				return iResourceFilterDescription.getFileInfoMatcherDescription();
			}
			public IPath getPath() {
				return iResourceFilterDescription.getResource().getProjectRelativePath();
			}
	
			public IProject getProject() {
				return iResourceFilterDescription.getResource().getProject();
			}
	
			public int getType() {
				return iResourceFilterDescription.getType();
			}
		};
	}
}