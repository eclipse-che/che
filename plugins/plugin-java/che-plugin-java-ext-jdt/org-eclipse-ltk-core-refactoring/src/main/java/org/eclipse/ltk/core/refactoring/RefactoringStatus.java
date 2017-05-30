/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A <code>RefactoringStatus</code> object represents the outcome of a
 * condition checking operation. It manages a list of <code>
 * RefactoringStatusEntry</code> objects. Each <code>RefactoringStatusEntry
 * </code> object describes one particular problem detected during
 * condition checking.
 * <p>
 * Additionally a problem severity is managed. Severities are ordered as follows:
 * <code>OK</code> &lt; <code>INFO</code> &lt; <code>WARNING</code> &lt; <code>
 * ERROR</code> &lt; <code>FATAL</code>. The status's problem severity is the maximum
 * of the severities of all entries. If the status doesn't have any entry the status's
 * severity is <code>OK</code>.
 * </p>
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @see RefactoringStatusEntry
 * @see Refactoring#checkAllConditions(IProgressMonitor)
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefactoringStatus {

	/**
	 * Status severity constant (value 0) indicating this status represents the nominal case.
	 */
	public static final int OK= 0;

	/**
	 * Status severity constant (value 1) indicating this status is informational only.
	 */
	public static final int INFO= 1;

	/**
	 * Status severity constant (value 2) indicating this status represents a warning.
	 * <p>
	 * Use this severity if the refactoring can be performed, but you assume that the
	 * user could not be aware of problems or confusions resulting from the execution.
	 * </p>
	 */
	public static final int WARNING= 2;

	/**
	 * Status severity constant (value 3) indicating this status represents an error.
	 * <p>
	 * Use this severity if the refactoring can be performed, but the refactoring will
	 * not be behavior preserving and/or the partial execution will lead to an inconsistent
	 * state (e.g. compile errors).
	 * </p>
	 */
	public static final int ERROR= 3;

	/**
	 * Status severity constant (value 4) indicating this status represents a fatal error.
	 * <p>
	 * Use this severity if the refactoring cannot be performed, and execution would lead
	 * to major problems. Note that this completely blocks the user from performing this refactoring.
	 * It is often preferable to use an {@link #ERROR} status and allow a partial execution
	 * (e.g. if just one reference to a refactored element cannot be updated).
	 * </p>
	 */
	public static final int FATAL= 4;

	/**
	 * List of refactoring status entries.
	 */
	private List fEntries;

	/**
	 * The status's severity. The following invariant holds for
	 * <code>fSeverity</code>: <code>OK</code> &le; <code>fSeverity</code> &le;
	 * <code>FATAL</code>.
	 */
	private int fSeverity= OK;

	/**
	 * Creates a new refactoring status with an empty list of
	 * status entries and a severity of <code>OK</code>.
	 */
	public RefactoringStatus() {
		fEntries= new ArrayList(0);
	}

	/**
	 * Returns the severity.
	 *
	 * @return the severity.
	 */
	public int getSeverity() {
		return fSeverity;
	}

	/**
	 * Returns the list of refactoring status entries.
	 *
	 * @return the list of refactoring status entries. Returns an empty array
	 *  if no entries are managed.
	 */
	public RefactoringStatusEntry[] getEntries() {
		return (RefactoringStatusEntry[])fEntries.toArray(new RefactoringStatusEntry[fEntries.size()]);
	}

	/**
	 * Returns a list of refactoring status entries which are considered equal
	 * to the specified status entry.
	 *
	 * @param comparator the comparator to determine whether two status entries
	 *               are considered equal
	 * @param entry the refactoring status entry to compare the entries of this
	 *               status with
	 * @return the list of refactoring status entries that are considered equal
	 *               to the specified one, in no particular order. Returns an empty
	 *               array if no entries are managed or none of them matches.
	 *
	 * @since 3.1
	 */
	public RefactoringStatusEntry[] getEntries(IRefactoringStatusEntryComparator comparator, RefactoringStatusEntry entry) {
		final List matches= new ArrayList(fEntries.size());
		RefactoringStatusEntry current= null;
		for (Iterator iterator= fEntries.iterator(); iterator.hasNext();) {
			current= (RefactoringStatusEntry) iterator.next();
			if (comparator.compare(current, entry) == 0)
				matches.add(current);
		}
		return (RefactoringStatusEntry[]) matches.toArray(new RefactoringStatusEntry[matches.size()]);
	}

	/**
	 * Returns whether the status has entries or not.
	 *
	 * @return <code>true</code> if the status as any entries; otherwise
	 *  <code>false</code> is returned.
	 */
	public boolean hasEntries() {
		return !fEntries.isEmpty();
	}

	/**
	 * Returns the <code>RefactoringStatusEntry</code> at the specified index.
	 *
	 * @param index the index of the entry to return
	 * @return the entry at the specified index
	 *
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public RefactoringStatusEntry getEntryAt(int index) {
		return (RefactoringStatusEntry)fEntries.get(index);
	}

	/**
	 * Returns the first entry managed by this refactoring status that
	 * matches the given plug-in identifier and code. If more than one
	 * entry exists that matches the criteria the first one in the list
	 * of entries is returned. Returns <code>null</code> if no entry
	 * matches.
	 *
	 * @param pluginId the entry's plug-in identifier
	 * @param code the entry's code
	 * @return the entry that matches the given plug-in identifier and
	 *  code; <code>null</code> otherwise
	 */
	public RefactoringStatusEntry getEntryMatchingCode(String pluginId, int code) {
		Assert.isTrue(pluginId != null);
		for (Iterator iter= fEntries.iterator(); iter.hasNext(); ) {
			RefactoringStatusEntry entry= (RefactoringStatusEntry)iter.next();
			if (pluginId.equals(entry.getPluginId()) && entry.getCode() == code)
				return entry;
		}
		return null;
	}

	/**
	 * Returns the first entry which severity is equal or greater than the
	 * given severity. If more than one entry exists that matches the
	 * criteria the first one is returned. Returns <code>null</code> if no
	 * entry matches.
	 *
	 * @param severity the severity to search for. Must be one of <code>FATAL
	 *  </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>
	 * @return the entry that matches the search criteria
	 */
	public RefactoringStatusEntry getEntryMatchingSeverity(int severity) {
		Assert.isTrue(severity >= OK && severity <= FATAL);
		if (severity > fSeverity)
			return null;
		Iterator iter= fEntries.iterator();
		while (iter.hasNext()) {
			RefactoringStatusEntry entry= (RefactoringStatusEntry)iter.next();
			if (entry.getSeverity() >= severity)
				return entry;
		}
		return null;
	}

	/**
	 * Returns the entry with the highest severity. If there is more than one
	 * entry that matches the first one found in the list of entries is returned.
	 *
	 * @return the entry with the highest severity or <code>null</code> if no
	 *  entries are present
	 *
	 * @since 3.1
	 */
	public RefactoringStatusEntry getEntryWithHighestSeverity() {
		if (fEntries == null || fEntries.size() == 0)
			return null;
		RefactoringStatusEntry result= (RefactoringStatusEntry)fEntries.get(0);
		for (int i= 1; i < fEntries.size(); i++) {
			RefactoringStatusEntry entry= (RefactoringStatusEntry)fEntries.get(i);
			if (result.getSeverity() < entry.getSeverity())
				result= entry;
		}
		return result;
	}

	/**
	 * Returns the first message which severity is equal or greater than the
	 * given severity. If more than one entry exists that matches the criteria
	 * the first one is returned. Returns <code>null</code> if no entry matches.
	 *
	 * @param severity the severity to search for. Must be one of <code>FATAL
	 *  </code>, <code>ERROR</code>, <code>WARNING</code> or <code>INFO</code>
	 * @return the message of the entry that matches the search criteria
	 */
	public String getMessageMatchingSeverity(int severity) {
		RefactoringStatusEntry entry= getEntryMatchingSeverity(severity);
		if (entry == null)
			return null;
		return entry.getMessage();
	}

	/**
	 * Creates a new <code>RefactoringStatus</code> with one entry filled with the given
	 * arguments.
	 *
	 * @param severity the severity
	 * @param msg the message
	 * @param context the context. Can be <code>null</code>
	 * @param pluginId the plug-in identifier. Can be <code>null</code> if argument <code>
	 *  code</code> equals <code>NO_CODE</code>
	 * @param code the problem code. Must be either <code>NO_CODE</code> or a positive integer
	 * @param data application specific data
	 *
	 * @return the newly created refactoring status
	 *
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createStatus(int severity, String msg, RefactoringStatusContext context, String pluginId, int code, Object data) {
		RefactoringStatus result= new RefactoringStatus();
		result.fEntries.add(new RefactoringStatusEntry(severity, msg, context, pluginId, code, data));
		result.fSeverity= severity;
		return result;
	}

	/**
	 * Creates a new <code>RefactoringStatus</code> with one <code>INFO</code> entry
	 * filled with the given message.
	 *
	 * @param msg the message of the info entry
	 * @return the refactoring status
	 *
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createInfoStatus(String msg) {
		return createStatus(INFO, msg, null, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a new <code>RefactoringStatus</code> with one <code>INFO</code> entry
	 * filled with the given message and context.
	 *
	 * @param msg the message of the info entry
	 * @param context the context of the info entry
	 * @return the refactoring status
	 *
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createInfoStatus(String msg, RefactoringStatusContext context) {
		return createStatus(INFO, msg, context, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a new <code>RefactoringStatus</code> with one <code>WARNING</code> entry
	 * filled with the given message.
	 *
	 * @param msg the message of the warning entry
	 * @return the refactoring status
	 *
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createWarningStatus(String msg) {
		return createStatus(WARNING, msg, null, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a <code>RefactoringStatus</code> with one <code>WARNING</code> entry
	 * fill with the given message and context.
	 *
	 * @param msg the message of the warning entry
	 * @param context the context of the warning entry
	 * @return the refactoring status
	 *
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createWarningStatus(String msg, RefactoringStatusContext context) {
		return createStatus(WARNING, msg, context, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a new <code>RefactoringStatus</code> with one <code>ERROR</code> entry
	 * filled with the given message.
	 *
	 * @param msg the message of the error entry
	 * @return the refactoring status
	 *
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createErrorStatus(String msg) {
		return createStatus(ERROR, msg, null, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a <code>RefactoringStatus</code> with one <code>ERROR</code> entry
	 * fill with the given message and context.
	 *
	 * @param msg the message of the error entry
	 * @param context the context of the error entry
	 * @return the refactoring status
	 *
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createErrorStatus(String msg, RefactoringStatusContext context) {
		return createStatus(ERROR, msg, context, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a new <code>RefactoringStatus</code> with one <code>FATAL</code> entry
	 * filled with the given message.
	 *
	 * @param msg the message of the fatal entry
	 * @return the refactoring status
	 *
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createFatalErrorStatus(String msg) {
		return createStatus(FATAL, msg, null, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a <code>RefactoringStatus</code> with one <code>FATAL</code> entry
	 * fill with the given message and context.
	 *
	 * @param msg the message of the fatal entry
	 * @param context the context of the fatal entry
	 * @return the refactoring status
	 *
	 * @see RefactoringStatusEntry
	 */
	public static RefactoringStatus createFatalErrorStatus(String msg, RefactoringStatusContext context) {
		return createStatus(FATAL, msg, context, null, RefactoringStatusEntry.NO_CODE, null);
	}

	/**
	 * Creates a new <code>RefactoringStatus</code> from the given <code>IStatus</code>. An
	 * OK status is mapped to an OK refactoring status, an information status is mapped
	 * to a warning refactoring status, a warning status is mapped to an error refactoring
	 * status and an error or cancel status is mapped to a fatal refactoring status. An unknown
	 * status is converted into a fatal error status as well. If the status is a <code>MultiStatus
	 * </code> then the first level of children of the status will be added as refactoring status
	 * entries to the created refactoring status.
	 *
	 * @param status the status to create a refactoring status from
	 * @return the refactoring status
	 *
	 * @see IStatus
	 * @since 3.2
	 */
	public static RefactoringStatus create(IStatus status) {
		if (status.isOK())
			return new RefactoringStatus();

		if (!status.isMultiStatus()) {
			switch (status.getSeverity()) {
				case IStatus.OK :
					return new RefactoringStatus();
				case IStatus.INFO :
					return RefactoringStatus.createWarningStatus(status.getMessage());
				case IStatus.WARNING :
					return RefactoringStatus.createErrorStatus(status.getMessage());
				case IStatus.ERROR :
					return RefactoringStatus.createFatalErrorStatus(status.getMessage());
				case IStatus.CANCEL :
					return RefactoringStatus.createFatalErrorStatus(status.getMessage());
				default :
					return RefactoringStatus.createFatalErrorStatus(status.getMessage());
			}
		} else {
			IStatus[] children= status.getChildren();
			RefactoringStatus result= new RefactoringStatus();
			for (int i= 0; i < children.length; i++) {
				result.merge(RefactoringStatus.create(children[i]));
			}
			return result;
		}
	}

	/**
	 * Merges the receiver and the parameter statuses. The resulting list of
	 * entries in the receiver will contain entries from both. The resulting
	 * severity in the receiver will be the more severe of its current severity
	 * and the parameter's severity. Merging with <code>null</code> is
	 * allowed - it has no effect.
	 *
	 * @param other the refactoring status to merge with
	 */
	public void merge(RefactoringStatus other) {
		if (other == null)
			return;
		fEntries.addAll(other.fEntries);
		fSeverity= Math.max(fSeverity, other.getSeverity());
	}

	/**
	 * Adds an <code>INFO</code> entry filled with the given message to this status.
	 * If the current severity is <code>OK</code> it will be changed to <code>INFO
	 * </code>. It will remain unchanged otherwise.
	 *
	 * @param msg the message of the info entry
	 *
	 * @see RefactoringStatusEntry
	 */
	public void addInfo(String msg) {
		addInfo(msg, null);
	}

	/**
	 * Adds an <code>INFO</code> entry filled with the given message and context to
	 * this status. If the current severity is <code>OK</code> it will be changed to
	 * <code>INFO</code>. It will remain unchanged otherwise.
	 *
	 * @param msg the message of the info entry
	 * @param context the context of the info entry
	 *
	 * @see RefactoringStatusEntry
	 */
	public void addInfo(String msg, RefactoringStatusContext context) {
		fEntries.add(new RefactoringStatusEntry(RefactoringStatus.INFO, msg, context));
		fSeverity= Math.max(fSeverity, INFO);
	}

	/**
	 * Adds a <code>WARNING</code> entry filled with the given message to this status.
	 * If the current severity is <code>OK</code> or <code>INFO</code> it will be
	 * changed to <code>WARNING</code>. It will remain unchanged otherwise.
	 *
	 * @param msg the message of the warning entry
	 *
	 * @see RefactoringStatusEntry
	 */
	public void addWarning(String msg) {
		addWarning(msg, null);
	}

	/**
	 * Adds a <code>WARNING</code> entry filled with the given message and context to
	 * this status. If the current severity is <code>OK</code> or <code>INFO</code> it
	 * will be changed to <code>WARNING</code>. It will remain unchanged otherwise.
	 *
	 * @param msg the message of the warning entry
	 * @param context the context of the warning entry
	 *
	 * @see RefactoringStatusEntry
	 */
	public void addWarning(String msg, RefactoringStatusContext context) {
		fEntries.add(new RefactoringStatusEntry(RefactoringStatus.WARNING, msg, context));
		fSeverity= Math.max(fSeverity, WARNING);
	}

	/**
	 * Adds an <code>ERROR</code> entry filled with the given message to this status.
	 * If the current severity is <code>OK</code>, <code>INFO</code> or <code>WARNING
	 * </code> it will be changed to <code>ERROR</code>. It will remain unchanged
	 * otherwise.
	 *
	 * @param msg the message of the error entry
	 *
	 * @see RefactoringStatusEntry
	 */
	public void addError(String msg) {
		addError(msg, null);
	}

	/**
	 * Adds an <code>ERROR</code> entry filled with the given message and context to
	 * this status. If the current severity is <code>OK</code>, <code>INFO</code> or
	 * <code>WARNING</code> it will be changed to <code>ERROR</code>. It will remain
	 * unchanged otherwise.
	 *
	 * @param msg the message of the error entry
	 * @param context the context of the error entry
	 *
	 * @see RefactoringStatusEntry
	 */
	public void addError(String msg, RefactoringStatusContext context) {
		fEntries.add(new RefactoringStatusEntry(RefactoringStatus.ERROR, msg, context));
		fSeverity= Math.max(fSeverity, ERROR);
	}

	/**
	 * Adds a <code>FATAL</code> entry filled with the given message to this status.
	 * The severity of this status will changed to <code>FATAL</code>.
	 *
	 * @param msg the message of the fatal entry
	 *
	 * @see RefactoringStatusEntry
	 */
	public void addFatalError(String msg) {
		addFatalError(msg, null);
	}

	/**
	 * Adds a <code>FATAL</code> entry filled with the given message and status to
	 * this status. The severity of this status will changed to <code>FATAL</code>.
	 *
	 * @param msg the message of the fatal entry
	 * @param context the context of the fatal entry
	 *
	 * @see RefactoringStatusEntry
	 */
	public void addFatalError(String msg, RefactoringStatusContext context) {
		fEntries.add(new RefactoringStatusEntry(RefactoringStatus.FATAL, msg, context));
		fSeverity= Math.max(fSeverity, FATAL);
	}

	/**
	 * Adds a new entry filled with the given arguments to this status. The severity
	 * of this status is set to the maximum of <code>fSeverity</code> and
	 * <code>severity</code>.
	 *
	 * @param severity the severity of the entry
	 * @param msg the message of the entry
	 * @param context the context of the entry. Can be <code>null</code>
	 * @param pluginId the plug-in identifier of the entry. Can be <code>null</code> if
	 *  argument <code>code</code> equals <code>NO_CODE</code>
	 * @param code the problem code of the entry. Must be either <code>NO_CODE</code>
	 *  or a positive integer
	 */
	public void addEntry(int severity, String msg, RefactoringStatusContext context, String pluginId, int code) {
		fEntries.add(new RefactoringStatusEntry(severity, msg, context, pluginId, code));
		fSeverity= Math.max(fSeverity, severity);
	}

	/**
	 * Adds a new entry filled with the given arguments to this status. The severity
	 * of this status is set to the maximum of <code>fSeverity</code> and
	 * <code>severity</code>.
	 *
	 * @param severity the severity of the entry
	 * @param msg the message of the entry
	 * @param context the context of the entry. Can be <code>null</code>
	 * @param pluginId the plug-in identifier of the entry. Can be <code>null</code> if
	 *  argument <code>code</code> equals <code>NO_CODE</code>
	 * @param code the problem code of the entry. Must be either <code>NO_CODE</code>
	 *  or a positive integer
	 * @param data application specific data of the entry
	 */
	public void addEntry(int severity, String msg, RefactoringStatusContext context, String pluginId, int code, Object data) {
		fEntries.add(new RefactoringStatusEntry(severity, msg, context, pluginId, code, data));
		fSeverity= Math.max(fSeverity, severity);
	}

	/**
	 * Adds the given <code>RefactoringStatusEntry</code>. The severity of this
	 * status is set to the maximum of <code>fSeverity</code> and the severity of
	 * the entry.
	 *
	 * @param entry the <code>RefactoringStatusEntry</code> to be added
	 */
	public void addEntry(RefactoringStatusEntry entry) {
		Assert.isNotNull(entry);
		fEntries.add(entry);
		fSeverity= Math.max(fSeverity, entry.getSeverity());
	}

	/**
	 * Returns whether the status's severity is <code>OK</code> or not.
	 *
	 * @return <code>true</code> if the severity is <code>OK</code>;
	 *  otherwise <code>false</code> is returned
	 */
	public boolean isOK() {
		return fSeverity == OK;
	}

	/**
	 * Returns <code>true</code> if the current severity is <code>
	 * FATAL</code>.
	 *
	 * @return <code>true</code> if the current severity is <code>
	 *  FATAL</code>; otherwise <code>false</code> is returned
	 */
	public boolean hasFatalError() {
		return fSeverity == FATAL;
	}

	/**
	 * Returns <code>true</code> if the current severity is <code>
	 * FATAL</code> or <code>ERROR</code>.
	 *
	 * @return <code>true</code> if the current severity is <code>
	 *  FATAL</code> or <code>ERROR</code>; otherwise <code>false
	 *  </code> is returned
	 */
	public boolean hasError() {
		return fSeverity == FATAL || fSeverity == ERROR;
	}

	/**
	 * Returns <code>true</code> if the current severity is <code>
	 * FATAL</code>, <code>ERROR</code> or <code>WARNING</code>.
	 *
	 * @return <code>true</code> if the current severity is <code>
	 *  FATAL</code>, <code>ERROR</code> or <code>WARNING</code>;
	 *  otherwise <code>false</code> is returned
	 */
	public boolean hasWarning() {
		return fSeverity == FATAL || fSeverity == ERROR || fSeverity == WARNING;
	}

	/**
	 * Returns <code>true</code> if the current severity is <code>
	 * FATAL</code>, <code>ERROR</code>, <code>WARNING</code> or
	 * <code>INFO</code>.
	 *
	 * @return <code>true</code> if the current severity is <code>
	 *  FATAL</code>, <code>ERROR</code>, <code>WARNING</code> or
	 *  <code>INFO</code>; otherwise <code>false</code> is returned
	 */
	public boolean hasInfo() {
		return fSeverity == FATAL || fSeverity == ERROR || fSeverity == WARNING || fSeverity == INFO;
	}

	/*
	 * (non java-doc)
	 * for debugging only
	 */
	public String toString() {
		StringBuffer buff= new StringBuffer();
		buff.append("<") //$NON-NLS-1$
			.append(getSeverityString(fSeverity)).append("\n"); //$NON-NLS-1$
		if (!isOK()) {
			for (Iterator iter= fEntries.iterator(); iter.hasNext(); ) {
				buff.append("\t") //$NON-NLS-1$
					.append(iter.next()).append("\n"); //$NON-NLS-1$
			}
		}
		buff.append(">"); //$NON-NLS-1$
		return buff.toString();
	}

	/*
	 * non java-doc
	 * for debugging only not for nls
	 */
	/* package */static String getSeverityString(int severity) {
		Assert.isTrue(severity >= OK && severity <= FATAL);
		if (severity == RefactoringStatus.OK)
			return "OK"; //$NON-NLS-1$
		if (severity == RefactoringStatus.INFO)
			return "INFO"; //$NON-NLS-1$
		if (severity == RefactoringStatus.WARNING)
			return "WARNING"; //$NON-NLS-1$
		if (severity == RefactoringStatus.ERROR)
			return "ERROR"; //$NON-NLS-1$
		if (severity == RefactoringStatus.FATAL)
			return "FATALERROR"; //$NON-NLS-1$
		return null;
	}
}
