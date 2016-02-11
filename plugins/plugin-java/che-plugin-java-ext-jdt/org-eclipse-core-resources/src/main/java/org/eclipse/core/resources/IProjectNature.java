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

import org.eclipse.core.runtime.CoreException;

/**
 * Interface for project nature runtime classes.
 * It can configure a project with the project nature, or de-configure it.
 * When a project is configured with a project nature, this is
 * recorded in the list of project natures on the project.
 * Individual project natures may expose a more specific runtime type,
 * with additional API for manipulating the project in a
 * nature-specific way.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see IProject#getNature(String)
 * @see IProject#hasNature(String)
 * @see IProjectDescription#getNatureIds()
 * @see IProjectDescription#hasNature(String)
 * @see IProjectDescription#setNatureIds(String[])
 */
public interface IProjectNature {
	/** 
	 * Configures this nature for its project. This is called by the workspace 
	 * when natures are added to the project using <code>IProject.setDescription</code>
	 * and should not be called directly by clients.  The nature extension 
	 * id is added to the list of natures before this method is called,
	 * and need not be added here.
	 * 
	 * Exceptions thrown by this method will be propagated back to the caller
	 * of <code>IProject.setDescription</code>, but the nature will remain in
	 * the project description.
	 *
	 * @exception CoreException if this method fails.
	 */
	public void configure() throws CoreException;

	/** 
	 * De-configures this nature for its project.  This is called by the workspace 
	 * when natures are removed from the project using 
	 * <code>IProject.setDescription</code> and should not be called directly by 
	 * clients.  The nature extension id is removed from the list of natures before 
	 * this method is called, and need not be removed here.
	 * 
	 * Exceptions thrown by this method will be propagated back to the caller
	 * of <code>IProject.setDescription</code>, but the nature will still be 
	 * removed from the project description.
	 * *
	 * @exception CoreException if this method fails. 
	 */
	public void deconfigure() throws CoreException;

	/** 
	 * Returns the project to which this project nature applies.
	 *
	 * @return the project handle
	 */
	public IProject getProject();

	/**
	 * Sets the project to which this nature applies.
	 * Used when instantiating this project nature runtime.
	 * This is called by <code>IProject.create()</code> or
	 * <code>IProject.setDescription()</code>
	 * and should not be called directly by clients.
	 *
	 * @param project the project to which this nature applies
	 */
	public void setProject(IProject project);
}
