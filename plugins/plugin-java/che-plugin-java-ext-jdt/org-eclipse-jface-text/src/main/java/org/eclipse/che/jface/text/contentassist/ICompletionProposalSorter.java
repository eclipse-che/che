/**
 * Copyright (c) 2012 Darmstadt University of Technology and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Marcel Bruch, bruch@cs.tu-darmstadt.de - [content assist] Allow to re-sort
 * proposals - https://bugs.eclipse.org/bugs/show_bug.cgi?id=350991
 */
package org.eclipse.che.jface.text.contentassist;

/**
 * An <code>ICompletionProposalSorter</code> provides support for sorting proposals of a content
 * assistant.
 *
 * <p>Implementors of this interface have to register this sorter with the content assist whenever
 * needed. See {@link ContentAssistant#setSorter(ICompletionProposalSorter)} for more information on
 * how to register a proposal sorter.
 *
 * @since 3.8
 */
public interface ICompletionProposalSorter {

  /**
   * The orderings imposed by an implementation need not be consistent with equals.
   *
   * @param p1 the first proposal to be compared
   * @param p2 the second proposal to be compared
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
   *     equal to, or greater than the second
   * @see java.util.Comparator#compare(Object, Object)
   */
  public int compare(ICompletionProposal p1, ICompletionProposal p2);
}
