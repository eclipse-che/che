/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.javaeditor;

import java.util.List;
import org.eclipse.che.jdt.dom.ASTNodes;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CheASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
public class ASTProvider {
  public static final int SHARED_AST_LEVEL = AST.JLS8;
  public static final boolean SHARED_AST_STATEMENT_RECOVERY = true;
  public static final boolean SHARED_BINDING_RECOVERY = true;
  private static final Logger LOG = LoggerFactory.getLogger(ASTProvider.class);
  private static final String DEBUG_PREFIX = "ASTProvider > "; // $NON-NLS-1$
  private Object fReconcileLock = new Object();
  private CompilationUnit fAST;
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
   * Returns a string for the given Java element used for debugging.
   *
   * @param javaElement the compilation unit AST
   * @return a string used for debugging
   */
  private String toString(ITypeRoot javaElement) {
    if (javaElement == null) return "null"; // $NON-NLS-1$
    else return javaElement.getElementName();
  }

  /**
   * Returns a string for the given AST used for debugging.
   *
   * @param ast the compilation unit AST
   * @return a string used for debugging
   */
  private String toString(CompilationUnit ast) {
    if (ast == null) return "null"; // $NON-NLS-1$

    List<AbstractTypeDeclaration> types = ast.types();
    if (types != null && types.size() > 0)
      return types.get(0).getName().getIdentifier()
          + "("
          + ast.hashCode()
          + ")"; // $NON-NLS-1$//$NON-NLS-2$
    else return "AST without any type"; // $NON-NLS-1$
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

    final CheASTParser parser = CheASTParser.newParser(SHARED_AST_LEVEL);
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

  /**
   * Returns a shared compilation unit AST for the given Java element.
   *
   * <p>Clients are not allowed to modify the AST and must synchronize all access to its nodes.
   *
   * @param input the Java element, must not be <code>null</code>
   * @param waitFlag {@link SharedASTProvider#WAIT_YES}, {@link SharedASTProvider#WAIT_NO} or {@link
   *     SharedASTProvider#WAIT_ACTIVE_ONLY}
   * @param progressMonitor the progress monitor or <code>null</code>
   * @return the AST or <code>null</code> if the AST is not available
   */
  public CompilationUnit getAST(
      final ITypeRoot input,
      SharedASTProvider.WAIT_FLAG waitFlag,
      IProgressMonitor progressMonitor) {
    if (input == null || waitFlag == null)
      throw new IllegalArgumentException("input or wait flag are null"); // $NON-NLS-1$

    if (progressMonitor != null && progressMonitor.isCanceled()) return null;

    boolean isActiveElement;
    synchronized (this) {
      isActiveElement = false; // input.equals(fActiveJavaElement);
      if (isActiveElement) {
        if (fAST != null) {
          if (DEBUG)
            System.out.println(
                getThreadName()
                    + " - "
                    + DEBUG_PREFIX
                    + "returning cached AST:"
                    + toString(fAST)
                    + " for: "
                    + input.getElementName()); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

          return fAST;
        }
        if (waitFlag == SharedASTProvider.WAIT_NO) {
          if (DEBUG)
            System.out.println(
                getThreadName()
                    + " - "
                    + DEBUG_PREFIX
                    + "returning null (WAIT_NO) for: "
                    + input.getElementName()); // $NON-NLS-1$ //$NON-NLS-2$

          return null;
        }
      }
    }

    final boolean canReturnNull =
        waitFlag == SharedASTProvider.WAIT_NO
            || (waitFlag == SharedASTProvider.WAIT_ACTIVE_ONLY
                && !(isActiveElement && fAST == null));
    boolean isReconciling = false;
    final ITypeRoot activeElement;
    if (isActiveElement) {
      //            synchronized (fReconcileLock) {
      //                activeElement= fReconcilingJavaElement;
      //                isReconciling= isReconciling(input);
      //                if (!isReconciling && !canReturnNull)
      //                    aboutToBeReconciled(input);
      //            }
    } else activeElement = null;

    if (isReconciling) {
      // Wait for AST
      //                synchronized (fWaitLock) {
      //                    if (isReconciling(input)) {
      //                        if (DEBUG)
      //                            System.out.println(getThreadName() + " - " + DEBUG_PREFIX +
      // "waiting for AST for: " + input.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$

      //                        fWaitLock.wait(30000); // XXX: The 30 seconds timeout is an attempt
      // to at least avoid a deadlock. See https://bugs.eclipse.org/366048#c21
      //                    }
      //                }

      // Check whether active element is still valid
      //                synchronized (this) {
      //                    if (activeElement == fActiveJavaElement && fAST != null) {
      //                        if (DEBUG)
      //                            System.out.println(getThreadName() + " - " + DEBUG_PREFIX +
      // "...got AST: " + toString(fAST) + " for: " + input.getElementName()); //$NON-NLS-1$
      // //$NON-NLS-2$ //$NON-NLS-3$
      //
      //                        return fAST;
      //                    }
      //                }
      return getAST(input, waitFlag, progressMonitor);
    } /*else if (canReturnNull)
      return null;*/

    CompilationUnit ast = null;
    try {
      ast = createAST(input, progressMonitor);
      if (progressMonitor != null && progressMonitor.isCanceled()) {
        ast = null;
        if (DEBUG)
          System.out.println(
              getThreadName()
                  + " - "
                  + DEBUG_PREFIX
                  + "Ignore created AST for: "
                  + input.getElementName()
                  + " - operation has been cancelled"); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    } finally {
      if (isActiveElement) {
        if (fAST != null) {
          // in the meantime, reconcile created a new AST. Return that one
          if (DEBUG)
            System.out.println(
                getThreadName()
                    + " - "
                    + DEBUG_PREFIX
                    + "Ignore created AST for "
                    + input.getElementName()
                    + " - AST from reconciler is newer"); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          reconciled(fAST, input, null);
          return fAST;
        } else reconciled(ast, input, null);
      }
    }
    return ast;
  }

  /**
   * Update internal structures after reconcile.
   *
   * @param ast the compilation unit AST or <code>null</code> if the working copy was consistent or
   *     reconciliation has been cancelled
   * @param javaElement the Java element for which the AST was built
   * @param progressMonitor the progress monitor
   * @see org.eclipse.jdt.internal.ui.text.java.IJavaReconcilingListener#reconciled(CompilationUnit,
   *     boolean, IProgressMonitor)
   */
  void reconciled(CompilationUnit ast, ITypeRoot javaElement, IProgressMonitor progressMonitor) {
    //        if (DEBUG)
    //            System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "reconciled: " +
    // toString(javaElement) + ", AST: " + toString(ast)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    //
    //        synchronized (fReconcileLock) {
    //            fIsReconciling= false;
    //            if (javaElement == null || !javaElement.equals(fReconcilingJavaElement)) {
    //
    //                if (DEBUG)
    //                    System.out.println(getThreadName() + " - " + DEBUG_PREFIX + "  ignoring
    // AST of out-dated editor"); //$NON-NLS-1$ //$NON-NLS-2$
    //
    //                // Signal - threads might wait for wrong element
    //                synchronized (fWaitLock) {
    //                    fWaitLock.notifyAll();
    //                }
    //
    //                return;
    //            }
    //            cache(ast, javaElement);
    //        }
  }
}
