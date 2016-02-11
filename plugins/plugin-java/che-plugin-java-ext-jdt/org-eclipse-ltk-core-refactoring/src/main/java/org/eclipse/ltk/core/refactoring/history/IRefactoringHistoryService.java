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
package org.eclipse.ltk.core.refactoring.history;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IProject;

import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;

/**
 * Interface for a refactoring history service. A refactoring history service
 * provides methods to register refactoring history listeners, refactoring
 * execution listeners and facilities to query the global refactoring history
 * index for specific refactoring histories. Additionally, methods are provided
 * which read or write refactoring information. The refactoring history service
 * only returns refactorings which have contributed a refactoring descriptor via
 * their change object.
 * <p>
 * An instance of a refactoring history service may be obtained by calling
 * {@link RefactoringCore#getHistoryService()}.
 * </p>
 * <p>
 * All time stamps are measured as the milliseconds since January 1, 1970,
 * 00:00:00 GMT.
 * </p>
 * <p>
 * Note: this interface is not intended to be implemented by clients.
 * </p>
 *
 * @see RefactoringCore
 * @see IRefactoringHistoryListener
 * @see IRefactoringExecutionListener
 *
 * @see RefactoringHistory
 * @see RefactoringDescriptorProxy
 *
 * @since 3.2
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRefactoringHistoryService {

	/**
	 * Adds the specified refactoring execution listener to this service.
	 * <p>
	 * If the listener is already registered with the service, nothing happens.
	 * </p>
	 *
	 * @param listener
	 *            the listener to add
	 */
	public void addExecutionListener(IRefactoringExecutionListener listener);

	/**
	 * Adds the specified refactoring history listener to this service.
	 * <p>
	 * If the listener is already registered with the service, nothing happens.
	 * </p>
	 *
	 * @param listener
	 *            the listener to add
	 */
	public void addHistoryListener(IRefactoringHistoryListener listener);

	/**
	 * Connects the refactoring history service to the workbench's operation
	 * history if necessary and increments an internal counter.
	 * <p>
	 * If the service is already connected, nothing happens.
	 * </p>
	 * <p>
	 * Every call to {@link #connect()} must be balanced with a corresponding
	 * call to {@link #disconnect()}.
	 * </p>
	 */
	public void connect();

	/**
	 * Disconnects the refactoring history service from the workbench's
	 * operation history if necessary and decrements an internal counter.
	 * <p>
	 * If the service is not connected, nothing happens. If the service is
	 * connected, all resources acquired since the corresponding call to
	 * {@link #connect()} are released.
	 * </p>
	 * <p>
	 * Every call to {@link #disconnect()} must be balanced with a corresponding
	 * call to {@link #connect()}.
	 * </p>
	 */
	public void disconnect();

	/**
	 * Returns a project refactoring history for the specified project.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 *
	 * @param project
	 *            the project, which must exist
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code> if no
	 *            progress monitoring or cancelation is desired
	 * @return the project refactoring history
	 */
	public RefactoringHistory getProjectHistory(IProject project, IProgressMonitor monitor);

	/**
	 * Returns a project refactoring history for the specified project.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 * <p>
	 * Note that calling this method with a flag argument unequal to
	 * <code>RefactoringDescriptor#NONE</code> may result in a performance
	 * degradation, since the actual descriptors have to be eagerly resolved.
	 * This in turn results in faster execution of any subsequent calls to
	 * {@link RefactoringDescriptorProxy#requestDescriptor(IProgressMonitor)}
	 * which try to request a descriptor from the returned refactoring history.
	 * </p>
	 *
	 * @param project
	 *            the project, which must exist
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param flags
	 *            the refactoring descriptor flags which must be present in
	 *            order to be returned in the refactoring history object, or
	 *            <code>RefactoringDescriptor#NONE</code>
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code> if no
	 *            progress monitoring or cancelation is desired
	 * @return the project refactoring history
	 */
	public RefactoringHistory getProjectHistory(IProject project, long start, long end, int flags, IProgressMonitor monitor);

	/**
	 * Returns the combined refactoring history for the specified projects.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 *
	 * @param projects
	 *            the projects, which must exist
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code> if no
	 *            progress monitoring or cancelation is desired
	 * @return the combined refactoring history
	 */
	public RefactoringHistory getRefactoringHistory(IProject[] projects, IProgressMonitor monitor);

	/**
	 * Returns the combined refactoring history for the specified projects.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 * <p>
	 * Note that calling this method with a flag argument unequal to
	 * <code>RefactoringDescriptor#NONE</code> may result in a performance
	 * degradation, since the actual descriptors have to be eagerly resolved.
	 * This in turn results in faster execution of any subsequent calls to
	 * {@link RefactoringDescriptorProxy#requestDescriptor(IProgressMonitor)}
	 * which try to request a descriptor from the returned refactoring history.
	 * </p>
	 *
	 * @param projects
	 *            the projects, which must exist
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param flags
	 *            the refactoring descriptor flags which must be present in
	 *            order to be returned in the refactoring history object, or
	 *            <code>RefactoringDescriptor#NONE</code>
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code> if no
	 *            progress monitoring or cancelation is desired
	 * @return the combined refactoring history
	 */
	public RefactoringHistory getRefactoringHistory(IProject[] projects, long start, long end, int flags, IProgressMonitor monitor);

	/**
	 * Returns the workspace refactoring history.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 *
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code> if no
	 *            progress monitoring or cancelation is desired
	 * @return the workspace refactoring history
	 */
	public RefactoringHistory getWorkspaceHistory(IProgressMonitor monitor);

	/**
	 * Returns the workspace refactoring history.
	 * <p>
	 * Clients must connect to the refactoring history service first before
	 * calling this method.
	 * </p>
	 *
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp, inclusive
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code> if no
	 *            progress monitoring or cancelation is desired
	 * @return the workspace refactoring history
	 */
	public RefactoringHistory getWorkspaceHistory(long start, long end, IProgressMonitor monitor);

	/**
	 * Reads a refactoring history from the input stream.
	 * <p>
	 * The resulting refactoring history contains resolved refactoring
	 * descriptors and should not be held on to.
	 * </p>
	 * <p>
	 * It is the responsibility of the caller to close the input stream.
	 * </p>
	 *
	 * @param stream
	 *            a <code>UTF-8</code> input stream where to read the
	 *            refactoring history from
	 * @param flags
	 *            the refactoring descriptor flags to filter the refactoring
	 *            descriptors
	 * @return a refactoring history containing the filtered refactoring
	 *         descriptors
	 * @throws CoreException
	 *             if an error occurs while reading form the input stream.
	 *             Reasons include:
	 *             <ul>
	 *             <li>The input stream contains no version information for the
	 *             refactoring history.</li>
	 *             <li>The input stream contains an unsupported version of a
	 *             refactoring history.</li>
	 *             <li>An I/O error occurs while reading the refactoring
	 *             history from the input stream.</li>
	 *             </ul>
	 *
	 * @see RefactoringDescriptor#NONE
	 * @see RefactoringDescriptor#STRUCTURAL_CHANGE
	 * @see RefactoringDescriptor#BREAKING_CHANGE
	 *
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_IO_ERROR
	 * @see IRefactoringCoreStatusCodes#UNSUPPORTED_REFACTORING_HISTORY_VERSION
	 * @see IRefactoringCoreStatusCodes#MISSING_REFACTORING_HISTORY_VERSION
	 */
	public RefactoringHistory readRefactoringHistory(InputStream stream, int flags) throws CoreException;

	/**
	 * Removes the specified refactoring execution listener from this service.
	 * <p>
	 * If the listener is not registered with the service, nothing happens.
	 * </p>
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeExecutionListener(IRefactoringExecutionListener listener);

	/**
	 * Removes the specified refactoring history listener from this service.
	 * <p>
	 * If the listener is not registered with the service, nothing happens.
	 * </p>
	 *
	 * @param listener
	 *            the listener to remove
	 */
	public void removeHistoryListener(IRefactoringHistoryListener listener);

	/**
	 * Writes the specified refactoring descriptor proxies to the output stream.
	 * Refactoring descriptor proxies which cannot be resolved are automatically
	 * skipped.
	 * <p>
	 * It is the responsibility of the caller to close the output stream.
	 * </p>
	 *
	 * @param proxies
	 *            the refactoring descriptor proxies
	 * @param stream
	 *            a <code>UTF-8</code> output stream where to write the
	 *            refactoring descriptors to
	 * @param flags
	 *            the flags which must be present in order to be written to the
	 *            output stream, or <code>RefactoringDescriptor#NONE</code>
	 * @param time
	 *            <code>true</code> to write time information associated with
	 *            the refactorings, <code>false</code> otherwise
	 * @param monitor
	 *            the progress monitor to use, or <code>null</code> if no
	 *            progress monitoring or cancelation is desired
	 * @throws CoreException
	 *             if an error occurs while writing to the output stream.
	 *             Reasons include:
	 *             <ul>
	 *             <li>The refactoring descriptors have an illegal format,
	 *             contain illegal arguments or otherwise illegal information.</li>
	 *             <li>An I/O error occurs while writing the refactoring
	 *             descriptors to the output stream.</li>
	 *             </ul>
	 *
	 * @see RefactoringDescriptor#NONE
	 * @see RefactoringDescriptor#STRUCTURAL_CHANGE
	 * @see RefactoringDescriptor#BREAKING_CHANGE
	 *
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_FORMAT_ERROR
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_IO_ERROR
	 */
	public void writeRefactoringDescriptors(RefactoringDescriptorProxy[] proxies, OutputStream stream, int flags, boolean time, IProgressMonitor monitor) throws CoreException;

	/**
	 * Writes the specified refactoring session descriptor to the output stream.
	 * <p>
	 * It is the responsibility of the caller to close the output stream.
	 * </p>
	 *
	 * @param descriptor
	 *            the refactoring session descriptor to write
	 * @param stream
	 *            a <code>UTF-8</code> output stream where to write the
	 *            refactoring session to
	 * @param time
	 *            <code>true</code> to write time information associated with
	 *            the refactorings, <code>false</code> otherwise
	 * @throws CoreException
	 *             if an error occurs while writing to the output stream.
	 *             Reasons include:
	 *             <ul>
	 *             <li>The refactoring descriptors have an illegal format,
	 *             contain illegal arguments or otherwise illegal information.</li>
	 *             <li>An I/O error occurs while writing the refactoring
	 *             descriptors to the output stream.</li>
	 *             </ul>
	 *
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_FORMAT_ERROR
	 * @see IRefactoringCoreStatusCodes#REFACTORING_HISTORY_IO_ERROR
	 */
	public void writeRefactoringSession(RefactoringSessionDescriptor descriptor, OutputStream stream, boolean time) throws CoreException;
}
