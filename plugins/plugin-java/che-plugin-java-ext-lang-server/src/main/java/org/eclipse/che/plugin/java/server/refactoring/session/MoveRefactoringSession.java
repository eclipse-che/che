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
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaMoveProcessor;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgDestinationFactory;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/** @author Evgen Vidolob */
public class MoveRefactoringSession extends ReorgRefactoringSession {

  JavaMoveProcessor processor;

  public MoveRefactoringSession(Refactoring refactoring, JavaMoveProcessor processor) {
    super(refactoring);
    this.processor = processor;
  }

  @Override
  public RefactoringStatus verifyDestination(Object selected) throws JavaModelException {
    return processor.setDestination(ReorgDestinationFactory.createDestination(selected));
  }

  public void setUpdateQualifiedNames(boolean update) {
    processor.setUpdateQualifiedNames(update);
  }

  public void setUpdateReferences(boolean update) {
    processor.setUpdateReferences(update);
  }

  public void setFilePatterns(String patterns) {
    processor.setFilePatterns(patterns);
  }
}
