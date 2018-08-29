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

import java.util.Collection;
import java.util.Set;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.JavaPreferencesSettings;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.StyledString;

/** Method declaration proposal. */
public class MethodDeclarationCompletionProposal extends JavaTypeCompletionProposal
    implements ICompletionProposalExtension4 {

  public static void evaluateProposals(
      IType type,
      String prefix,
      int offset,
      int length,
      int relevance,
      Set<String> suggestedMethods,
      Collection<IJavaCompletionProposal> result)
      throws CoreException {
    IMethod[] methods = type.getMethods();
    if (!type.isInterface()) {
      String constructorName = type.getElementName();
      if (constructorName.length() > 0
          && constructorName.startsWith(prefix)
          && !hasMethod(methods, constructorName)
          && suggestedMethods.add(constructorName)) {
        result.add(
            new MethodDeclarationCompletionProposal(
                type, constructorName, null, offset, length, relevance + 500));
      }
    }

    if (prefix.length() > 0
        && !"main".equals(prefix)
        && !hasMethod(methods, prefix)
        && suggestedMethods.add(prefix)) { // $NON-NLS-1$
      if (!JavaConventionsUtil.validateMethodName(prefix, type).matches(IStatus.ERROR))
        result.add(
            new MethodDeclarationCompletionProposal(
                type, prefix, Signature.SIG_VOID, offset, length, relevance));
    }
  }

  private static boolean hasMethod(IMethod[] methods, String name) {
    for (int i = 0; i < methods.length; i++) {
      IMethod curr = methods[i];
      if (curr.getElementName().equals(name) && curr.getParameterTypes().length == 0) {
        return true;
      }
    }
    return false;
  }

  private final IType fType;
  private final String fReturnTypeSig;
  private final String fMethodName;

  public MethodDeclarationCompletionProposal(
      IType type, String methodName, String returnTypeSig, int start, int length, int relevance) {
    super(
        "",
        type.getCompilationUnit(),
        start,
        length,
        null,
        getDisplayName(methodName, returnTypeSig),
        relevance); // $NON-NLS-1$
    Assert.isNotNull(type);
    Assert.isNotNull(methodName);

    fType = type;
    fMethodName = methodName;
    fReturnTypeSig = returnTypeSig;

    if (returnTypeSig == null) {
      setProposalInfo(new ProposalInfo(type));

      //            ImageDescriptor desc = new
      // JavaElementImageDescriptor(JavaPluginImages.DESC_MISC_PUBLIC,
      // JavaElementImageDescriptor.CONSTRUCTOR,
      //
      // JavaElementImageProvider.SMALL_SIZE);
      //            setImage(JavaPlugin.getImageDescriptorRegistry().get(desc));
      setImage(JavaPluginImages.get(JavaPluginImages.DESC_MISC_PUBLIC));
    } else {
      setImage(JavaPluginImages.get(JavaPluginImages.DESC_MISC_PRIVATE));
    }
  }

  private static StyledString getDisplayName(String methodName, String returnTypeSig) {
    StyledString buf = new StyledString();
    buf.append(methodName);
    buf.append('(');
    buf.append(')');
    if (returnTypeSig != null) {
      buf.append(" : "); // $NON-NLS-1$
      buf.append(Signature.toString(returnTypeSig));
      buf.append(" - ", StyledString.QUALIFIER_STYLER); // $NON-NLS-1$
      buf.append(
          JavaTextMessages.MethodCompletionProposal_method_label, StyledString.QUALIFIER_STYLER);
    } else {
      buf.append(" - ", StyledString.QUALIFIER_STYLER); // $NON-NLS-1$
      buf.append(
          JavaTextMessages.MethodCompletionProposal_constructor_label,
          StyledString.QUALIFIER_STYLER);
    }
    return buf;
  }

  /* (non-Javadoc)
   * @see JavaTypeCompletionProposal#updateReplacementString(IDocument, char, int, ImportRewrite)
   */
  @Override
  protected boolean updateReplacementString(
      IDocument document, char trigger, int offset, ImportRewrite impRewrite)
      throws CoreException, BadLocationException {

    CodeGenerationSettings settings =
        JavaPreferencesSettings.getCodeGenerationSettings(fType.getJavaProject());
    boolean addComments = settings.createComments;

    String[] empty = new String[0];
    String lineDelim = TextUtilities.getDefaultLineDelimiter(document);
    String declTypeName = fType.getTypeQualifiedName('.');
    boolean isInterface = fType.isInterface();

    StringBuffer buf = new StringBuffer();
    if (addComments) {
      String comment =
          CodeGeneration.getMethodComment(
              fType.getCompilationUnit(),
              declTypeName,
              fMethodName,
              empty,
              empty,
              fReturnTypeSig,
              empty,
              null,
              lineDelim);
      if (comment != null) {
        buf.append(comment);
        buf.append(lineDelim);
      }
    }
    if (fReturnTypeSig != null) {
      if (!isInterface) {
        buf.append("private "); // $NON-NLS-1$
      }
    } else {
      if (fType.isEnum()) buf.append("private "); // $NON-NLS-1$
      else buf.append("public "); // $NON-NLS-1$
    }

    if (fReturnTypeSig != null) {
      buf.append(Signature.toString(fReturnTypeSig));
    }
    buf.append(' ');
    buf.append(fMethodName);
    if (isInterface) {
      buf.append("();"); // $NON-NLS-1$
      buf.append(lineDelim);
    } else {
      buf.append("() {"); // $NON-NLS-1$
      buf.append(lineDelim);

      String body =
          CodeGeneration.getMethodBodyContent(
              fType.getCompilationUnit(),
              declTypeName,
              fMethodName,
              fReturnTypeSig == null,
              "",
              lineDelim); // $NON-NLS-1$
      if (body != null) {
        buf.append(body);
        buf.append(lineDelim);
      }
      buf.append("}"); // $NON-NLS-1$
      buf.append(lineDelim);
    }
    String stub = buf.toString();

    // use the code formatter
    IRegion region = document.getLineInformationOfOffset(getReplacementOffset());
    int lineStart = region.getOffset();
    int indent =
        Strings.computeIndentUnits(
            document.get(lineStart, getReplacementOffset() - lineStart),
            settings.tabWidth,
            settings.indentWidth);

    String replacement =
        CodeFormatterUtil.format(
            CodeFormatter.K_CLASS_BODY_DECLARATIONS,
            stub,
            indent,
            lineDelim,
            fType.getJavaProject());

    if (replacement.endsWith(lineDelim)) {
      replacement = replacement.substring(0, replacement.length() - lineDelim.length());
    }

    setReplacementString(Strings.trimLeadingTabsAndSpaces(replacement));
    return true;
  }

  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    return new String(); // don't let method stub proposals complete incrementally
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension4#isAutoInsertable()
   */
  public boolean isAutoInsertable() {
    return false;
  }
}
