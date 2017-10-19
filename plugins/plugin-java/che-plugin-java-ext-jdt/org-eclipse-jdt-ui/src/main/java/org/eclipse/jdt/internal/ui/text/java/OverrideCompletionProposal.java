/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CheASTParser;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.formatter.IndentManipulation;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.text.edits.MalformedTreeException;

public class OverrideCompletionProposal extends JavaTypeCompletionProposal
    implements ICompletionProposalExtension4 {

  private IJavaProject fJavaProject;
  private String fMethodName;
  private String[] fParamTypes;

  public OverrideCompletionProposal(
      IJavaProject jproject,
      ICompilationUnit cu,
      String methodName,
      String[] paramTypes,
      int start,
      int length,
      StyledString displayName,
      String completionProposal) {
    super(completionProposal, cu, start, length, null, displayName, 0);
    Assert.isNotNull(jproject);
    Assert.isNotNull(methodName);
    Assert.isNotNull(paramTypes);
    Assert.isNotNull(cu);

    fParamTypes = paramTypes;
    fMethodName = methodName;

    fJavaProject = jproject;

    StringBuffer buffer = new StringBuffer();
    buffer.append(completionProposal);
    buffer.append(" {};"); // $NON-NLS-1$

    setReplacementString(buffer.toString());
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getPrefixCompletionText(org.eclipse.jface.text.IDocument,int)
   */
  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    return fMethodName;
  }

  private CompilationUnit getRecoveredAST(
      IDocument document, int offset, Document recoveredDocument) {
    CompilationUnit ast =
        SharedASTProvider.getAST(fCompilationUnit, SharedASTProvider.WAIT_ACTIVE_ONLY, null);
    if (ast != null) {
      recoveredDocument.set(document.get());
      return ast;
    }

    char[] content = document.get().toCharArray();

    // clear prefix to avoid compile errors
    int index = offset - 1;
    while (index >= 0 && Character.isJavaIdentifierPart(content[index])) {
      content[index] = ' ';
      index--;
    }

    recoveredDocument.set(new String(content));

    final CheASTParser parser = CheASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
    parser.setResolveBindings(true);
    parser.setStatementsRecovery(true);
    parser.setSource(content);
    parser.setUnitName(fCompilationUnit.getElementName());
    parser.setProject(fCompilationUnit.getJavaProject());
    return (CompilationUnit) parser.createAST(new NullProgressMonitor());
  }

  /*
   * @see JavaTypeCompletionProposal#updateReplacementString(IDocument,char,int,ImportRewrite)
   */
  @Override
  protected boolean updateReplacementString(
      IDocument document, char trigger, int offset, ImportRewrite importRewrite)
      throws CoreException, BadLocationException {
    Document recoveredDocument = new Document();
    CompilationUnit unit = getRecoveredAST(document, offset, recoveredDocument);
    ImportRewriteContext context;
    if (importRewrite != null) {
      context = new ContextSensitiveImportRewriteContext(unit, offset, importRewrite);
    } else {
      importRewrite =
          StubUtility.createImportRewrite(unit, true); // create a dummy import rewriter to have one
      context =
          new ImportRewriteContext() { // forces that all imports are fully qualified
            @Override
            public int findInContext(String qualifier, String name, int kind) {
              return RES_NAME_CONFLICT;
            }
          };
    }

    ITypeBinding declaringType = null;
    ChildListPropertyDescriptor descriptor = null;
    ASTNode node = NodeFinder.perform(unit, offset, 1);
    if (node instanceof AnonymousClassDeclaration) {
      declaringType = ((AnonymousClassDeclaration) node).resolveBinding();
      descriptor = AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;
    } else if (node instanceof AbstractTypeDeclaration) {
      AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) node;
      descriptor = declaration.getBodyDeclarationsProperty();
      declaringType = declaration.resolveBinding();
    }
    if (declaringType != null) {
      ASTRewrite rewrite = ASTRewrite.create(unit.getAST());
      IMethodBinding methodToOverride =
          Bindings.findMethodInHierarchy(declaringType, fMethodName, fParamTypes);
      if (methodToOverride == null && declaringType.isInterface()) {
        methodToOverride =
            Bindings.findMethodInType(
                node.getAST().resolveWellKnownType("java.lang.Object"),
                fMethodName,
                fParamTypes); // $NON-NLS-1$
      }
      if (methodToOverride != null) {
        CodeGenerationSettings settings =
            JavaPreferencesSettings.getCodeGenerationSettings(fJavaProject);
        MethodDeclaration stub =
            StubUtility2.createImplementationStub(
                fCompilationUnit,
                rewrite,
                importRewrite,
                context,
                methodToOverride,
                declaringType.getName(),
                settings,
                declaringType.isInterface());
        ListRewrite rewriter = rewrite.getListRewrite(node, descriptor);
        rewriter.insertFirst(stub, null);

        ITrackedNodePosition position = rewrite.track(stub);
        try {
          rewrite
              .rewriteAST(recoveredDocument, fJavaProject.getOptions(true))
              .apply(recoveredDocument);

          String generatedCode =
              recoveredDocument.get(position.getStartPosition(), position.getLength());
          int generatedIndent =
              IndentManipulation.measureIndentUnits(
                  getIndentAt(recoveredDocument, position.getStartPosition(), settings),
                  settings.tabWidth,
                  settings.indentWidth);

          String indent = getIndentAt(document, getReplacementOffset(), settings);
          setReplacementString(
              IndentManipulation.changeIndent(
                  generatedCode,
                  generatedIndent,
                  settings.tabWidth,
                  settings.indentWidth,
                  indent,
                  TextUtilities.getDefaultLineDelimiter(document)));

        } catch (MalformedTreeException exception) {
          JavaPlugin.log(exception);
        } catch (BadLocationException exception) {
          JavaPlugin.log(exception);
        }
      }
    }
    return true;
  }

  private static String getIndentAt(
      IDocument document, int offset, CodeGenerationSettings settings) {
    try {
      IRegion region = document.getLineInformationOfOffset(offset);
      return IndentManipulation.extractIndentString(
          document.get(region.getOffset(), region.getLength()),
          settings.tabWidth,
          settings.indentWidth);
    } catch (BadLocationException e) {
      return ""; // $NON-NLS-1$
    }
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension4#isAutoInsertable()
   */
  public boolean isAutoInsertable() {
    return false;
  }
}
