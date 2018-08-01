/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.refactoring.session;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/** @author Evgen Vidolob */
public abstract class ReorgRefactoringSession extends RefactoringSession {

  public ReorgRefactoringSession(Refactoring refactoring) {
    super(refactoring);
  }

  /**
   * Set and verify destination
   *
   * @param selected
   * @return the resulting status
   * @throws JavaModelException
   */
  public abstract RefactoringStatus verifyDestination(Object selected) throws JavaModelException;
}
