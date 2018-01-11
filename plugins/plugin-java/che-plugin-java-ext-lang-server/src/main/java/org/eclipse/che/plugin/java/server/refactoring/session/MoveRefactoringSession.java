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
