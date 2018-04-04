/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Proposal info that computes the javadoc lazily when it is queried.
 *
 * @since 3.1
 */
public abstract class MemberProposalInfo extends ProposalInfo {
  /* configuration */
  protected final IJavaProject fJavaProject;
  protected final CompletionProposal fProposal;

  /* cache filled lazily */
  private boolean fJavaElementResolved = false;

  /**
   * Creates a new proposal info.
   *
   * @param project the java project to reference when resolving types
   * @param proposal the proposal to generate information for
   */
  public MemberProposalInfo(IJavaProject project, CompletionProposal proposal) {
    Assert.isNotNull(project);
    Assert.isNotNull(proposal);
    fJavaProject = project;
    fProposal = proposal;
  }

  /**
   * Returns the java element that this computer corresponds to, possibly <code>null</code>.
   *
   * @return the java element that this computer corresponds to, possibly <code>null</code>
   * @throws org.eclipse.jdt.core.JavaModelException if accessing the java model fails
   */
  @Override
  public IJavaElement getJavaElement() throws JavaModelException {
    if (!fJavaElementResolved) {
      fJavaElementResolved = true;
      fElement = resolveMember();
    }
    return fElement;
  }

  /**
   * Resolves the member described by the receiver and returns it if found. Returns <code>null
   * </code> if no corresponding member can be found.
   *
   * @return the resolved member or <code>null</code> if none is found
   * @throws org.eclipse.jdt.core.JavaModelException if accessing the java model fails
   */
  protected abstract IMember resolveMember() throws JavaModelException;
}
