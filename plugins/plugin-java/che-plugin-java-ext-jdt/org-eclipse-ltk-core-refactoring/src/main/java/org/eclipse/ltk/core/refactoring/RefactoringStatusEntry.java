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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

/**
 * An immutable object representing an entry in the list in <code>RefactoringStatus</code>.
 * A refactoring status entry consists of a severity, a message, a problem code
 * (represented by a tuple(plug-in identifier and code number)), a context object and a
 * generic data pointer. The context object is used to provide context information for
 * the problem itself. An example context is a tuple consisting of the resource that contains
 * the problem and a corresponding line number.
 * <p>
 * Note: this class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefactoringStatusEntry {

	/**
	 * A special problem code indicating that no problem code is provided. If
	 * <code>NO_CODE</code> is used then the plug-in identifier can be <code>
	 * null</code>
	 */
	public static final int NO_CODE= -1;

	/** The severity */
	private final int fSeverity;

	/** The message */
	private final String fMessage;

	/** A plug-in specific problem code */
	private final int fCode;

	/** A plug-in identifier to make the problem code unique */
	private final String fPluginId;

	/** A context providing detailed information of where the problem occurred */
	private final RefactoringStatusContext fContext;

	/** A generic data pointer */
	private final Object fData;

	/**
	 * Creates a new refactoring status entry. The context is set to <code>
	 * null</code> the problem code is set to <code>NO_CODE</code>, the
	 * plug-in identifier is set to <code>null</code> and the data pointer
	 * is set to <code>null</code> as well.
	 *
	 * @param severity the severity
	 * @param msg the message
	 */
	public RefactoringStatusEntry(int severity, String msg) {
		this(severity, msg, null);
	}

	/**
	 * Creates a new refactoring status entry. The problem code is set to <code>
	 * NO_CODE</code>, the plug-in identifier is set to <code>null</code> and
	 * the data pointer is set to <code>null</code> as well.
	 *
	 * @param severity the severity
	 * @param msg the message
	 * @param context the context. Can be <code>null</code>
	 */
	public RefactoringStatusEntry(int severity, String msg, RefactoringStatusContext context) {
		this(severity, msg, context, null, NO_CODE, null);
	}

	/**
	 * Creates a new refactoring status entry.
	 *
	 * @param severity the severity
	 * @param msg the message
	 * @param context the context. Can be <code>null</code>
	 * @param pluginId the plug-in identifier. Can be <code>null</code> if argument <code>
	 *  code</code> equals <code>NO_CODE</code>
	 * @param code the problem code. Must be either <code>NO_CODE</code> or equals or greater
	 *  than zero
	 */
	public RefactoringStatusEntry(int severity, String msg, RefactoringStatusContext context, String pluginId, int code) {
		this(severity, msg, context, pluginId, code, null);
	}

	/**
	 * Creates a new refactoring status entry.
	 *
	 * @param severity the severity
	 * @param msg the message
	 * @param context the context. Can be <code>null</code>
	 * @param pluginId the plug-in identifier. Can be <code>null</code> if argument <code>
	 *  code</code> equals <code>NO_CODE</code>
	 * @param code the problem code. Must be either <code>NO_CODE</code> or a positive integer
	 * @param data application specific data
	 */
	public RefactoringStatusEntry(int severity, String msg, RefactoringStatusContext context, String pluginId, int code, Object data) {
		Assert.isTrue(severity == RefactoringStatus.INFO || severity == RefactoringStatus.WARNING
			|| severity == RefactoringStatus.ERROR || severity == RefactoringStatus.FATAL);
		Assert.isNotNull(msg);
		Assert.isTrue(code == NO_CODE || code >= 0);
		if (code != NO_CODE) Assert.isTrue(pluginId != null);
		fMessage= msg;
		fSeverity= severity;
		fContext= context;
		fPluginId= pluginId;
		fCode= code;
		fData= data;
	}

	/**
	 * Returns the message of the status entry.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return fMessage;
	}

	/**
	 * Returns the severity level.
	 *
	 * @return the severity level
	 *
	 * @see RefactoringStatus#INFO
	 * @see RefactoringStatus#WARNING
	 * @see RefactoringStatus#ERROR
	 * @see RefactoringStatus#FATAL
	 */
	public int getSeverity() {
		return fSeverity;
	}

	/**
	 * Returns the context which can be used to show more detailed information regarding
	 * this status entry in the UI. The method may return <code>null</code> indicating
	 * that no context is available.
	 *
	 * @return the status entry's context
	 */
	public RefactoringStatusContext getContext() {
		return fContext;
	}

	/**
	 * Returns the plug-in identifier associated with the
	 * problem code. Might return <code>null</code> if the
	 * problem code equals <code>NO_CODE</code>.
	 *
	 * @return the plug-in identifier
	 */
	public String getPluginId() {
		return fPluginId;
	}

	/**
	 * Returns the problem code.
	 *
	 * @return the problem code
	 */
	public int getCode() {
		return fCode;
	}

	/**
	 * Returns the application defined entry data associated
	 * with the receiver, or <code>null</code> if it has not
	 * been set.
	 *
	 * @return the entry data
	 */
	public Object getData() {
		return fData;
	}

	/**
	 * Returns whether the entry represents a fatal error or not.
	 *
	 * @return <code>true</code> if (severity ==<code>RefactoringStatus.FATAL</code>)
	 */
	public boolean isFatalError() {
		return fSeverity == RefactoringStatus.FATAL;
	}

	/**
	 * Returns whether the entry represents an error or not.
	 *
	 * @return <code>true</code> if (severity ==<code>RefactoringStatus.ERROR</code>).
	 */
	public boolean isError() {
		return fSeverity == RefactoringStatus.ERROR;
	}

	/**
	 * Returns whether the entry represents a warning or not.
	 *
	 * @return <code>true</code> if (severity ==<code>RefactoringStatus.WARNING</code>).
	 */
	public boolean isWarning() {
		return fSeverity == RefactoringStatus.WARNING;
	}

	/**
	 * Returns whether the entry represents an information or not.
	 *
	 * @return <code>true</code> if (severity ==<code>RefactoringStatus.INFO</code>).
	 */
	public boolean isInfo() {
		return fSeverity == RefactoringStatus.INFO;
	}

	/**
	 * Returns this refactoring status entry as an {@link IStatus}.
	 * <p>
	 * If this refactoring status entry has a severity of
	 * {@link RefactoringStatus#FATAL}, the returned status will have a
	 * severity of {@link IStatus#ERROR}, otherwise a status with severity
	 * corresponding to the refactoring status entry is returned. If the plugin
	 * id of this refactoring status entry is not defined, the plugin id
	 * <code>org.eclipse.ltk.core.refactoring</code> will be used in the
	 * returned status.
	 * </p>
	 *
	 * @return the corresponding status
	 *
	 * @since 3.2
	 */
	public IStatus toStatus() {
		int statusSeverity= IStatus.ERROR;
		switch (getSeverity()) {
			case RefactoringStatus.OK:
				statusSeverity= IStatus.OK;
				break;
			case RefactoringStatus.INFO:
				statusSeverity= IStatus.INFO;
				break;
			case RefactoringStatus.WARNING:
			case RefactoringStatus.ERROR:
				statusSeverity= IStatus.WARNING;
				break;
		}
		String pluginId= getPluginId();
		int code= getCode();
		if (pluginId == null) {
			pluginId= RefactoringCorePlugin.getPluginId();
			code= IStatus.ERROR;
		}
		return new Status(statusSeverity, pluginId, code, getMessage(), null);
	}

	/*
	 * non java-doc for debugging only
	 */
	public String toString() {
		String contextString= fContext == null ? "<Unspecified context>" : fContext.toString(); //$NON-NLS-1$
		return "\n" //$NON-NLS-1$
			+ RefactoringStatus.getSeverityString(fSeverity) + ": " + fMessage + //$NON-NLS-1$
			"\nContext: " + contextString + //$NON-NLS-1$
			(fCode == NO_CODE ? "\ncode: none" : "\nplug-in id: " + fPluginId + "code: " + fCode) +  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"\nData: " + fData;  //$NON-NLS-1$
	}
}
