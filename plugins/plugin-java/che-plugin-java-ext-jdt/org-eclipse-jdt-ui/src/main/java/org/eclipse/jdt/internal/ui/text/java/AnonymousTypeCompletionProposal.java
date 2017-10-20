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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.che.jface.text.contentassist.IContextInformation;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CheASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.MalformedTreeException;

public class AnonymousTypeCompletionProposal extends JavaTypeCompletionProposal
    implements ICompletionProposalExtension4 {

  private final String fDeclarationSignature;
  private final IType fSuperType;

  private boolean fIsContextInformationComputed;
  private int fContextInformationPosition;

  private ImportRewrite fImportRewrite;

  public AnonymousTypeCompletionProposal(
      IJavaProject jproject,
      ICompilationUnit cu,
      JavaContentAssistInvocationContext invocationContext,
      int start,
      int length,
      String constructorCompletion,
      StyledString displayName,
      String declarationSignature,
      IType superType,
      int relevance) {
    super(
        constructorCompletion,
        cu,
        start,
        length,
        null,
        displayName,
        relevance,
        null,
        invocationContext);
    Assert.isNotNull(declarationSignature);
    Assert.isNotNull(jproject);
    Assert.isNotNull(cu);
    Assert.isNotNull(superType);

    fDeclarationSignature = declarationSignature;
    fSuperType = superType;

    setImage(getImageForType(fSuperType));
    setCursorPosition(constructorCompletion.indexOf('(') + 1);
  }

  private String createDummyType(String name) throws JavaModelException {
    StringBuffer buffer = new StringBuffer();

    buffer.append("abstract class "); // $NON-NLS-1$
    buffer.append(name);
    if (fSuperType.isInterface()) buffer.append(" implements "); // $NON-NLS-1$
    else buffer.append(" extends "); // $NON-NLS-1$

    if (fDeclarationSignature != null) buffer.append(Signature.toString(fDeclarationSignature));
    else buffer.append(fSuperType.getFullyQualifiedParameterizedName());
    buffer.append(" {"); // $NON-NLS-1$
    buffer.append(
        "\n"); // Using newline is ok since source is used in dummy compilation unit //$NON-NLS-1$
    buffer.append("}"); // $NON-NLS-1$
    return buffer.toString();
  }

  private String createNewBody(ImportRewrite importRewrite) throws CoreException {
    if (importRewrite == null) return null;

    ICompilationUnit workingCopy = null;
    try {
      String name = "Type" + System.currentTimeMillis(); // $NON-NLS-1$
      workingCopy = fCompilationUnit.getPrimary().getWorkingCopy(null);

      ISourceRange range = fSuperType.getSourceRange();
      boolean sameUnit = range != null && fCompilationUnit.equals(fSuperType.getCompilationUnit());

      // creates a type that extends the super type
      String dummyClassContent = createDummyType(name);

      StringBuffer workingCopyContents = new StringBuffer(fCompilationUnit.getSource());
      int insertPosition;
      if (sameUnit) {
        insertPosition = range.getOffset() + range.getLength();
      } else {
        ISourceRange firstTypeRange = fCompilationUnit.getTypes()[0].getSourceRange();
        insertPosition = firstTypeRange.getOffset();
      }
      if (fSuperType.isLocal()) {
        // add an extra block: helps the AST to recover
        workingCopyContents.insert(insertPosition, '{' + dummyClassContent + '}');
        insertPosition++;
      } else {
        /*
         * The two empty lines are added because the trackedDeclaration uses the covered range
         * and hence would also included comments that directly follow the dummy class.
         */
        workingCopyContents.insert(insertPosition, dummyClassContent + "\n\n"); // $NON-NLS-1$
      }

      workingCopy.getBuffer().setContents(workingCopyContents.toString());

      CheASTParser parser = CheASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
      parser.setResolveBindings(true);
      parser.setStatementsRecovery(true);
      parser.setSource(workingCopy);

      CompilationUnit astRoot = (CompilationUnit) parser.createAST(new NullProgressMonitor());
      ASTNode newType = NodeFinder.perform(astRoot, insertPosition, dummyClassContent.length());
      if (!(newType instanceof AbstractTypeDeclaration)) return null;

      AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) newType;
      ITypeBinding dummyTypeBinding = declaration.resolveBinding();
      if (dummyTypeBinding == null) return null;

      IMethodBinding[] bindings =
          StubUtility2.getOverridableMethods(astRoot.getAST(), dummyTypeBinding, true);

      if (fSuperType.isInterface()) {
        ITypeBinding[] dummySuperInterfaces = dummyTypeBinding.getInterfaces();
        if (dummySuperInterfaces.length == 0
            || dummySuperInterfaces.length == 1 && dummySuperInterfaces[0].isRawType())
          bindings = new IMethodBinding[0];
      } else {
        ITypeBinding dummySuperclass = dummyTypeBinding.getSuperclass();
        if (dummySuperclass == null || dummySuperclass.isRawType())
          bindings = new IMethodBinding[0];
      }

      CodeGenerationSettings settings =
          JavaPreferencesSettings.getCodeGenerationSettings(fSuperType.getJavaProject());

      IMethodBinding[] methodsToOverride = null;

      IType type = null;
      if (!fSuperType.isInterface() && !fSuperType.isAnnotation()) {
        IJavaElement typeElement = dummyTypeBinding.getJavaElement();
        // add extra checks here as the recovered code is fragile
        if (typeElement instanceof IType
            && name.equals(typeElement.getElementName())
            && typeElement.exists()) {
          type = (IType) typeElement;
        }
      }

      if (type != null) {
        //				OverrideMethodDialog dialog= new
        // OverrideMethodDialog(JavaPlugin.getActiveWorkbenchShell(), null, type, true);
        //				dialog.setGenerateComment(false);
        //				dialog.setElementPositionEnabled(false);
        //				if (dialog.open() == Window.OK) {
        //					Object[] selection= dialog.getResult();
        //					ArrayList<Object> result= new ArrayList<Object>(selection.length);
        //					for (int i= 0; i < selection.length; i++) {
        //						if (selection[i] instanceof IMethodBinding)
        //							result.add(selection[i]);
        //					}
        //					methodsToOverride= result.toArray(new IMethodBinding[result.size()]);
        //					settings.createComments= dialog.getGenerateComment();
        //				} else {
        //					// cancelled
        //					setReplacementString(""); //$NON-NLS-1$
        //					setReplacementLength(0);
        //					return null;
        //				}
        // TODO window
        throw new UnsupportedOperationException();
      } else {
        settings.createComments = false;
        List<IMethodBinding> result = new ArrayList<IMethodBinding>();
        for (int i = 0; i < bindings.length; i++) {
          IMethodBinding curr = bindings[i];
          if (Modifier.isAbstract(curr.getModifiers())) result.add(curr);
        }
        methodsToOverride = result.toArray(new IMethodBinding[result.size()]);
      }
      ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
      ITrackedNodePosition trackedDeclaration = rewrite.track(declaration);

      ListRewrite rewriter =
          rewrite.getListRewrite(declaration, declaration.getBodyDeclarationsProperty());
      for (int i = 0; i < methodsToOverride.length; i++) {
        IMethodBinding curr = methodsToOverride[i];
        MethodDeclaration stub =
            StubUtility2.createImplementationStub(
                workingCopy,
                rewrite,
                importRewrite,
                null,
                curr,
                dummyTypeBinding.getName(),
                settings,
                dummyTypeBinding.isInterface());
        rewriter.insertFirst(stub, null);
      }

      IDocument document = new Document(workingCopy.getSource());
      try {
        rewrite.rewriteAST().apply(document);

        int bodyStart = trackedDeclaration.getStartPosition() + dummyClassContent.indexOf('{');
        int bodyEnd = trackedDeclaration.getStartPosition() + trackedDeclaration.getLength();
        return document.get(bodyStart, bodyEnd - bodyStart);
      } catch (MalformedTreeException exception) {
        JavaPlugin.log(exception);
      } catch (BadLocationException exception) {
        JavaPlugin.log(exception);
      }
      return null;
    } finally {
      if (workingCopy != null) workingCopy.discardWorkingCopy();
    }
  }

  private Image getImageForType(IType type) {
    ImageDescriptor imageName = JavaPluginImages.DESC_OBJS_CLASS; // default
    try {
      if (type.isAnnotation()) {
        imageName = JavaPluginImages.DESC_OBJS_ANNOTATION;
      } else if (type.isInterface()) {
        imageName = JavaPluginImages.DESC_OBJS_INTERFACE;
      }
    } catch (JavaModelException e) {
      JavaPlugin.log(e);
    }
    return JavaPluginImages.get(imageName);
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension4#isAutoInsertable()
   */
  public boolean isAutoInsertable() {
    return false;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#isOffsetValid(int)
   * @since 3.5
   */
  @Override
  protected boolean isOffsetValid(int offset) {
    CompletionProposal coreProposal = ((MemberProposalInfo) getProposalInfo()).fProposal;
    if (coreProposal.getKind() != CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION)
      return super.isOffsetValid(offset);

    return coreProposal.getRequiredProposals()[0].getReplaceStart() <= offset;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#getPrefixCompletionStart(org.eclipse.jface.text.IDocument, int)
   * @since 3.5
   */
  @Override
  public int getPrefixCompletionStart(IDocument document, int completionOffset) {
    CompletionProposal coreProposal = ((MemberProposalInfo) getProposalInfo()).fProposal;
    if (coreProposal.getKind() != CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION)
      return super.getPrefixCompletionStart(document, completionOffset);

    return coreProposal.getRequiredProposals()[0].getReplaceStart();
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.JavaTypeCompletionProposal#getPrefixCompletionText(org.eclipse.jface.text.IDocument, int)
   * @since 3.5
   */
  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    CompletionProposal coreProposal = ((MemberProposalInfo) getProposalInfo()).fProposal;
    if (coreProposal.getKind() != CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION)
      return super.getPrefixCompletionText(document, completionOffset);

    return String.valueOf(coreProposal.getName());
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#getPrefix(org.eclipse.jface.text.IDocument, int)
   * @since 3.5
   */
  @Override
  protected String getPrefix(IDocument document, int offset) {
    CompletionProposal coreProposal = ((MemberProposalInfo) getProposalInfo()).fProposal;
    if (coreProposal.getKind() != CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION)
      return super.getPrefix(document, offset);

    int replacementOffset = coreProposal.getRequiredProposals()[0].getReplaceStart();

    try {
      int length = offset - replacementOffset;
      if (length > 0) return document.get(replacementOffset, length);
    } catch (BadLocationException x) {
    }
    return ""; // $NON-NLS-1$
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.JavaTypeCompletionProposal#isValidPrefix(java.lang.String)
   * @since 3.5
   */
  @Override
  protected boolean isValidPrefix(String prefix) {
    CompletionProposal coreProposal = ((MemberProposalInfo) getProposalInfo()).fProposal;
    if (coreProposal.getKind() != CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION)
      return super.isValidPrefix(prefix);

    return super.isValidPrefix(prefix) || isPrefix(prefix, String.valueOf(coreProposal.getName()));
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.JavaTypeCompletionProposal#apply(org.eclipse.jface.text.IDocument, char, int)
   * @since 3.5
   */
  @Override
  public void apply(IDocument document, char trigger, int offset) {
    super.apply(document, trigger, offset);
    LinkedModeModel.closeAllModels(document);
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.JavaTypeCompletionProposal#updateReplacementString(org.eclipse.jface.text.IDocument, char, int, org.eclipse.jdt.core.dom.rewrite.ImportRewrite)
   */
  @Override
  protected boolean updateReplacementString(
      IDocument document, char trigger, int offset, ImportRewrite impRewrite)
      throws CoreException, BadLocationException {
    fImportRewrite = impRewrite;
    String newBody = createNewBody(impRewrite);
    if (newBody == null) return false;

    CompletionProposal coreProposal = ((MemberProposalInfo) getProposalInfo()).fProposal;
    boolean isAnonymousConstructorInvoc =
        coreProposal.getKind() == CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION;

    boolean replacementStringEndsWithParentheses =
        isAnonymousConstructorInvoc || getReplacementString().endsWith(")"); // $NON-NLS-1$

    // construct replacement text: an expression to be formatted
    StringBuffer buf = new StringBuffer("new A("); // $NON-NLS-1$
    if (!replacementStringEndsWithParentheses || isAnonymousConstructorInvoc) buf.append(')');
    buf.append(newBody);

    // use the code formatter
    String lineDelim = TextUtilities.getDefaultLineDelimiter(document);
    final IJavaProject project = fCompilationUnit.getJavaProject();
    IRegion lineInfo = document.getLineInformationOfOffset(getReplacementOffset());
    int indent =
        Strings.computeIndentUnits(
            document.get(lineInfo.getOffset(), lineInfo.getLength()), project);

    Map<String, String> options =
        project != null ? project.getOptions(true) : JavaCore.getOptions();
    options.put(
        DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES,
        DefaultCodeFormatterConstants.TRUE);
    String replacementString =
        CodeFormatterUtil.format(CodeFormatter.K_EXPRESSION, buf.toString(), 0, lineDelim, options);

    int lineEndOffset = lineInfo.getOffset() + lineInfo.getLength();

    int p = offset;
    char ch = document.getChar(p);
    while (p < lineEndOffset) {
      if (ch == '(' || ch == ')' || ch == ';' || ch == ',') break;
      ch = document.getChar(++p);
    }

    if (ch != ';' && ch != ',' && ch != ')') replacementString = replacementString + ';';

    replacementString =
        Strings.changeIndent(
            replacementString,
            0,
            project,
            CodeFormatterUtil.createIndentString(indent, project),
            lineDelim);

    int beginIndex = replacementString.indexOf('(');
    if (!isAnonymousConstructorInvoc) beginIndex++;
    replacementString = replacementString.substring(beginIndex);

    int pos = offset;
    if (isAnonymousConstructorInvoc && (insertCompletion() ^ isInsertModeToggled())) {
      // Keep existing code
      int endPos = pos;
      ch = document.getChar(endPos);
      while (endPos < lineEndOffset
          && ch != '('
          && ch != ')'
          && ch != ';'
          && ch != ','
          && !Character.isWhitespace(ch)) ch = document.getChar(++endPos);

      int keepLength = endPos - pos;
      if (keepLength > 0) {
        String keepStr = document.get(pos, keepLength);
        replacementString = replacementString + keepStr;
        setCursorPosition(replacementString.length() - keepLength);
      }
    } else setCursorPosition(replacementString.length());

    setReplacementString(replacementString);

    if (pos < document.getLength() && document.getChar(pos) == ')') {
      int currentLength = getReplacementLength();
      if (replacementStringEndsWithParentheses) setReplacementLength(currentLength + pos - offset);
      else setReplacementLength(currentLength + pos - offset + 1);
    }
    return false;
  }

  /*
   * @see ICompletionProposalExtension#getContextInformationPosition()
   * @since 3.4
   */
  @Override
  public int getContextInformationPosition() {
    if (!fIsContextInformationComputed) setContextInformation(computeContextInformation());
    return fContextInformationPosition;
  }

  /*
   * @see ICompletionProposal#getContextInformation()
   * @since 3.4
   */
  @Override
  public final IContextInformation getContextInformation() {
    if (!fIsContextInformationComputed) setContextInformation(computeContextInformation());
    return super.getContextInformation();
  }

  protected IContextInformation computeContextInformation() {
    try {
      fContextInformationPosition = getReplacementOffset() - 1;

      CompletionProposal proposal = ((MemberProposalInfo) getProposalInfo()).fProposal;
      // no context information for METHOD_NAME_REF proposals (e.g. for static imports)
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=94654
      if (hasParameters()
          && (getReplacementString().endsWith(")")
              || getReplacementString().length() == 0)) { // $NON-NLS-1$
        ProposalContextInformation contextInformation = new ProposalContextInformation(proposal);
        fContextInformationPosition = getReplacementOffset() + getCursorPosition();
        if (fContextInformationPosition != 0 && proposal.getCompletion().length == 0)
          contextInformation.setContextInformationPosition(fContextInformationPosition);
        return contextInformation;
      }
      return null;
    } finally {
      fIsContextInformationComputed = true;
    }
  }

  /**
   * Returns <code>true</code> if the method being inserted has at least one parameter. Note that
   * this does not say anything about whether the argument list should be inserted.
   *
   * @return <code>true</code> if the method has any parameters, <code>false</code> if it has no
   *     parameters
   * @since 3.4
   */
  private boolean hasParameters() {
    CompletionProposal proposal = ((MemberProposalInfo) getProposalInfo()).fProposal;
    return Signature.getParameterCount(proposal.getSignature()) > 0;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal#createLazyJavaTypeCompletionProposal(org.eclipse.jdt.core.CompletionProposal, org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext)
   * @since 3.5
   */
  @Override
  protected LazyJavaCompletionProposal createRequiredTypeCompletionProposal(
      CompletionProposal completionProposal, JavaContentAssistInvocationContext invocationContext) {
    LazyJavaCompletionProposal proposal =
        super.createRequiredTypeCompletionProposal(completionProposal, invocationContext);
    if (proposal instanceof LazyJavaTypeCompletionProposal)
      ((LazyJavaTypeCompletionProposal) proposal).setImportRewrite(fImportRewrite);
    return proposal;
  }
}
