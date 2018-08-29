/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.javadoc;

import org.eclipse.che.jdt.dom.ASTNodes;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a shared AST for clients. The shared AST is the AST of the active Java editor's input
 * element.
 *
 * @since 3.0
 */
public class ASTProvider {
  public static final int SHARED_AST_LEVEL = AST.JLS8;
  public static final boolean SHARED_AST_STATEMENT_RECOVERY = true;
  public static final boolean SHARED_BINDING_RECOVERY = true;
  private static final Logger LOG = LoggerFactory.getLogger(ASTProvider.class);
  private static final String DEBUG_PREFIX = "ASTProvider > "; // $NON-NLS-1$
  /**
   * Tells whether this class is in debug mode.
   *
   * @since 3.0
   */
  private static final boolean DEBUG = false;

  /**
   * Checks whether the given Java element has accessible source.
   *
   * @param je the Java element to test
   * @return <code>true</code> if the element has source
   * @since 3.2
   */
  private static boolean hasSource(ITypeRoot je) {
    if (je == null || !je.exists()) return false;

    try {
      return je.getBuffer() != null;
    } catch (JavaModelException ex) {
      LOG.error(ex.getMessage(), ex);
    }
    return false;
  }

  /**
   * Creates a new compilation unit AST.
   *
   * @param input the Java element for which to create the AST
   * @param progressMonitor the progress monitor
   * @return AST
   */
  public static CompilationUnit createAST(
      final ITypeRoot input, final IProgressMonitor progressMonitor) {
    if (!hasSource(input)) return null;

    if (progressMonitor != null && progressMonitor.isCanceled()) return null;

    final ASTParser parser = ASTParser.newParser(SHARED_AST_LEVEL);
    parser.setResolveBindings(true);
    parser.setStatementsRecovery(SHARED_AST_STATEMENT_RECOVERY);
    parser.setBindingsRecovery(SHARED_BINDING_RECOVERY);
    parser.setSource(input);

    if (progressMonitor != null && progressMonitor.isCanceled()) return null;

    final CompilationUnit root[] = new CompilationUnit[1];

    SafeRunner.run(
        new ISafeRunnable() {
          public void run() {
            try {
              if (progressMonitor != null && progressMonitor.isCanceled()) return;
              if (DEBUG)
                System.err.println(
                    getThreadName()
                        + " - "
                        + DEBUG_PREFIX
                        + "creating AST for: "
                        + input.getElementName()); // $NON-NLS-1$ //$NON-NLS-2$
              root[0] = (CompilationUnit) parser.createAST(progressMonitor);

              // mark as unmodifiable
              ASTNodes.setFlagsToAST(root[0], ASTNode.PROTECT);
            } catch (OperationCanceledException ex) {
              return;
            }
          }

          public void handleException(Throwable ex) {
            LOG.error(ex.getMessage(), ex);
          }
        });
    return root[0];
  }

  private static String getThreadName() {
    String name = Thread.currentThread().getName();
    if (name != null) return name;
    else return Thread.currentThread().toString();
  }
}
