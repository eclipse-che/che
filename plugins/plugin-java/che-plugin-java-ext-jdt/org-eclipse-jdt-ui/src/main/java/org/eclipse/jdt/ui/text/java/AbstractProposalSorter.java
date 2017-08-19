/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Marcel Bruch
 * <bruch@cs.tu-darmstadt.de> - [content assist] Allow to re-sort proposals -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=350991
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.text.java;

import java.util.Comparator;
import org.eclipse.che.jface.text.contentassist.ICompletionProposal;
import org.eclipse.che.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Abstract base class for sorters contributed to the <code>
 * org.eclipse.jdt.ui.javaCompletionProposalSorters</code> extension point.
 *
 * <p>Subclasses need to implement {@link #compare(ICompletionProposal, ICompletionProposal)} and
 * may override {@link #beginSorting(ContentAssistInvocationContext) beginSorting} and {@link
 * #endSorting() endSorting}.
 *
 * <p>The orderings imposed by a subclass need not be consistent with equals.
 *
 * @since 3.2
 */
public abstract class AbstractProposalSorter
    implements Comparator<ICompletionProposal>, ICompletionProposalSorter {

  /**
   * Creates a new sorter. Note that subclasses must provide a zero-argument constructor to be
   * instantiatable via {@link IConfigurationElement#createExecutableExtension(String)}.
   */
  protected AbstractProposalSorter() {}

  /**
   * Called once before initial sorting starts the first time.
   *
   * <p><strong>Note:</strong> As of 3.8 a completion proposal computer can request that proposals
   * are resorted. If such a computer is active, then this method will not be called.
   *
   * <p>Clients may override, the default implementation does nothing.
   *
   * @param context the context of the content assist invocation
   */
  public void beginSorting(ContentAssistInvocationContext context) {}

  /**
   * The orderings imposed by an implementation need not be consistent with equals.
   *
   * @param p1 the first proposal to be compared
   * @param p2 the second proposal to be compared
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
   *     equal to, or greater than the second
   * @see java.util.Comparator#compare(Object, Object)
   */
  public abstract int compare(ICompletionProposal p1, ICompletionProposal p2);

  /**
   * Called once after the initial sorting finished.
   *
   * <p><strong>Note:</strong> As of 3.8 a completion proposal computer can request that proposals
   * are resorted. If such a computer is active, then this method will not be called.
   *
   * <p>Clients may override, the default implementation does nothing.
   */
  public void endSorting() {}
}
