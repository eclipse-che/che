/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.shared.dto.refactoring;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * A <code>RefactoringStatus</code> object represents the outcome of a
 * condition checking operation. It manages a list of <code>
 * RefactoringStatusEntry</code> objects. Each <code>RefactoringStatusEntry
 * </code> object describes one particular problem detected during
 * condition checking.
 * @author Evgen Vidolob
 */
@DTO
public interface RefactoringStatus {
    /**
     * Status severity constant (value 0) indicating this status represents the nominal case.
     */
    int OK = 0;

    /**
     * Status severity constant (value 1) indicating this status is informational only.
     */
    int INFO = 1;

    /**
     * Status severity constant (value 2) indicating this status represents a warning.
     * <p>
     * Use this severity if the refactoring can be performed, but you assume that the
     * user could not be aware of problems or confusions resulting from the execution.
     * </p>
     */
    int WARNING = 2;

    /**
     * Status severity constant (value 3) indicating this status represents an error.
     * <p>
     * Use this severity if the refactoring can be performed, but the refactoring will
     * not be behavior preserving and/or the partial execution will lead to an inconsistent
     * state (e.g. compile errors).
     * </p>
     */
    int ERROR = 3;

    /**
     * Status severity constant (value 4) indicating this status represents a fatal error.
     * <p>
     * Use this severity if the refactoring cannot be performed, and execution would lead
     * to major problems. Note that this completely blocks the user from performing this refactoring.
     * It is often preferable to use an {@link #ERROR} status and allow a partial execution
     * (e.g. if just one reference to a refactored element cannot be updated).
     * </p>
     */
    int FATAL = 4;

    /**
     * Returns the severity.
     *
     * @return the severity.
     */
    int getSeverity();

    void setSeverity(int severity);

    /**
     * Returns the list of refactoring status entries.
     *
     * @return the list of refactoring status entries.
     */
    List<RefactoringStatusEntry> getEntries();

    void setEntries(List<RefactoringStatusEntry> entries);

}
