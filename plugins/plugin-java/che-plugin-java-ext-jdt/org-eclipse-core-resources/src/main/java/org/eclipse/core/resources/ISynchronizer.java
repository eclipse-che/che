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
import org.eclipse.core.runtime.QualifiedName;

/**
 * A synchronizer which maintains a list of registered partners and, on behalf of
 * each partner, it keeps resource level synchronization information 
 * (a byte array). Sync info is saved only when the workspace is saved.
 * 
 * @see IWorkspace#getSynchronizer()
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISynchronizer {
	/**
	 * Visits the given resource and its descendents with the specified visitor
	 * if sync information for the given sync partner is found on the resource. If
	 * sync information for the given sync partner is not found on the resource,
	 * still visit the children of the resource if the depth specifies to do so.
	 *
	 * @param partner the sync partner name
	 * @param start the parent resource to start the visitation
	 * @param visitor the visitor to use when visiting the resources
	 * @param depth the depth to which members of this resource should be
	 *		visited.  One of <code>IResource.DEPTH_ZERO</code>, <code>IResource.DEPTH_ONE</code>,
	 *		or <code>IResource.DEPTH_INFINITE</code>.
	 * @exception CoreException if this operation fails. Reasons include:
	 *    <ul>
	 *    <li>The resource does not exist.</li>
	 *    <li><code>IResourceStatus.PARTNER_NOT_REGISTERED</code> 
	 The sync partner is not registered.</li>
	 *    </ul>
	 */
	public void accept(QualifiedName partner, IResource start, IResourceVisitor visitor, int depth) throws CoreException;

	/**
	 * Adds the named synchronization partner to this synchronizer's
	 * registry of partners. Once a partner's name has been registered, sync
	 * information can be set and retrieve on resources relative to the name.
	 * Adding a sync partner multiple times has no effect.
	 *
	 * @param partner the partner name to register
	 * @see #remove(QualifiedName)
	 */
	public void add(QualifiedName partner);

	/**
	 * Discards the named partner's synchronization information 
	 * associated with the specified resource and its descendents to the
	 * specified depth. 
	 *
	 * @param partner the sync partner name
	 * @param resource the resource
	 * @param depth the depth to which members of this resource should be
	 *		visited.  One of <code>IResource.DEPTH_ZERO</code>, <code>IResource.DEPTH_ONE</code>,
	 *		or <code>IResource.DEPTH_INFINITE</code>.
	 * @exception CoreException if this operation fails. Reasons include:
	 *    <ul>
	 *    <li>The resource does not exist.</li>
	 *    <li><code>IResourceStatus.PARTNER_NOT_REGISTERED</code> 
	 The sync partner is not registered.</li>
	 *    </ul>
	 */
	public void flushSyncInfo(QualifiedName partner, IResource resource, int depth) throws CoreException;

	/**
	 * Returns a list of synchronization partner names currently registered
	 * with this synchronizer. Returns an empty array if there are no
	 * registered sync partners.
	 *
	 * @return a list of sync partner names
	 */
	public QualifiedName[] getPartners();

	/**
	 * Returns the named sync partner's synchronization information for the given resource.
	 * Returns <code>null</code> if no information is found.
	 *
	 * @param partner the sync partner name
	 * @param resource the resource
	 * @return the synchronization information, or <code>null</code> if none
	 * @exception CoreException if this operation fails. Reasons include:
	 *    <ul>
	 *    <li><code>IResourceStatus.PARTNER_NOT_REGISTERED</code> 
	 The sync partner is not registered.</li>
	 *    </ul>
	 */
	public byte[] getSyncInfo(QualifiedName partner, IResource resource) throws CoreException;

	/**
	 * Removes the named synchronization partner from this synchronizer's
	 * registry.  Does nothing if the partner is not registered.
	 * This discards all sync information for the defunct partner.
	 * After a partner has been unregistered, sync information for it can no 
	 * longer be stored on resources.
	 *
	 * @param partner the partner name to remove from the registry
	 * @see #add(QualifiedName)
	 */
	public void remove(QualifiedName partner);

	/**
	 * Sets the named sync partner's synchronization information for the given resource.
	 * If the given info is non-<code>null</code> and the resource neither exists
	 * nor is a phantom, this method creates a phantom resource to hang on to the info.
	 * If the given info is <code>null</code>, any sync info for the resource stored by the
	 * given sync partner is discarded; in some cases, this may result in the deletion
	 * of a phantom resource if there is no more sync info to maintain for that resource.
	 * <p>
	 * Sync information is not stored on the workspace root. Attempts to set information
	 * on the root will be ignored.
	 * </p>
	 *
	 * @param partner the sync partner name
	 * @param resource the resource
	 * @param info the synchronization information, or <code>null</code>
	 * @exception CoreException if this operation fails. Reasons include:
	 *    <ul>
	 *    <li><code>IResourceStatus.PARTNER_NOT_REGISTERED</code> 
	 The sync partner is not registered.</li>
	 *    </ul>
	 */
	public void setSyncInfo(QualifiedName partner, IResource resource, byte[] info) throws CoreException;
}
