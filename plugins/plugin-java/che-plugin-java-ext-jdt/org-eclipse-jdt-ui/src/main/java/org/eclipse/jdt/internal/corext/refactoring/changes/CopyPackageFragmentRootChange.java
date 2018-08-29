/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.changes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.internal.corext.refactoring.reorg.INewNameQuery;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IPackageFragmentRootManipulationQuery;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.ltk.core.refactoring.Change;

public class CopyPackageFragmentRootChange extends PackageFragmentRootReorgChange {

  public CopyPackageFragmentRootChange(
      IPackageFragmentRoot root,
      IProject destination,
      INewNameQuery newNameQuery,
      IPackageFragmentRootManipulationQuery updateClasspathQuery) {
    super(root, destination, newNameQuery, updateClasspathQuery);
  }

  @Override
  protected Change doPerformReorg(IPath destinationPath, IProgressMonitor pm)
      throws JavaModelException {
    getRoot().copy(destinationPath, getResourceUpdateFlags(), getUpdateModelFlags(true), null, pm);
    return null;
  }

  @Override
  public String getName() {
    String rootName = JavaElementLabels.getElementLabel(getRoot(), JavaElementLabels.ALL_DEFAULT);
    String destinationName = BasicElementLabels.getResourceName(getDestination());
    return Messages.format(
        RefactoringCoreMessages.CopyPackageFragmentRootChange_copy,
        new String[] {rootName, destinationName});
  }
}
