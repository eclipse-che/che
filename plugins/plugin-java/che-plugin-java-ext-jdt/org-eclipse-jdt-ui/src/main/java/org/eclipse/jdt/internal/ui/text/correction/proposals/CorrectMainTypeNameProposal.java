/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.correction.proposals;

import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.LinkedNodeFinder;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;

/**
 * Renames the primary type to be compatible with the name of the compilation unit. All constructors
 * and local references to the type are renamed as well.
 */
public class CorrectMainTypeNameProposal extends ASTRewriteCorrectionProposal {

  private final String fOldName;
  private final String fNewName;
  private final IInvocationContext fContext;

  /**
   * Constructor for CorrectTypeNameProposal.
   *
   * @param cu the compilation unit
   * @param context the invocation context
   * @param oldTypeName the old type name
   * @param newTypeName the new type name
   * @param relevance the relevance
   */
  public CorrectMainTypeNameProposal(
      ICompilationUnit cu,
      IInvocationContext context,
      String oldTypeName,
      String newTypeName,
      int relevance) {
    super("", cu, null, relevance, null); // $NON-NLS-1$
    fContext = context;

    setDisplayName(
        Messages.format(
            CorrectionMessages.ReorgCorrectionsSubProcessor_renametype_description,
            BasicElementLabels.getJavaElementName(newTypeName)));
    setImage(JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));

    fOldName = oldTypeName;
    fNewName = newTypeName;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.internal.ui.text.correction.ASTRewriteCorrectionProposal#getRewrite()
   */
  @Override
  protected ASTRewrite getRewrite() throws CoreException {
    CompilationUnit astRoot = fContext.getASTRoot();

    AST ast = astRoot.getAST();
    ASTRewrite rewrite = ASTRewrite.create(ast);

    AbstractTypeDeclaration decl = findTypeDeclaration(astRoot.types(), fOldName);
    if (decl != null) {
      ASTNode[] sameNodes = LinkedNodeFinder.findByNode(astRoot, decl.getName());
      for (int i = 0; i < sameNodes.length; i++) {
        rewrite.replace(sameNodes[i], ast.newSimpleName(fNewName), null);
      }
    }
    return rewrite;
  }

  private AbstractTypeDeclaration findTypeDeclaration(
      List<AbstractTypeDeclaration> types, String name) {
    for (Iterator<AbstractTypeDeclaration> iter = types.iterator(); iter.hasNext(); ) {
      AbstractTypeDeclaration decl = iter.next();
      if (name.equals(decl.getName().getIdentifier())) {
        return decl;
      }
    }
    return null;
  }
}
