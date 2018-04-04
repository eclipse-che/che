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
import org.eclipse.che.jdt.util.JdtFlags;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.GetterSetterUtil;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.StyledString;

public class GetterSetterCompletionProposal extends JavaTypeCompletionProposal
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
    if (prefix.length() == 0) {
      relevance--;
    }

    IField[] fields = type.getFields();
    IMethod[] methods = type.getMethods();
    for (int i = 0; i < fields.length; i++) {
      IField curr = fields[i];
      if (!JdtFlags.isEnum(curr)) {
        String getterName = GetterSetterUtil.getGetterName(curr, null);
        if (Strings.startsWithIgnoreCase(getterName, prefix) && !hasMethod(methods, getterName)) {
          suggestedMethods.add(getterName);
          int getterRelevance = relevance;
          if (JdtFlags.isStatic(curr) && JdtFlags.isFinal(curr)) getterRelevance = relevance - 1;
          result.add(
              new GetterSetterCompletionProposal(curr, offset, length, true, getterRelevance));
        }

        if (!JdtFlags.isFinal(curr)) {
          String setterName = GetterSetterUtil.getSetterName(curr, null);
          if (Strings.startsWithIgnoreCase(setterName, prefix) && !hasMethod(methods, setterName)) {
            suggestedMethods.add(setterName);
            result.add(new GetterSetterCompletionProposal(curr, offset, length, false, relevance));
          }
        }
      }
    }
  }

  private static boolean hasMethod(IMethod[] methods, String name) {
    for (int i = 0; i < methods.length; i++) {
      if (methods[i].getElementName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  private final IField fField;
  private final boolean fIsGetter;

  public GetterSetterCompletionProposal(
      IField field, int start, int length, boolean isGetter, int relevance)
      throws JavaModelException {
    super(
        "",
        field.getCompilationUnit(),
        start,
        length,
        JavaPluginImages.get(JavaPluginImages.DESC_MISC_PUBLIC),
        getDisplayName(field, isGetter),
        relevance); // $NON-NLS-1$
    Assert.isNotNull(field);

    fField = field;
    fIsGetter = isGetter;
    setProposalInfo(new ProposalInfo(field));
  }

  private static StyledString getDisplayName(IField field, boolean isGetter)
      throws JavaModelException {
    StyledString buf = new StyledString();
    String fieldTypeName = Signature.toString(field.getTypeSignature());
    String fieldNameLabel = BasicElementLabels.getJavaElementName(field.getElementName());
    if (isGetter) {
      buf.append(
          BasicElementLabels.getJavaElementName(
              GetterSetterUtil.getGetterName(field, null)
                  + "() : "
                  + fieldTypeName)); // $NON-NLS-1$
      buf.append(" - ", StyledString.QUALIFIER_STYLER); // $NON-NLS-1$
      buf.append(
          Messages.format(
              JavaTextMessages.GetterSetterCompletionProposal_getter_label, fieldNameLabel),
          StyledString.QUALIFIER_STYLER);
    } else {
      buf.append(
          BasicElementLabels.getJavaElementName(
              GetterSetterUtil.getSetterName(field, null)
                  + '('
                  + fieldTypeName
                  + ") : void")); // $NON-NLS-1$
      buf.append(" - ", StyledString.QUALIFIER_STYLER); // $NON-NLS-1$
      buf.append(
          Messages.format(
              JavaTextMessages.GetterSetterCompletionProposal_setter_label, fieldNameLabel),
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
        JavaPreferencesSettings.getCodeGenerationSettings(fField.getJavaProject());
    boolean addComments = settings.createComments;
    int flags = Flags.AccPublic | (fField.getFlags() & Flags.AccStatic);

    String stub;
    if (fIsGetter) {
      String getterName = GetterSetterUtil.getGetterName(fField, null);
      stub = GetterSetterUtil.getGetterStub(fField, getterName, addComments, flags);
    } else {
      String setterName = GetterSetterUtil.getSetterName(fField, null);
      stub = GetterSetterUtil.getSetterStub(fField, setterName, addComments, flags);
    }

    // use the code formatter
    String lineDelim = TextUtilities.getDefaultLineDelimiter(document);

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
            fField.getJavaProject());

    if (replacement.endsWith(lineDelim)) {
      replacement = replacement.substring(0, replacement.length() - lineDelim.length());
    }

    setReplacementString(Strings.trimLeadingTabsAndSpaces(replacement));
    return true;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension4#isAutoInsertable()
   */
  public boolean isAutoInsertable() {
    return false;
  }
}
