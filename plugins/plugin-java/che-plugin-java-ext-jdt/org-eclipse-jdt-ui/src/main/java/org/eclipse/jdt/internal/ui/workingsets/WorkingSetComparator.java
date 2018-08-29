/**
 * ***************************************************************************** Copyright (c) 2009,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.workingsets;

import java.text.Collator;
import java.util.Comparator;
import org.eclipse.ui.IWorkingSet;

/**
 * Comparator class to sort working sets, optionally keeping the default working set at the top.
 *
 * @since 3.5
 */
public class WorkingSetComparator implements Comparator<IWorkingSet> {

  private Collator fCollator = Collator.getInstance();

  /** Boolean value to determine whether to keep default working set on the top while sorting. */
  private boolean fIsOtherWorkingSetOnTop;

  /** Creates new instance of the working set comparator. */
  public WorkingSetComparator() {
    fIsOtherWorkingSetOnTop = false;
  }

  /**
   * Creates a new instance of working set comparator and initializes the boolean field value to the
   * given value, which determines whether or not the default working set is kept on top while
   * sorting the working sets.
   *
   * @param isOtherWorkingSetOnTop <code>true</code> if default working set is to be retained at the
   *     top, <code>false</code> otherwise
   */
  public WorkingSetComparator(boolean isOtherWorkingSetOnTop) {
    fIsOtherWorkingSetOnTop = isOtherWorkingSetOnTop;
  }

  /**
   * Returns <code>-1</code> if the first argument is the default working set, <code>1</code> if the
   * second argument is the default working set and if the boolean <code>fIsOtherWorkingSetOnTop
   * </code> is set, to keep the default working set on top while sorting.
   *
   * @see Comparator#compare(Object, Object)
   */
  public int compare(IWorkingSet w1, IWorkingSet w2) {

    if (fIsOtherWorkingSetOnTop && IWorkingSetIDs.OTHERS.equals(w1.getId())) return -1;

    if (fIsOtherWorkingSetOnTop && IWorkingSetIDs.OTHERS.equals(w2.getId())) return 1;

    return fCollator.compare(w1.getLabel(), w2.getLabel());
  }
}
