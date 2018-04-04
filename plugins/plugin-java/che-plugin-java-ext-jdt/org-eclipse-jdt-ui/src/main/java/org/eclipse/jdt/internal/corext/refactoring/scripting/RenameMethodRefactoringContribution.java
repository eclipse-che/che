/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.scripting;

import java.util.Map;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.MethodChecks;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameNonVirtualMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameVirtualMethodProcessor;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

/**
 * Refactoring contribution for the rename method refactoring.
 *
 * @since 3.2
 */
public final class RenameMethodRefactoringContribution extends JavaUIRefactoringContribution {

  /** {@inheritDoc} */
  @Override
  public Refactoring createRefactoring(
      JavaRefactoringDescriptor descriptor, RefactoringStatus status) throws JavaModelException {
    JavaRefactoringArguments arguments =
        new JavaRefactoringArguments(descriptor.getProject(), retrieveArgumentMap(descriptor));

    String input = arguments.getAttribute(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT);
    IMethod method =
        (IMethod) JavaRefactoringDescriptorUtil.handleToElement(arguments.getProject(), input);
    if (method == null) {
      status.addFatalError(
          Messages.format(
              RefactoringCoreMessages.RenameMethodRefactoringContribution_could_not_create,
              new Object[] {BasicElementLabels.getResourceName(arguments.getProject()), input}));
      return null;
    }

    JavaRenameProcessor processor;
    if (MethodChecks.isVirtual(method)) {
      processor = new RenameVirtualMethodProcessor(method, arguments, status);
    } else {
      processor = new RenameNonVirtualMethodProcessor(method, arguments, status);
    }
    return new RenameRefactoring(processor);
  }

  @Override
  public RefactoringDescriptor createDescriptor() {
    return RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(
        IJavaRefactorings.RENAME_METHOD);
  }

  @Override
  public RefactoringDescriptor createDescriptor(
      String id, String project, String description, String comment, Map arguments, int flags) {

    return RefactoringSignatureDescriptorFactory.createRenameJavaElementDescriptor(
        id, project, description, comment, arguments, flags);
  }
}
