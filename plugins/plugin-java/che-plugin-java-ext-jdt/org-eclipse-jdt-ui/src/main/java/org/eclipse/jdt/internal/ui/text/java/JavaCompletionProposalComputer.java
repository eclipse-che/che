/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.che.jface.text.ITextViewer;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.che.jface.text.contentassist.IContextInformation;
import org.eclipse.che.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner;
import org.eclipse.jdt.internal.ui.text.Symbols;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Computes Java completion proposals and context infos.
 *
 * @since 3.2
 */
public class JavaCompletionProposalComputer implements IJavaCompletionProposalComputer {

  private static final class ContextInformationWrapper
      implements IContextInformation, IContextInformationExtension {

    private final IContextInformation fContextInformation;
    private int fPosition;

    public ContextInformationWrapper(IContextInformation contextInformation) {
      fContextInformation = contextInformation;
    }

    /*
     * @see IContextInformation#getContextDisplayString()
     */
    public String getContextDisplayString() {
      return fContextInformation.getContextDisplayString();
    }

    /*
     * @see IContextInformation#getImage()
     */
    public Image getImage() {
      return fContextInformation.getImage();
    }

    /*
     * @see IContextInformation#getInformationDisplayString()
     */
    public String getInformationDisplayString() {
      return fContextInformation.getInformationDisplayString();
    }

    /*
     * @see IContextInformationExtension#getContextInformationPosition()
     */
    public int getContextInformationPosition() {
      return fPosition;
    }

    public void setContextInformationPosition(int position) {
      fPosition = position;
    }

    /*
     * @see org.eclipse.jface.text.contentassist.IContextInformation#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
      if (object instanceof ContextInformationWrapper)
        return fContextInformation.equals(((ContextInformationWrapper) object).fContextInformation);
      else return fContextInformation.equals(object);
    }

    /*
     * @see java.lang.Object#hashCode()
     * @since 3.5
     */
    @Override
    public int hashCode() {
      return fContextInformation.hashCode();
    }
  }

  private static final long JAVA_CODE_ASSIST_TIMEOUT =
      Long.getLong("org.eclipse.jdt.ui.codeAssistTimeout", 5000).longValue();
  // ms //$NON-NLS-1$

  private String fErrorMessage;

  private final IProgressMonitor fTimeoutProgressMonitor;

  public JavaCompletionProposalComputer() {
    fTimeoutProgressMonitor = createTimeoutProgressMonitor(JAVA_CODE_ASSIST_TIMEOUT);
  }

  protected int guessContextInformationPosition(ContentAssistInvocationContext context) {
    return context.getInvocationOffset();
  }

  protected final int guessMethodContextInformationPosition(
      ContentAssistInvocationContext context) {
    final int contextPosition = context.getInvocationOffset();

    IDocument document = context.getDocument();
    JavaHeuristicScanner scanner = new JavaHeuristicScanner(document);
    int bound = Math.max(-1, contextPosition - 2000);

    // try the innermost scope of parentheses that looks like a method call
    int pos = contextPosition - 1;
    do {
      int paren = scanner.findOpeningPeer(pos, bound, '(', ')');
      if (paren == JavaHeuristicScanner.NOT_FOUND) break;
      int token = scanner.previousToken(paren - 1, bound);
      // next token must be a method name (identifier) or the closing angle of a
      // constructor call of a parameterized type.
      if (token == Symbols.TokenIDENT || token == Symbols.TokenGREATERTHAN) return paren + 1;
      pos = paren - 1;
    } while (true);

    return contextPosition;
  }

  private List<IContextInformation> addContextInformations(
      JavaContentAssistInvocationContext context, int offset) {
    List<ICompletionProposal> proposals = internalComputeCompletionProposals(offset, context);
    List<IContextInformation> result = new ArrayList<IContextInformation>(proposals.size());
    List<IContextInformation> anonymousResult =
        new ArrayList<IContextInformation>(proposals.size());

    for (Iterator<ICompletionProposal> it = proposals.iterator(); it.hasNext(); ) {
      ICompletionProposal proposal = it.next();
      IContextInformation contextInformation = proposal.getContextInformation();
      if (contextInformation != null) {
        ContextInformationWrapper wrapper = new ContextInformationWrapper(contextInformation);
        wrapper.setContextInformationPosition(offset);
        if (proposal instanceof AnonymousTypeCompletionProposal) anonymousResult.add(wrapper);
        else result.add(wrapper);
      }
    }

    if (result.size() == 0) return anonymousResult;
    return result;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#computeContextInformation(org.eclipse.jface.text
   * .contentassist.TextContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
   */
  public List<IContextInformation> computeContextInformation(
      ContentAssistInvocationContext context, IProgressMonitor monitor) {
    if (context instanceof JavaContentAssistInvocationContext) {
      JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;

      int contextInformationPosition = guessContextInformationPosition(javaContext);
      List<IContextInformation> result =
          addContextInformations(javaContext, contextInformationPosition);
      return result;
    }
    return Collections.emptyList();
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#computeCompletionProposals(org.eclipse.jface.text
   * .contentassist.TextContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
   */
  public List<ICompletionProposal> computeCompletionProposals(
      ContentAssistInvocationContext context, IProgressMonitor monitor) {
    if (context instanceof JavaContentAssistInvocationContext) {
      JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
      return internalComputeCompletionProposals(context.getInvocationOffset(), javaContext);
    }
    return Collections.emptyList();
  }

  private List<ICompletionProposal> internalComputeCompletionProposals(
      int offset, JavaContentAssistInvocationContext context) {
    ICompilationUnit unit = context.getCompilationUnit();
    if (unit == null) return Collections.emptyList();

    ITextViewer viewer = context.getViewer();

    CompletionProposalCollector collector = createCollector(context);
    collector.setInvocationContext(context);

    // Allow completions for unresolved types - since 3.3
    collector.setAllowsRequiredProposals(
        CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
    collector.setAllowsRequiredProposals(
        CompletionProposal.FIELD_REF, CompletionProposal.TYPE_IMPORT, true);
    collector.setAllowsRequiredProposals(
        CompletionProposal.FIELD_REF, CompletionProposal.FIELD_IMPORT, true);

    collector.setAllowsRequiredProposals(
        CompletionProposal.METHOD_REF, CompletionProposal.TYPE_REF, true);
    collector.setAllowsRequiredProposals(
        CompletionProposal.METHOD_REF, CompletionProposal.TYPE_IMPORT, true);
    collector.setAllowsRequiredProposals(
        CompletionProposal.METHOD_REF, CompletionProposal.METHOD_IMPORT, true);

    collector.setAllowsRequiredProposals(
        CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);

    collector.setAllowsRequiredProposals(
        CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION,
        CompletionProposal.TYPE_REF,
        true);
    collector.setAllowsRequiredProposals(
        CompletionProposal.ANONYMOUS_CLASS_DECLARATION, CompletionProposal.TYPE_REF, true);

    collector.setAllowsRequiredProposals(
        CompletionProposal.TYPE_REF, CompletionProposal.TYPE_REF, true);

    // Set the favorite list to propose static members - since 3.3
    collector.setFavoriteReferences(getFavoriteStaticMembers());

    try {
      Point selection = viewer.getSelectedRange();
      if (selection.y > 0) collector.setReplacementLength(selection.y);
      unit.codeComplete(offset, collector, fTimeoutProgressMonitor);
    } catch (OperationCanceledException x) {
      //			IBindingService bindingSvc=
      // (IBindingService)PlatformUI.getWorkbench().getAdapter(IBindingService.class);
      //			String keyBinding=
      // bindingSvc.getBestActiveBindingFormattedFor(IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST);
      //			fErrorMessage=
      // Messages.format(JavaTextMessages.CompletionProcessor_error_javaCompletion_took_too_long_message, keyBinding);
      JavaPlugin.log(x);
    } catch (JavaModelException x) {
      //			Shell shell= viewer.getTextWidget().getShell();
      //			if (x.isDoesNotExist() && !unit.getJavaProject().isOnClasspath(unit))
      //				MessageDialog.openInformation(shell,
      // JavaTextMessages.CompletionProcessor_error_notOnBuildPath_title,
      // JavaTextMessages.CompletionProcessor_error_notOnBuildPath_message);
      //			else
      //				ErrorDialog.openError(shell, JavaTextMessages.CompletionProcessor_error_accessing_title,
      // JavaTextMessages.CompletionProcessor_error_accessing_message, x.getStatus());
      JavaPlugin.log(x);
    }

    ICompletionProposal[] javaProposals = collector.getJavaCompletionProposals();
    int contextInformationOffset = guessMethodContextInformationPosition(context);
    if (contextInformationOffset != offset) {
      for (int i = 0; i < javaProposals.length; i++) {
        if (javaProposals[i] instanceof JavaMethodCompletionProposal) {
          JavaMethodCompletionProposal jmcp = (JavaMethodCompletionProposal) javaProposals[i];
          jmcp.setContextInformationPosition(contextInformationOffset);
        }
      }
    }

    List<ICompletionProposal> proposals =
        new ArrayList<ICompletionProposal>(Arrays.asList(javaProposals));
    if (proposals.size() == 0) {
      String error = collector.getErrorMessage();
      if (error.length() > 0) fErrorMessage = error;
    }
    return proposals;
  }

  /**
   * Returns a new progress monitor that get cancelled after the given timeout.
   *
   * @param timeout the timeout in ms
   * @return the progress monitor
   * @since 3.5
   */
  private IProgressMonitor createTimeoutProgressMonitor(final long timeout) {
    return new IProgressMonitor() {

      private long fEndTime;

      public void beginTask(String name, int totalWork) {
        fEndTime = System.currentTimeMillis() + timeout;
      }

      public boolean isCanceled() {
        return fEndTime <= System.currentTimeMillis();
      }

      public void done() {}

      public void internalWorked(double work) {}

      public void setCanceled(boolean value) {}

      public void setTaskName(String name) {}

      public void subTask(String name) {}

      public void worked(int work) {}
    };
  }

  /**
   * Returns the array with favorite static members.
   *
   * @return the <code>String</code> array with with favorite static members
   * @see org.eclipse.jdt.core.CompletionRequestor#setFavoriteReferences(String[])
   * @since 3.3
   */
  private String[] getFavoriteStaticMembers() {
    // todo
    //		String serializedFavorites=
    // PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS);
    //		if (serializedFavorites != null && serializedFavorites.length() > 0)
    //			return serializedFavorites.split(";"); //$NON-NLS-1$

    return new String[0];
  }

  /**
   * Creates the collector used to get proposals from core.
   *
   * @param context the context
   * @return the collector
   */
  protected CompletionProposalCollector createCollector(
      JavaContentAssistInvocationContext context) {
    //		if
    // (PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES))
    return new FillArgumentNamesCompletionProposalCollector(context);
    //		else
    //			return new CompletionProposalCollector(context.getCompilationUnit(), true);
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#getErrorMessage()
   */
  public String getErrorMessage() {
    return fErrorMessage;
  }

  /*
   * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#sessionStarted()
   */
  public void sessionStarted() {}

  /*
   * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#sessionEnded()
   */
  public void sessionEnded() {
    fErrorMessage = null;
  }
}
