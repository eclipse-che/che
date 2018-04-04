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

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;

/**
 * Proposal info that computes the javadoc lazily when it is queried.
 *
 * @since 3.1
 */
public final class FieldProposalInfo extends MemberProposalInfo {

  /**
   * Creates a new proposal info.
   *
   * @param project the java project to reference when resolving types
   * @param proposal the proposal to generate information for
   */
  public FieldProposalInfo(IJavaProject project, CompletionProposal proposal) {
    super(project, proposal);
  }

  /**
   * Resolves the member described by the receiver and returns it if found. Returns <code>null
   * </code> if no corresponding member can be found.
   *
   * @return the resolved member or <code>null</code> if none is found
   * @throws org.eclipse.jdt.core.JavaModelException if accessing the java model fails
   */
  @Override
  protected IMember resolveMember() throws JavaModelException {
    char[] declarationSignature = fProposal.getDeclarationSignature();
    // for synthetic fields on arrays, declaration signatures may be null
    // TODO remove when https://bugs.eclipse.org/bugs/show_bug.cgi?id=84690 gets fixed
    if (declarationSignature == null) return null;
    String typeName = SignatureUtil.stripSignatureToFQN(String.valueOf(declarationSignature));
    IType type = fJavaProject.findType(typeName);
    if (type != null) {
      String name = String.valueOf(fProposal.getName());
      IField field = type.getField(name);
      if (field.exists()) return field;
    }

    return null;
  }
}
