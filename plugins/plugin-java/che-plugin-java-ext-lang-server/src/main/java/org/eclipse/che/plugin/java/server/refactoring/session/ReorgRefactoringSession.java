/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
