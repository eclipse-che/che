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
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;
import org.eclipse.jface.text.templates.TemplateContext;

/** The context type for templates inside Javadoc. */
public class JavaDocContextType extends CompilationUnitContextType {

  /**
   * The word selection variable determines templates that work on a full lines selection.
   *
   * <p>This class contains additional description that tells about the 'Source &gt; Surround With >
   * ...' menu.
   *
   * @since 3.7
   * @see org.eclipse.jface.text.templates.GlobalTemplateVariables.WordSelection
   */
  protected static class SurroundWithWordSelection extends SimpleTemplateVariableResolver {

    /** Creates a new word selection variable */
    public SurroundWithWordSelection() {
      super(
          org.eclipse.jface.text.templates.GlobalTemplateVariables.WordSelection.NAME,
          JavaTemplateMessages.JavaDocContextType_variable_description_word_selection);
    }

    @Override
    protected String resolve(TemplateContext context) {
      String selection =
          context.getVariable(org.eclipse.jface.text.templates.GlobalTemplateVariables.SELECTION);
      if (selection == null) return ""; // $NON-NLS-1$
      return selection;
    }
  }

  /** The id under which this context type is registered */
  public static final String ID = "javadoc"; // $NON-NLS-1$

  /** Creates a java context type. */
  public JavaDocContextType() {

    // global
    addResolver(new GlobalTemplateVariables.Cursor());
    addResolver(new SurroundWithLineSelection());
    addResolver(new SurroundWithWordSelection());
    addResolver(new GlobalTemplateVariables.Dollar());
    addResolver(new GlobalTemplateVariables.Date());
    addResolver(new GlobalTemplateVariables.Year());
    addResolver(new GlobalTemplateVariables.Time());
    addResolver(new GlobalTemplateVariables.User());

    // compilation unit
    addResolver(new File());
    addResolver(new PrimaryTypeName());
    addResolver(new Method());
    addResolver(new ReturnType());
    addResolver(new Arguments());
    addResolver(new Type());
    addResolver(new Package());
    addResolver(new Project());
  }

  /*
   * @see org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType#createContext(org.eclipse.jface.text.IDocument, int, int, org.eclipse.jdt.core.ICompilationUnit)
   */
  @Override
  public CompilationUnitContext createContext(
      IDocument document, int offset, int length, ICompilationUnit compilationUnit) {
    return new JavaDocContext(this, document, offset, length, compilationUnit);
  }

  /*
   * @see org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType#createContext(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.Position, org.eclipse.jdt.core.ICompilationUnit)
   */
  @Override
  public CompilationUnitContext createContext(
      IDocument document, Position completionPosition, ICompilationUnit compilationUnit) {
    return new JavaDocContext(this, document, completionPosition, compilationUnit);
  }
}
