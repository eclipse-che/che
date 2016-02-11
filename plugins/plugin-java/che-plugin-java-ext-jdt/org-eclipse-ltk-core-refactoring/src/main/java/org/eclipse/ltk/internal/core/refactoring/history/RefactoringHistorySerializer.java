/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistoryEvent;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * Refactoring history listener which continuously persists the global
 * refactoring history in the different history locations.
 *
 * @since 3.2
 */
public final class RefactoringHistorySerializer implements IRefactoringHistoryListener {

	/**
	 * {@inheritDoc}
	 */
	public void historyNotification(final RefactoringHistoryEvent event) {
		Assert.isNotNull(event);
		switch (event.getEventType()) {
			case RefactoringHistoryEvent.ADDED:
			case RefactoringHistoryEvent.PUSHED:
			case RefactoringHistoryEvent.POPPED: {
				final RefactoringDescriptorProxy proxy= event.getDescriptor();
				final long stamp= proxy.getTimeStamp();
				if (stamp >= 0) {
					final String name= proxy.getProject();
					final IFileStore store= EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation()).getChild(RefactoringHistoryService.NAME_HISTORY_FOLDER);
					if (name != null && !"".equals(name)) { //$NON-NLS-1$
						final IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
						if (project.isAccessible()) {
							if (RefactoringHistoryService.hasSharedRefactoringHistory(project)) {
								final URI uri= project.getLocationURI();
								if (uri != null) {
									try {
										processHistoryNotification(EFS.getStore(uri).getChild(RefactoringHistoryService.NAME_HISTORY_FOLDER), event, name);
									} catch (CoreException exception) {
										RefactoringCorePlugin.log(exception);
									} finally {
										try {
											project.refreshLocal(IResource.DEPTH_INFINITE, null);
										} catch (CoreException exception) {
											RefactoringCorePlugin.log(exception);
										}
									}
								}
							} else {
								try {
									processHistoryNotification(store.getChild(name), event, name);
								} catch (CoreException exception) {
									RefactoringCorePlugin.log(exception);
								}
							}
						}
					} else {
						try {
							processHistoryNotification(store.getChild(RefactoringHistoryService.NAME_WORKSPACE_PROJECT), event, name);
						} catch (CoreException exception) {
							RefactoringCorePlugin.log(exception);
						}
					}
				}
			}
		}
	}

	/**
	 * Processes the history event.
	 *
	 * @param store
	 *            the file store
	 * @param event
	 *            the history event
	 * @param name
	 *            the project name, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs
	 */
	private void processHistoryNotification(final IFileStore store, final RefactoringHistoryEvent event, final String name) throws CoreException {
		final RefactoringDescriptorProxy proxy= event.getDescriptor();
		final int type= event.getEventType();
		final RefactoringHistoryManager manager= new RefactoringHistoryManager(store, name);
		final NullProgressMonitor monitor= new NullProgressMonitor();
		if (type == RefactoringHistoryEvent.PUSHED || type == RefactoringHistoryEvent.ADDED) {
			final RefactoringDescriptor descriptor= proxy.requestDescriptor(monitor);
			if (descriptor != null)
				manager.addRefactoringDescriptor(descriptor, type == RefactoringHistoryEvent.ADDED, monitor);
		} else if (type == RefactoringHistoryEvent.POPPED)
			manager.removeRefactoringDescriptors(new RefactoringDescriptorProxy[] { proxy}, monitor, RefactoringCoreMessages.RefactoringHistoryService_updating_history);
	}
}
