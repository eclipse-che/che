/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.base;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.internal.corext.dom.Selection;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

/**
 * A Java element context that can be used to annotate a </code>RefactoringStatusEntry<code>
 * with detailed information about an error detected in an <code>IJavaElement</code>.
 */
public abstract class JavaStatusContext extends RefactoringStatusContext {

  private static class MemberSourceContext extends JavaStatusContext {
    private IMember fMember;

    private MemberSourceContext(IMember member) {
      fMember = member;
    }

    @Override
    public boolean isBinary() {
      return fMember.isBinary();
    }

    @Override
    public ICompilationUnit getCompilationUnit() {
      return fMember.getCompilationUnit();
    }

    @Override
    public IClassFile getClassFile() {
      return fMember.getClassFile();
    }

    @Override
    public ISourceRange getSourceRange() {
      try {
        return fMember.getSourceRange();
      } catch (JavaModelException e) {
        return new SourceRange(0, 0);
      }
    }
  }

  private static class ImportDeclarationSourceContext extends JavaStatusContext {
    private IImportDeclaration fImportDeclartion;

    private ImportDeclarationSourceContext(IImportDeclaration declaration) {
      fImportDeclartion = declaration;
    }

    @Override
    public boolean isBinary() {
      return false;
    }

    @Override
    public ICompilationUnit getCompilationUnit() {
      return (ICompilationUnit) fImportDeclartion.getParent().getParent();
    }

    @Override
    public IClassFile getClassFile() {
      return null;
    }

    @Override
    public ISourceRange getSourceRange() {
      try {
        return fImportDeclartion.getSourceRange();
      } catch (JavaModelException e) {
        return new SourceRange(0, 0);
      }
    }
  }

  private static class CompilationUnitSourceContext extends JavaStatusContext {
    private ICompilationUnit fCUnit;
    private ISourceRange fSourceRange;

    private CompilationUnitSourceContext(ICompilationUnit cunit, ISourceRange range) {
      fCUnit = cunit;
      fSourceRange = range;
      if (fSourceRange == null) fSourceRange = new SourceRange(0, 0);
    }

    @Override
    public boolean isBinary() {
      return false;
    }

    @Override
    public ICompilationUnit getCompilationUnit() {
      return fCUnit;
    }

    @Override
    public IClassFile getClassFile() {
      return null;
    }

    @Override
    public ISourceRange getSourceRange() {
      return fSourceRange;
    }

    @Override
    public String toString() {
      return getSourceRange() + " in " + super.toString(); // $NON-NLS-1$
    }
  }

  private static class ClassFileSourceContext extends JavaStatusContext {
    private IClassFile fClassFile;
    private ISourceRange fSourceRange;

    private ClassFileSourceContext(IClassFile classFile, ISourceRange range) {
      fClassFile = classFile;
      fSourceRange = range;
      if (fSourceRange == null) fSourceRange = new SourceRange(0, 0);
    }

    @Override
    public boolean isBinary() {
      return true;
    }

    @Override
    public ICompilationUnit getCompilationUnit() {
      return null;
    }

    @Override
    public IClassFile getClassFile() {
      return fClassFile;
    }

    @Override
    public ISourceRange getSourceRange() {
      return fSourceRange;
    }

    @Override
    public String toString() {
      return getSourceRange() + " in " + super.toString(); // $NON-NLS-1$
    }
  }

  /**
   * Creates an status entry context for the given member
   *
   * @param member the java member for which the context is supposed to be created
   * @return the status entry context or <code>null</code> if the context cannot be created
   */
  public static RefactoringStatusContext create(IMember member) {
    if (member == null || !member.exists()) return null;
    return new MemberSourceContext(member);
  }

  /**
   * Creates an status entry context for the given import declaration
   *
   * @param declaration the import declaration for which the context is supposed to be created
   * @return the status entry context or <code>null</code> if the context cannot be created
   */
  public static RefactoringStatusContext create(IImportDeclaration declaration) {
    if (declaration == null || !declaration.exists()) return null;
    return new ImportDeclarationSourceContext(declaration);
  }

  /**
   * Creates an status entry context for the given method binding
   *
   * @param method the method binding for which the context is supposed to be created
   * @return the status entry context or <code>Context.NULL_CONTEXT</code> if the context cannot be
   *     created
   */
  public static RefactoringStatusContext create(IMethodBinding method) {
    return create((IMethod) method.getJavaElement());
  }

  /**
   * Creates an status entry context for the given type root.
   *
   * @param typeRoot the type root containing the error
   * @return the status entry context or <code>Context.NULL_CONTEXT</code> if the context cannot be
   *     created
   */
  public static RefactoringStatusContext create(ITypeRoot typeRoot) {
    return create(typeRoot, (ISourceRange) null);
  }

  /**
   * Creates an status entry context for the given type root and source range.
   *
   * @param typeRoot the type root containing the error
   * @param range the source range that has caused the error or <code>null</code> if the source
   *     range is unknown
   * @return the status entry context or <code>null</code> if the context cannot be created
   */
  public static RefactoringStatusContext create(ITypeRoot typeRoot, ISourceRange range) {
    if (typeRoot instanceof ICompilationUnit)
      return new CompilationUnitSourceContext((ICompilationUnit) typeRoot, range);
    else if (typeRoot instanceof IClassFile)
      return new ClassFileSourceContext((IClassFile) typeRoot, range);
    else return null;
  }

  /**
   * Creates an status entry context for the given type root and AST node.
   *
   * @param typeRoot the type root containing the error
   * @param node an astNode denoting the source range that has caused the error
   * @return the status entry context or <code>Context.NULL_CONTEXT</code> if the context cannot be
   *     created
   */
  public static RefactoringStatusContext create(ITypeRoot typeRoot, ASTNode node) {
    ISourceRange range = null;
    if (node != null) range = new SourceRange(node.getStartPosition(), node.getLength());
    return create(typeRoot, range);
  }

  /**
   * Creates an status entry context for the given type root and selection.
   *
   * @param typeRoot the type root containing the error
   * @param selection a selection denoting the source range that has caused the error
   * @return the status entry context or <code>Context.NULL_CONTEXT</code> if the context cannot be
   *     created
   */
  public static RefactoringStatusContext create(ITypeRoot typeRoot, Selection selection) {
    ISourceRange range = null;
    if (selection != null) range = new SourceRange(selection.getOffset(), selection.getLength());
    return create(typeRoot, range);
  }

  /**
   * Returns whether this context is for a class file.
   *
   * @return <code>true</code> if from a class file, and <code>false</code> if from a compilation
   *     unit
   */
  public abstract boolean isBinary();

  /**
   * Returns the compilation unit this context is working on. Returns <code>null</code> if the
   * context is a binary context.
   *
   * @return the compilation unit
   */
  public abstract ICompilationUnit getCompilationUnit();

  /**
   * Returns the class file this context is working on. Returns <code>null</code> if the context is
   * not a binary context.
   *
   * @return the class file
   */
  public abstract IClassFile getClassFile();

  /**
   * Returns the source range associated with this element.
   *
   * @return the source range
   */
  public abstract ISourceRange getSourceRange();

  /* (non-Javadoc)
   * Method declared on Context.
   */
  @Override
  public Object getCorrespondingElement() {
    if (isBinary()) return getClassFile();
    else return getCompilationUnit();
  }
}
