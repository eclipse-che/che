/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.template.java;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;

/** A context for javadoc. */
public class JavaDocContext extends CompilationUnitContext {

  // tags
  private static final char HTML_TAG_BEGIN = '<';
  private static final char HTML_TAG_END = '>';
  private static final char JAVADOC_TAG_BEGIN = '@';

  /**
   * Creates a javadoc template context.
   *
   * @param type the context type.
   * @param document the document.
   * @param completionOffset the completion offset within the document.
   * @param completionLength the completion length within the document.
   * @param compilationUnit the compilation unit (may be <code>null</code>).
   */
  public JavaDocContext(
      TemplateContextType type,
      IDocument document,
      int completionOffset,
      int completionLength,
      ICompilationUnit compilationUnit) {
    super(type, document, completionOffset, completionLength, compilationUnit);
  }

  /**
   * Creates a javadoc template context.
   *
   * @param type the context type.
   * @param document the document.
   * @param completionPosition the position defining the completion offset and length
   * @param compilationUnit the compilation unit (may be <code>null</code>).
   * @since 3.2
   */
  public JavaDocContext(
      TemplateContextType type,
      IDocument document,
      Position completionPosition,
      ICompilationUnit compilationUnit) {
    super(type, document, completionPosition, compilationUnit);
  }

  /*
   * @see TemplateContext#canEvaluate(Template templates)
   */
  @Override
  public boolean canEvaluate(Template template) {
    String key = getKey();

    if (fForceEvaluation) return true;

    return template.matches(key, getContextType().getId())
        && (key.length() != 0)
        && template.getName().toLowerCase().startsWith(key.toLowerCase());
  }

  /*
   * @see DocumentTemplateContext#getStart()
   */
  @Override
  public int getStart() {
    if (fIsManaged && getCompletionLength() > 0) return super.getStart();

    try {
      IDocument document = getDocument();

      if (getCompletionLength() == 0) {
        int start = getCompletionOffset();

        if ((start != 0) && (document.getChar(start - 1) == HTML_TAG_END)) start--;

        while ((start != 0) && Character.isUnicodeIdentifierPart(document.getChar(start - 1)))
          start--;

        if ((start != 0) && Character.isUnicodeIdentifierStart(document.getChar(start - 1)))
          start--;

        // include html and javadoc tags
        if ((start != 0)
            && ((document.getChar(start - 1) == HTML_TAG_BEGIN)
                || (document.getChar(start - 1) == JAVADOC_TAG_BEGIN))) {
          start--;
        }

        return start;
      }

      int start = getCompletionOffset();
      int end = getCompletionOffset() + getCompletionLength();

      while (start != 0 && Character.isUnicodeIdentifierPart(document.getChar(start - 1))) start--;

      while (start != end && Character.isWhitespace(document.getChar(start))) start++;

      if (start == end) start = getCompletionOffset();

      return start;

    } catch (BadLocationException e) {
      return getCompletionOffset();
    }
  }

  /*
   * @see org.eclipse.jdt.internal.corext.template.DocumentTemplateContext#getEnd()
   */
  @Override
  public int getEnd() {

    if (fIsManaged || getCompletionLength() == 0) return super.getEnd();

    try {
      IDocument document = getDocument();

      int start = getCompletionOffset();
      int end = getCompletionOffset() + getCompletionLength();

      while (start != end && Character.isWhitespace(document.getChar(end - 1))) end--;

      return end;

    } catch (BadLocationException e) {
      return super.getEnd();
    }
  }

  /*
   * @see org.eclipse.jdt.internal.corext.template.DocumentTemplateContext#getKey()
   */
  @Override
  public String getKey() {

    if (getCompletionLength() == 0) return super.getKey();

    try {
      IDocument document = getDocument();

      int start = getStart();
      int end = getCompletionOffset();
      return start <= end ? document.get(start, end - start) : ""; // $NON-NLS-1$

    } catch (BadLocationException e) {
      return super.getKey();
    }
  }

  /*
   * @see TemplateContext#evaluate(Template)
   */
  @Override
  public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
    TemplateTranslator translator = new TemplateTranslator();
    TemplateBuffer buffer = translator.translate(template);

    getContextType().resolve(buffer, this);

    //		IPreferenceStore prefs= JavaPlugin.getDefault().getPreferenceStore();
    boolean useCodeFormatter =
        true; // prefs.getBoolean(PreferenceConstants.TEMPLATES_USE_CODEFORMATTER);

    IJavaProject project = getJavaProject();
    JavaFormatter formatter =
        new JavaFormatter(
            TextUtilities.getDefaultLineDelimiter(getDocument()),
            getIndentation(),
            useCodeFormatter,
            project);
    formatter.format(buffer, this);

    return buffer;
  }

  /**
   * Returns the indentation level at the position of code completion.
   *
   * @return the indentation level at the position of the code completion
   */
  private int getIndentation() {
    int start = getStart();
    IDocument document = getDocument();
    try {
      IRegion region = document.getLineInformationOfOffset(start);
      String lineContent = document.get(region.getOffset(), region.getLength());
      IJavaProject project = getJavaProject();
      return Strings.computeIndentUnits(lineContent, project);
    } catch (BadLocationException e) {
      return 0;
    }
  }
}
