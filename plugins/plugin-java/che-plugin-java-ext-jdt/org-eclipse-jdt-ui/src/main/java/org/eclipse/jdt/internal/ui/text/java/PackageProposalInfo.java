/**
 * ***************************************************************************** Copyright (c) 2012,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public class PackageProposalInfo extends ProposalInfo {

  private boolean fJavaElementResolved = false;

  private final IJavaProject fJavaProject;

  private final CompletionProposal fProposal;

  PackageProposalInfo(IJavaProject project, CompletionProposal proposal) {
    Assert.isNotNull(project);
    Assert.isNotNull(proposal);
    fJavaProject = project;
    fProposal = proposal;
  }

  @Override
  public IJavaElement getJavaElement() throws JavaModelException {
    if (!fJavaElementResolved) {
      fJavaElementResolved = true;
      fElement = resolvePackage();
    }
    return fElement;
  }

  /**
   * Resolves to a PackageFragment.
   *
   * @return the <code>IPackageFragment</code> or <code>null</code> if no Java element can be found
   * @throws org.eclipse.jdt.core.JavaModelException thrown if the given path is <code>null</code>
   *     or absolute
   * @since 3.9
   */
  private IJavaElement resolvePackage() throws JavaModelException {
    char[] signature = fProposal.getDeclarationSignature();
    if (signature != null) {
      String typeName = String.valueOf(signature);
      typeName = typeName.replace('.', IPath.SEPARATOR);
      return fJavaProject.findElement(new Path(typeName));
    }
    return null;
  }
}
