/*******************************************************************************
 * Copyright (c) 2008, 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM - ongoing development
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.*;

/**
 * A description of a resource filter.
 * 
 * A filter determines which file system objects will be visible when a local refresh is 
 * performed for an IContainer.
 *
 * @see IContainer#getFilters()
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.6
 */
public interface IResourceFilterDescription {

	/*====================================================================
	 * Constants defining which members are wanted:
	 *====================================================================*/

	/**
	 * Flag for resource filters indicating that the filter list includes only 
	 * the files matching the filters. All INCLUDE_ONLY filters are applied to
	 * the resource list with an logical OR operation.
	 */
	public static final int INCLUDE_ONLY = 1;

	/**
	 * Flag for resource filters indicating that the filter list excludes all
	 * the files matching the filters.  All EXCLUDE_ALL filters are applied to
	 * the resource list with an logical AND operation.
	 */
	public static final int EXCLUDE_ALL = 2;

	/**
	 * Flag for resource filters indicating that this filter applies to files.
	 */
	public static final int FILES = 4;

	/**
	 * Flag for resource filters indicating that this filter applies to folders.
	 */
	public static final int FOLDERS = 8;

	/**
	 * Flag for resource filters indicating that the container children of the
	 * path inherit from this filter as well.
	 */
	public static final int INHERITABLE = 16;

	/**
	 * Returns the description of the file info matcher corresponding to this resource
	 * filter.
	 * @return the file info matcher description for this resource filter
	 */
	public FileInfoMatcherDescription getFileInfoMatcherDescription();

	/**
	 * Return the resource towards which this filter is set.
	 * 
	 * @return the resource towards which this filter is set
	 */
	public IResource getResource();

	/**
	 * Return the filter type, either INCLUDE_ONLY or EXCLUDE_ALL
	 * 
	 * @return (INCLUDE_ONLY or EXCLUDE_ALL) and/or INHERITABLE
	 */
	public int getType();
	
	/**
	 * Deletes this filter description from its associated resource.
	 * <p>
	 * The {@link IResource#BACKGROUND_REFRESH} update flag controls when
	 * changes to the resource hierarchy under this container resulting from the filter 
	 * removal take effect. If this flag is specified, the resource hierarchy is updated in a 
	 * separate thread after this method returns. If the flag is not specified, any resource 
	 * changes resulting from the filter removal will occur before this method returns.
	 * </p>
	 * <p> 
	 * This operation changes resources; these changes will be reported
	 * in a subsequent resource change event that will include an indication 
	 * of any resources that have been added as a result of the filter removal.
	 * </p>
	 * <p>
	 * This operation is long-running; progress and cancellation are provided
	 * by the given progress monitor. 
	 * </p>
	 * 
	 * @param updateFlags bit-wise or of update flag constants
	 *   ({@link IResource#BACKGROUND_REFRESH})
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting is not desired
	 * @exception CoreException if this filter could not be removed. Reasons include:
	 * <ul>
	 * <li> Resource changes are disallowed during certain types of resource change 
	 *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
	 * </ul>
	 * @exception OperationCanceledException if the operation is canceled. 
	 * Cancelation can occur even if no progress monitor is provided.
	 * @see IContainer#getFilters()
	 * @see IContainer#createFilter(int, FileInfoMatcherDescription, int, IProgressMonitor)
	 */
	public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException;

}