/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.text.java;

import org.eclipse.che.jface.text.ITextViewer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.ContentAssistHistory.RHSHistory;

/**
 * Describes the context of a content assist invocation in a Java editor.
 *
 * <p>Clients may use but not subclass this class.
 *
 * @since 3.2
 * @noextend This class is not intended to be subclassed by clients.
 */
public class JavaContentAssistInvocationContext extends ContentAssistInvocationContext {
  private final ICompilationUnit fEditor;

  private IJavaProject fJavaProject;
  private ICompilationUnit fCU = null;
  private boolean fCUComputed = false;

  private CompletionProposalLabelProvider fLabelProvider;
  private CompletionProposalCollector fCollector;
  private RHSHistory fRHSHistory;
  private IType fType;

  private IJavaCompletionProposal[] fKeywordProposals = null;
  private CompletionContext fCoreContext = null;

  /**
   * Creates a new context.
   *
   * @param viewer the viewer used by the editor
   * @param offset the invocation offset
   * @param unit the editor that content assist is invoked in
   */
  public JavaContentAssistInvocationContext(ITextViewer viewer, int offset, ICompilationUnit unit) {
    super(viewer, offset);
    Assert.isNotNull(unit);
    fEditor = unit;
  }

  /**
   * Creates a new context.
   *
   * @param unit the compilation unit in <code>document</code>
   */
  public JavaContentAssistInvocationContext(ICompilationUnit unit) {
    super();
    fCU = unit;
    fCUComputed = true;
    fEditor = null;
  }

  /**
   * Creates a new context.
   *
   * @param javaProject the Java project
   * @since 3.9
   */
  public JavaContentAssistInvocationContext(IJavaProject javaProject) {
    super();
    fJavaProject = javaProject;
    fEditor = null;
  }

  /**
   * Returns the compilation unit that content assist is invoked in, <code>null</code> if there is
   * none.
   *
   * @return the compilation unit that content assist is invoked in, possibly <code>null</code>
   */
  public ICompilationUnit getCompilationUnit() {
    if (!fCUComputed) {
      fCUComputed = true;
      if (fCollector != null) fCU = fCollector.getCompilationUnit();
      else {
        //                IJavaElement je = EditorUtility.getEditorInputJavaElement(fEditor, false);
        //                if (je instanceof ICompilationUnit)
        //                    fCU = (ICompilationUnit)je;
        fCU = fEditor;
      }
    }
    return fCU;
  }

  /**
   * Returns the project of the compilation unit that content assist is invoked in, <code>null
   * </code> if none.
   *
   * @return the current java project, possibly <code>null</code>
   */
  public IJavaProject getProject() {
    ICompilationUnit unit = getCompilationUnit();
    return unit == null ? fJavaProject : unit.getJavaProject();
  }

  /**
   * Returns the keyword proposals that are available in this context, possibly none.
   *
   * <p><strong>Note:</strong> This method may run {@linkplain
   * org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor)
   * codeComplete} on the compilation unit.
   *
   * @return the available keyword proposals
   */
  public IJavaCompletionProposal[] getKeywordProposals() {
    if (fKeywordProposals == null) {
      if (fCollector != null
          && !fCollector.isIgnored(CompletionProposal.KEYWORD)
          && fCollector.getContext() != null) {
        // use the existing collector if it exists, collects keywords, and has already been invoked
        fKeywordProposals = fCollector.getKeywordCompletionProposals();
      } else {
        // otherwise, retrieve keywords ourselves
        computeKeywordsAndContext();
      }
    }

    return fKeywordProposals;
  }

  /**
   * Returns the {@link org.eclipse.jdt.core.CompletionContext core completion context} if
   * available, <code>null</code> otherwise.
   *
   * <p><strong>Note:</strong> This method may run {@linkplain
   * org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor)
   * codeComplete} on the compilation unit.
   *
   * @return the core completion context if available, <code>null</code> otherwise
   */
  public CompletionContext getCoreContext() {
    if (fCollector != null) {
      CompletionContext context = fCollector.getContext();
      if (context != null) {
        if (fCoreContext == null) fCoreContext = context;
        return context;
      }
    }

    if (fCoreContext == null) computeKeywordsAndContext(); // Retrieve the context ourselves

    return fCoreContext;
  }

  /**
   * Returns an float in [0.0,&nbsp;1.0] based on whether the type has been recently used as a right
   * hand side for the type expected in the current context. 0 signals that the <code>
   * qualifiedTypeName</code> does not match the expected type, while 1.0 signals that <code>
   * qualifiedTypeName</code> has most recently been used in a similar context.
   *
   * <p><strong>Note:</strong> This method may run {@linkplain
   * org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor)
   * codeComplete} on the compilation unit.
   *
   * @param qualifiedTypeName the type name of the type of interest
   * @return a relevance in [0.0,&nbsp;1.0] based on previous content assist invocations
   */
  public float getHistoryRelevance(String qualifiedTypeName) {
    return getRHSHistory().getRank(qualifiedTypeName);
  }

  /**
   * Returns the content assist type history for the expected type.
   *
   * @return the content assist type history for the expected type
   */
  private RHSHistory getRHSHistory() {
    if (fRHSHistory == null) {
      CompletionContext context = getCoreContext();
      if (context != null) {
        char[][] expectedTypes = context.getExpectedTypesSignatures();
        if (expectedTypes != null && expectedTypes.length > 0) {
          String expected = SignatureUtil.stripSignatureToFQN(String.valueOf(expectedTypes[0]));
          fRHSHistory = JavaPlugin.getDefault().getContentAssistHistory().getHistory(expected);
        }
      }
      if (fRHSHistory == null)
        fRHSHistory = JavaPlugin.getDefault().getContentAssistHistory().getHistory(null);
    }
    return fRHSHistory;
  }

  /**
   * Returns the expected type if any, <code>null</code> otherwise.
   *
   * <p><strong>Note:</strong> This method may run {@linkplain
   * org.eclipse.jdt.core.ICodeAssist#codeComplete(int, org.eclipse.jdt.core.CompletionRequestor)
   * codeComplete} on the compilation unit.
   *
   * @return the expected type if any, <code>null</code> otherwise
   */
  public IType getExpectedType() {
    if (fType == null && getCompilationUnit() != null) {
      CompletionContext context = getCoreContext();
      if (context != null) {
        char[][] expectedTypes = context.getExpectedTypesSignatures();
        if (expectedTypes != null && expectedTypes.length > 0) {
          IJavaProject project = getCompilationUnit().getJavaProject();
          if (project != null) {
            try {
              fType =
                  project.findType(
                      SignatureUtil.stripSignatureToFQN(String.valueOf(expectedTypes[0])));
            } catch (JavaModelException x) {
              JavaPlugin.log(x);
            }
          }
        }
      }
    }
    return fType;
  }

  /**
   * Returns a label provider that can be used to compute proposal labels.
   *
   * @return a label provider that can be used to compute proposal labels
   */
  public CompletionProposalLabelProvider getLabelProvider() {
    if (fLabelProvider == null) {
      if (fCollector != null) fLabelProvider = fCollector.getLabelProvider();
      else fLabelProvider = new CompletionProposalLabelProvider();
    }

    return fLabelProvider;
  }

  /**
   * Sets the collector, which is used to access the compilation unit, the core context and the
   * label provider. This is a performance optimization: {@link IJavaCompletionProposalComputer}s
   * may instantiate a {@link CompletionProposalCollector} and set this invocation context via
   * {@link CompletionProposalCollector#setInvocationContext(JavaContentAssistInvocationContext)},
   * which in turn calls this method. This allows the invocation context to retrieve the core
   * context and keyword proposals from the existing collector, instead of computing theses values
   * itself via {@link #computeKeywordsAndContext()}.
   *
   * @param collector the collector
   */
  void setCollector(CompletionProposalCollector collector) {
    fCollector = collector;
  }

  /**
   * Fallback to retrieve a core context and keyword proposals when no collector is available. Runs
   * code completion on the cu and collects keyword proposals. {@link #fKeywordProposals} is non-
   * <code>null</code> after this call.
   *
   * @since 3.3
   */
  private void computeKeywordsAndContext() {
    ICompilationUnit cu = getCompilationUnit();
    if (cu == null) {
      if (fKeywordProposals == null) fKeywordProposals = new IJavaCompletionProposal[0];
      return;
    }

    CompletionProposalCollector collector = new CompletionProposalCollector(cu, true);
    collector.setIgnored(CompletionProposal.KEYWORD, false);

    try {
      cu.codeComplete(getInvocationOffset(), collector);
      if (fCoreContext == null) fCoreContext = collector.getContext();
      if (fKeywordProposals == null) fKeywordProposals = collector.getKeywordCompletionProposals();
      if (fLabelProvider == null) fLabelProvider = collector.getLabelProvider();
    } catch (JavaModelException x) {
      if (!x.isDoesNotExist()
          || cu.getJavaProject() == null
          || cu.getJavaProject().isOnClasspath(cu)) JavaPlugin.log(x);
      if (fKeywordProposals == null) fKeywordProposals = new IJavaCompletionProposal[0];
    }
  }

  /*
   * Implementation note: There is no need to override hashCode and equals, as we only add cached
   * values shared across one assist invocation.
   */
}
