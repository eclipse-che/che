/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.shared.dto.refactoring;

import org.eclipse.che.dto.shared.DTO;

/**
 * An immutable object representing an entry in the list in <code>RefactoringStatus</code>. A
 * refactoring status entry consists of a severity, a message, a problem code (represented by a
 * tuple(plug-in identifier and code number)).
 *
 * @author Evgen Vidolob
 */
@DTO
public interface RefactoringStatusEntry {

  /**
   * Returns the message of the status entry.
   *
   * @return the message
   */
  String getMessage();

  void setMessage(String message);

  /**
   * Returns the severity level.
   *
   * @return the severity level
   * @see RefactoringStatus#INFO
   * @see RefactoringStatus#WARNING
   * @see RefactoringStatus#ERROR
   * @see RefactoringStatus#FATAL
   */
  int getSeverity();

  void setSeverity(int severity);
}
