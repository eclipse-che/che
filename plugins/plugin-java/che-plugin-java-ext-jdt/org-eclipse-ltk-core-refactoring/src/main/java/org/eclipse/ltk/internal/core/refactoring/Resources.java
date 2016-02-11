/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;

public class Resources {

	private Resources() {
	}

	/**
	 * Checks if the given resource is in sync with the underlying file system.
	 *
	 * @param resource the resource to be checked
	 * @return IStatus status describing the check's result. If <code>status.
	 * isOK()</code> returns <code>true</code> then the resource is in sync
	 */
	public static IStatus checkInSync(IResource resource) {
		return checkInSync(new IResource[] {resource});
	}

	/**
	 * Checks if the given resources are in sync with the underlying file
	 * system.
	 *
	 * @param resources the resources to be checked
	 * @return IStatus status describing the check's result. If <code>status.
	 *  isOK() </code> returns <code>true</code> then the resources are in sync
	 */
	public static IStatus checkInSync(IResource[] resources) {
		IStatus result= null;
		for (int i= 0; i < resources.length; i++) {
			IResource resource= resources[i];
			if (!resource.isSynchronized(IResource.DEPTH_INFINITE)) {
				result= addOutOfSync(result, resource);
			}
		}
		if (result != null)
			return result;
		return Status.OK_STATUS;
	}

	/**
	 * Makes the given resource committable. Committable means that it is
	 * writeable and that its content hasn't changed by calling
	 * <code>validateEdit</code> for the given resource on <tt>IWorkspace</tt>.
	 *
	 * @param resource the resource to be checked
	 * @param context the context passed to <code>validateEdit</code>
	 * @return status describing the method's result. If <code>status.isOK()</code> returns <code>true</code> then the resources are committable.
	 *
	 * @see org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)
	 */
	public static IStatus makeCommittable(IResource resource, Object context) {
		return makeCommittable(new IResource[] { resource }, context);
	}

	/**
	 * Makes the given resources committable. Committable means that all
	 * resources are writeable and that the content of the resources hasn't
	 * changed by calling <code>validateEdit</code> for a given file on
	 * <tt>IWorkspace</tt>.
	 *
	 * @param resources the resources to be checked
	 * @param context the context passed to <code>validateEdit</code>
	 * @return IStatus status describing the method's result. If <code>status.
	 * isOK()</code> returns <code>true</code> then the add resources are
	 * committable
	 *
	 * @see org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)
	 */
	public static IStatus makeCommittable(IResource[] resources, Object context) {
		List readOnlyFiles= new ArrayList();
		for (int i= 0; i < resources.length; i++) {
			IResource resource= resources[i];
			if (resource.getType() == IResource.FILE &&  isReadOnly(resource))
				readOnlyFiles.add(resource);
		}
		if (readOnlyFiles.size() == 0)
			return Status.OK_STATUS;

		Map oldTimeStamps= createModificationStampMap(readOnlyFiles);
		IStatus status= ResourcesPlugin.getWorkspace().validateEdit(
			(IFile[]) readOnlyFiles.toArray(new IFile[readOnlyFiles.size()]), context);
		if (!status.isOK())
			return status;

		IStatus modified= null;
		Map newTimeStamps= createModificationStampMap(readOnlyFiles);
		for (Iterator iter= oldTimeStamps.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry= (Entry) iter.next();
			if (!entry.getValue().equals(newTimeStamps.get(entry.getKey())))
				modified= addModified(modified, (IFile) entry.getKey());
		}
		if (modified != null)
			return modified;
		return Status.OK_STATUS;
	}

	private static Map createModificationStampMap(List files){
		Map map= new HashMap();
		for (Iterator iter= files.iterator(); iter.hasNext(); ) {
			IFile file= (IFile)iter.next();
			map.put(file, new Long(file.getModificationStamp()));
		}
		return map;
	}

	private static IStatus addModified(IStatus status, IFile file) {
		IStatus entry= new Status(
			IStatus.ERROR, RefactoringCorePlugin.getPluginId(),
			IRefactoringCoreStatusCodes.VALIDATE_EDIT_CHANGED_CONTENT,
			Messages.format(RefactoringCoreMessages.Resources_fileModified, BasicElementLabels.getPathLabel(file.getFullPath(), false)),
			null);
		if (status == null) {
			return entry;
		} else if (status.isMultiStatus()) {
			((MultiStatus)status).add(entry);
			return status;
		} else {
			MultiStatus result= new MultiStatus(RefactoringCorePlugin.getPluginId(),
				IRefactoringCoreStatusCodes.VALIDATE_EDIT_CHANGED_CONTENT,
				RefactoringCoreMessages.Resources_modifiedResources, null);
			result.add(status);
			result.add(entry);
			return result;
		}
	}

	private static IStatus addOutOfSync(IStatus status, IResource resource) {
		IStatus entry= new Status(
			IStatus.ERROR,
			ResourcesPlugin.PI_RESOURCES,
			IResourceStatus.OUT_OF_SYNC_LOCAL,
			Messages.format(RefactoringCoreMessages.Resources_outOfSync, BasicElementLabels.getPathLabel(resource.getFullPath(), false)),
			null);
		if (status == null) {
			return entry;
		} else if (status.isMultiStatus()) {
			((MultiStatus)status).add(entry);
			return status;
		} else {
			MultiStatus result= new MultiStatus(
				ResourcesPlugin.PI_RESOURCES,
				IResourceStatus.OUT_OF_SYNC_LOCAL,
				RefactoringCoreMessages.Resources_outOfSyncResources, null);
			result.add(status);
			result.add(entry);
			return result;
		}
	}

	public static boolean isReadOnly(IResource resource) {
		ResourceAttributes resourceAttributes = resource.getResourceAttributes();
		if (resourceAttributes == null)  // not supported on this platform for this resource
			return false;
		return resourceAttributes.isReadOnly();
	}

	static void setReadOnly(IResource resource, boolean readOnly) {
		ResourceAttributes resourceAttributes = resource.getResourceAttributes();
		if (resourceAttributes == null) // not supported on this platform for this resource
			return;

		resourceAttributes.setReadOnly(readOnly);
		try {
			resource.setResourceAttributes(resourceAttributes);
		} catch (CoreException e) {
			RefactoringCorePlugin.log(e);
		}
	}

	public static boolean containsOnlyNonProjects(IResource[] resources) {
		int types = getSelectedResourceTypes(resources);
		// check for empty selection
		if (types == 0) {
			return false;
		}
		// note that the selection may contain multiple types of resource
		return (types & IResource.PROJECT) == 0;
	}

	public static boolean containsOnlyProjects(IResource[] resources) {
		int types = getSelectedResourceTypes(resources);
		// note that the selection may contain multiple types of resource
		return types == IResource.PROJECT;
	}

	private static int getSelectedResourceTypes(IResource[] resources) {
		int types = 0;
		for (int i = 0; i < resources.length; i++) {
			types |= resources[i].getType();
		}
		return types;
	}
}
