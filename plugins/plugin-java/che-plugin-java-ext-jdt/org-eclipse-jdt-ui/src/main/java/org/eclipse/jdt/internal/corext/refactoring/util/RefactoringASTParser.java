/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.util;

import java.util.Iterator;
import java.util.Map;
import org.eclipse.che.jdt.util.JavaModelUtil;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.ui.SharedASTProvider;

public class RefactoringASTParser {

  private ASTParser fParser;

  public RefactoringASTParser(int level) {
    fParser = ASTParser.newParser(level);
  }

  public CompilationUnit parse(ITypeRoot typeRoot, boolean resolveBindings) {
    return parse(typeRoot, resolveBindings, null);
  }

  public CompilationUnit parse(ITypeRoot typeRoot, boolean resolveBindings, IProgressMonitor pm) {
    return parse(typeRoot, null, resolveBindings, pm);
  }

  public CompilationUnit parse(
      ITypeRoot typeRoot, WorkingCopyOwner owner, boolean resolveBindings, IProgressMonitor pm) {
    return parse(typeRoot, owner, resolveBindings, false, false, pm);
  }

  public CompilationUnit parse(
      ITypeRoot typeRoot,
      WorkingCopyOwner owner,
      boolean resolveBindings,
      boolean statementsRecovery,
      boolean bindingsRecovery,
      IProgressMonitor pm) {
    fParser.setResolveBindings(resolveBindings);
    fParser.setStatementsRecovery(statementsRecovery);
    fParser.setBindingsRecovery(bindingsRecovery);
    fParser.setSource(typeRoot);
    if (owner != null) fParser.setWorkingCopyOwner(owner);
    fParser.setCompilerOptions(getCompilerOptions(typeRoot));
    CompilationUnit result = (CompilationUnit) fParser.createAST(pm);
    return result;
  }

  /**
   * @param newCuSource the source
   * @param originalCu the compilation unit to get the name and project from
   * @param resolveBindings whether bindings are to be resolved
   * @param recovery whether statements and binding recovery should be enabled
   * @param pm an {@link IProgressMonitor}, or <code>null</code>
   * @return the parsed CompilationUnit
   */
  public CompilationUnit parse(
      String newCuSource,
      ICompilationUnit originalCu,
      boolean resolveBindings,
      boolean recovery,
      IProgressMonitor pm) {
    fParser.setResolveBindings(resolveBindings);
    fParser.setStatementsRecovery(recovery);
    fParser.setBindingsRecovery(recovery);
    fParser.setSource(newCuSource.toCharArray());
    fParser.setUnitName(originalCu.getElementName());
    fParser.setProject(originalCu.getJavaProject());
    fParser.setCompilerOptions(getCompilerOptions(originalCu));
    CompilationUnit newCUNode = (CompilationUnit) fParser.createAST(pm);
    return newCUNode;
  }

  /**
   * @param newCfSource the source
   * @param originalCf the class file to get the name and project from
   * @param resolveBindings whether bindings are to be resolved
   * @param recovery whether statements and binding recovery should be enabled
   * @param pm an {@link IProgressMonitor}, or <code>null</code>
   * @return the parsed CompilationUnit
   */
  public CompilationUnit parse(
      String newCfSource,
      IClassFile originalCf,
      boolean resolveBindings,
      boolean recovery,
      IProgressMonitor pm) {
    fParser.setResolveBindings(resolveBindings);
    fParser.setStatementsRecovery(recovery);
    fParser.setBindingsRecovery(recovery);
    fParser.setSource(newCfSource.toCharArray());
    String cfName = originalCf.getElementName();
    fParser.setUnitName(cfName.substring(0, cfName.length() - 6) + JavaModelUtil.DEFAULT_CU_SUFFIX);
    fParser.setProject(originalCf.getJavaProject());
    fParser.setCompilerOptions(getCompilerOptions(originalCf));
    CompilationUnit newCUNode = (CompilationUnit) fParser.createAST(pm);
    return newCUNode;
  }

  /**
   * Tries to get the shared AST from the ASTProvider. If the shared AST is not available, parses
   * the type root with a RefactoringASTParser that uses settings similar to the ASTProvider.
   *
   * @param typeRoot the type root
   * @param resolveBindings whether bindings are to be resolved if a new AST needs to be created
   * @param pm an {@link IProgressMonitor}, or <code>null</code>
   * @return the parsed CompilationUnit
   */
  public static CompilationUnit parseWithASTProvider(
      ITypeRoot typeRoot, boolean resolveBindings, IProgressMonitor pm) {
    CompilationUnit cuNode =
        SharedASTProvider.getAST(typeRoot, SharedASTProvider.WAIT_ACTIVE_ONLY, pm);
    if (cuNode != null) {
      return cuNode;
    } else {
      return new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL)
          .parse(
              typeRoot,
              null,
              resolveBindings,
              ASTProvider.SHARED_AST_STATEMENT_RECOVERY,
              ASTProvider.SHARED_BINDING_RECOVERY,
              pm);
    }
  }

  public static ICompilationUnit getCompilationUnit(ASTNode node) {
    ASTNode root = node.getRoot();
    if (root instanceof CompilationUnit) {
      IJavaElement cu = ((CompilationUnit) root).getJavaElement();
      if (cu instanceof ICompilationUnit) return (ICompilationUnit) cu;
    }
    return null;
  }

  /**
   * Returns the compiler options used for creating the refactoring AST.
   *
   * <p>Turns all errors and warnings into ignore and disables task tags. The customizable set of
   * compiler options only contains additional Eclipse options. The standard JDK compiler options
   * can't be changed anyway.
   *
   * @param element an element (not the Java model)
   * @return compiler options
   */
  public static Map<String, String> getCompilerOptions(IJavaElement element) {
    IJavaProject project = element.getJavaProject();
    Map<String, String> options = project.getOptions(true);
    for (Iterator<String> iter = options.keySet().iterator(); iter.hasNext(); ) {
      String key = iter.next();
      String value = options.get(key);
      if (JavaCore.ERROR.equals(value) || JavaCore.WARNING.equals(value)) {
        // System.out.println("Ignoring - " + key);
        options.put(key, JavaCore.IGNORE);
      }
    }
    options.put(JavaCore.COMPILER_PB_MAX_PER_UNIT, "0"); // $NON-NLS-1$
    options.put(JavaCore.COMPILER_TASK_TAGS, ""); // $NON-NLS-1$
    return options;
  }
}
