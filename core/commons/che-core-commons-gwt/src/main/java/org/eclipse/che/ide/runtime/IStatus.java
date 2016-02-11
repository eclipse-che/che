/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.runtime;

/**
 * A status object represents the outcome of an operation. All <code>CoreException</code>s carry a status object to indicate what
 * went wrong. Status objects are also returned by methods needing to provide details of failures (e.g., validation methods).
 * <p>
 * A status carries the following information:
 * <ul>
 * <li>plug-in identifier (required)</li>
 * <li>severity (required)</li>
 * <li>status code (required)</li>
 * <li>message (required) - localized to current locale</li>
 * <li>exception (optional) - for problems stemming from a failure at a lower level</li>
 * </ul>
 * Some status objects, known as multi-statuses, have other status objects as children.
 * </p>
 * <p>
 * The class <code>Status</code> is the standard public implementation of status objects; the subclass <code>MultiStatus</code> is
 * the implements multi-status objects.
 * </p>
 * <p>
 * This interface can be used without OSGi running.
 * </p>
 *
 * @see MultiStatus
 * @see Status
 */
public interface IStatus {

    /**
     * Status severity constant (value 0) indicating this status represents the nominal case. This constant is also used as the
     * status code representing the nominal case.
     *
     * @see #getSeverity()
     * @see #isOK()
     */
    public static final int OK = 0;

    /**
     * Status type severity (bit mask, value 1) indicating this status is informational only.
     *
     * @see #getSeverity()
     * @see #matches(int)
     */
    public static final int INFO = 0x01;

    /**
     * Status type severity (bit mask, value 2) indicating this status represents a warning.
     *
     * @see #getSeverity()
     * @see #matches(int)
     */
    public static final int WARNING = 0x02;

    /**
     * Status type severity (bit mask, value 4) indicating this status represents an error.
     *
     * @see #getSeverity()
     * @see #matches(int)
     */
    public static final int ERROR = 0x04;

    /**
     * Status type severity (bit mask, value 8) indicating this status represents a cancelation
     *
     * @see #getSeverity()
     * @see #matches(int)
     * @since 3.0
     */
    public static final int CANCEL = 0x08;

    /**
     * Returns a list of status object immediately contained in this multi-status, or an empty list if this is not a multi-status.
     *
     * @return an array of status objects
     * @see #isMultiStatus()
     */
    public IStatus[] getChildren();

    /**
     * Returns the plug-in-specific status code describing the outcome.
     *
     * @return plug-in-specific status code
     */
    public int getCode();

    /**
     * Returns the relevant low-level exception, or <code>null</code> if none. For example, when an operation fails because of a
     * network communications failure, this might return the <code>java.io.IOException</code> describing the exact nature of that
     * failure.
     *
     * @return the relevant low-level exception, or <code>null</code> if none
     */
    public Throwable getException();

    /**
     * Returns the message describing the outcome. The message is localized to the current locale.
     *
     * @return a localized message
     */
    public String getMessage();

    /**
     * Returns the unique identifier of the plug-in associated with this status (this is the plug-in that defines the meaning of
     * the status code).
     *
     * @return the unique identifier of the relevant plug-in
     */
    public String getPlugin();

    /**
     * Returns the severity. The severities are as follows (in descending order):
     * <ul>
     * <li><code>CANCEL</code> - cancelation occurred</li>
     * <li><code>ERROR</code> - a serious error (most severe)</li>
     * <li><code>WARNING</code> - a warning (less severe)</li>
     * <li><code>INFO</code> - an informational ("fyi") message (least severe)</li>
     * <li><code>OK</code> - everything is just fine</li>
     * </ul>
     * <p>
     * The severity of a multi-status is defined to be the maximum severity of any of its children, or <code>OK</code> if it has no
     * children.
     * </p>
     *
     * @return the severity: one of <code>OK</code>, <code>ERROR</code>, <code>INFO</code>, <code>WARNING</code>, or
     *         <code>CANCEL</code>
     * @see #matches(int)
     */
    public int getSeverity();

    /**
     * Returns whether this status is a multi-status. A multi-status describes the outcome of an operation involving multiple
     * operands.
     * <p>
     * The severity of a multi-status is derived from the severities of its children; a multi-status with no children is
     * <code>OK</code> by definition. A multi-status carries a plug-in identifier, a status code, a message, and an optional
     * exception. Clients may treat multi-status objects in a multi-status unaware way.
     * </p>
     *
     * @return <code>true</code> for a multi-status, <code>false</code> otherwise
     * @see #getChildren()
     */
    public boolean isMultiStatus();

    /**
     * Returns whether this status indicates everything is okay (neither info, warning, nor error).
     *
     * @return <code>true</code> if this status has severity <code>OK</code>, and <code>false</code> otherwise
     */
    public boolean isOK();

    /**
     * Returns whether the severity of this status matches the given severity mask. Note that a status with severity
     * <code>OK</code> will never match; use <code>isOK</code> instead to detect a status with a severity of <code>OK</code>.
     *
     * @param severityMask
     *         a mask formed by bitwise or'ing severity mask constants (<code>ERROR</code>, <code>WARNING</code>,
     *         <code>INFO</code>, <code>CANCEL</code>)
     * @return <code>true</code> if there is at least one match, <code>false</code> if there are no matches
     * @see #getSeverity()
     * @see #CANCEL
     * @see #ERROR
     * @see #WARNING
     * @see #INFO
     */
    public boolean matches(int severityMask);
}
