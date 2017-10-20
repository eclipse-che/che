/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.JavaHeuristicScanner;
import org.eclipse.jdt.internal.ui.text.Symbols;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/** @since 3.2 */
public class JavaTypeCompletionProposalComputer extends JavaCompletionProposalComputer {
  /*
   * @see org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer#createCollector(org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext)
   */
  @Override
  protected CompletionProposalCollector createCollector(
      JavaContentAssistInvocationContext context) {
    CompletionProposalCollector collector = super.createCollector(context);
    collector.setIgnored(CompletionProposal.TYPE_REF, false);
    return collector;
  }

  /*
   * @see org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer#computeCompletionProposals(org.eclipse.jface.text.contentassist.TextContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public List<ICompletionProposal> computeCompletionProposals(
      ContentAssistInvocationContext context, IProgressMonitor monitor) {
    List<ICompletionProposal> types = super.computeCompletionProposals(context, monitor);

    if (!(context instanceof JavaContentAssistInvocationContext)) return types;

    JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
    CompletionContext coreContext = javaContext.getCoreContext();
    if (coreContext != null
        && coreContext.getTokenLocation() != CompletionContext.TL_CONSTRUCTOR_START) return types;

    try {
      if (types.size() > 0 && context.computeIdentifierPrefix().length() == 0) {
        IType expectedType = javaContext.getExpectedType();
        if (expectedType != null) {
          // empty prefix completion - insert LRU types if known, but prune if they already occur in
          // the core list

          // compute minmimum relevance and already proposed list
          int relevance = Integer.MAX_VALUE;
          Set<String> proposed = new HashSet<String>();
          for (Iterator<ICompletionProposal> it = types.iterator(); it.hasNext(); ) {
            AbstractJavaCompletionProposal p = (AbstractJavaCompletionProposal) it.next();
            IJavaElement element = p.getJavaElement();
            if (element instanceof IType) proposed.add(((IType) element).getFullyQualifiedName());
            relevance = Math.min(relevance, p.getRelevance());
          }

          // insert history types
          List<String> history =
              JavaPlugin.getDefault()
                  .getContentAssistHistory()
                  .getHistory(expectedType.getFullyQualifiedName())
                  .getTypes();
          relevance -= history.size() + 1;
          for (Iterator<String> it = history.iterator(); it.hasNext(); ) {
            String type = it.next();
            if (proposed.contains(type)) continue;

            IJavaCompletionProposal proposal = createTypeProposal(relevance, type, javaContext);

            if (proposal != null) types.add(proposal);
            relevance++;
          }
        }
      }
    } catch (BadLocationException x) {
      // log & ignore
      JavaPlugin.log(x);
    } catch (JavaModelException x) {
      // log & ignore
      JavaPlugin.log(x);
    }

    return types;
  }

  private IJavaCompletionProposal createTypeProposal(
      int relevance, String fullyQualifiedType, JavaContentAssistInvocationContext context)
      throws JavaModelException {
    IType type = context.getCompilationUnit().getJavaProject().findType(fullyQualifiedType);
    if (type == null) return null;

    CompletionProposal proposal =
        CompletionProposal.create(CompletionProposal.TYPE_REF, context.getInvocationOffset());
    proposal.setCompletion(fullyQualifiedType.toCharArray());
    proposal.setDeclarationSignature(type.getPackageFragment().getElementName().toCharArray());
    proposal.setFlags(type.getFlags());
    proposal.setRelevance(relevance);
    proposal.setReplaceRange(context.getInvocationOffset(), context.getInvocationOffset());
    proposal.setSignature(Signature.createTypeSignature(fullyQualifiedType, true).toCharArray());

    if (shouldProposeGenerics(context.getProject()))
      return new LazyGenericTypeProposal(proposal, context);
    else return new LazyJavaTypeCompletionProposal(proposal, context);
  }

  /**
   * Returns <code>true</code> if generic proposals should be allowed, <code>false</code> if not.
   * Note that even though code (in a library) may be referenced that uses generics, it is still
   * possible that the current source does not allow generics.
   *
   * @param project the Java project
   * @return <code>true</code> if the generic proposals should be allowed, <code>false</code> if not
   */
  protected final boolean shouldProposeGenerics(IJavaProject project) {
    String sourceVersion;
    if (project != null) sourceVersion = project.getOption(JavaCore.COMPILER_SOURCE, true);
    else sourceVersion = JavaCore.getOption(JavaCore.COMPILER_SOURCE);

    return sourceVersion != null && JavaCore.VERSION_1_5.compareTo(sourceVersion) <= 0;
  }

  @Override
  protected int guessContextInformationPosition(ContentAssistInvocationContext context) {
    final int contextPosition = context.getInvocationOffset();

    IDocument document = context.getDocument();
    JavaHeuristicScanner scanner = new JavaHeuristicScanner(document);
    int bound = Math.max(-1, contextPosition - 200);

    // try the innermost scope of angle brackets that looks like a generic type argument list
    try {
      int pos = contextPosition - 1;
      do {
        int angle = scanner.findOpeningPeer(pos, bound, '<', '>');
        if (angle == JavaHeuristicScanner.NOT_FOUND) break;
        int token = scanner.previousToken(angle - 1, bound);
        // next token must be a method name that is a generic type
        if (token == Symbols.TokenIDENT) {
          int off = scanner.getPosition() + 1;
          int end = angle;
          String ident = document.get(off, end - off).trim();
          if (JavaHeuristicScanner.isGenericStarter(ident)) return angle + 1;
        }
        pos = angle - 1;
      } while (true);
    } catch (BadLocationException x) {
    }

    return super.guessContextInformationPosition(context);
  }
}
