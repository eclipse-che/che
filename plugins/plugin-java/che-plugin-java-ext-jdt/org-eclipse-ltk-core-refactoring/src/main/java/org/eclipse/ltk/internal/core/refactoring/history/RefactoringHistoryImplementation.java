/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.internal.core.refactoring.history;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

/**
 * Default implementation of a refactoring history.
 *
 * @since 3.2
 */
public final class RefactoringHistoryImplementation extends RefactoringHistory {

  /**
   * Returns a hash code value for the array
   *
   * @param array the array to create a hash code value for
   * @return a hash code value for the array
   */
  private static int hashCode(final Object[] array) {
    if (array == null) return 0;
    int result = 1;
    for (int index = 0; index < array.length; index++)
      result = 31 * result + (array[index] == null ? 0 : array[index].hashCode());
    return result;
  }

  /** The refactoring descriptor proxies */
  private final RefactoringDescriptorProxy[] fDescriptorProxies;

  /** Is the refactoring history already sorted? */
  private boolean fSorted = false;

  /**
   * Creates a new refactoring history implementation.
   *
   * @param proxies the refactoring descriptor proxies
   */
  public RefactoringHistoryImplementation(final RefactoringDescriptorProxy[] proxies) {
    Assert.isNotNull(proxies);
    fDescriptorProxies = new RefactoringDescriptorProxy[proxies.length];
    System.arraycopy(proxies, 0, fDescriptorProxies, 0, proxies.length);
  }

  /** {@inheritDoc} */
  public boolean equals(final Object object) {
    if (this == object) return true;
    if (object == null) return false;
    if (getClass() != object.getClass()) return false;
    final RefactoringHistoryImplementation other = (RefactoringHistoryImplementation) object;
    if (!Arrays.equals(getDescriptors(), other.getDescriptors())) return false;
    return true;
  }

  /**
   * Returns the descriptor proxies, in no particular order.
   *
   * @return the descriptor proxies
   */
  RefactoringDescriptorProxy[] getDescriptorProxies() {
    return fDescriptorProxies;
  }

  /** {@inheritDoc} */
  public RefactoringDescriptorProxy[] getDescriptors() {
    if (!fSorted && fDescriptorProxies.length > 1)
      RefactoringHistoryManager.sortRefactoringDescriptorsDescending(fDescriptorProxies);
    fSorted = true;
    final RefactoringDescriptorProxy[] proxies =
        new RefactoringDescriptorProxy[fDescriptorProxies.length];
    System.arraycopy(fDescriptorProxies, 0, proxies, 0, fDescriptorProxies.length);
    return proxies;
  }

  /** {@inheritDoc} */
  public int hashCode() {
    return 31 * RefactoringHistoryImplementation.hashCode(getDescriptors());
  }

  /** {@inheritDoc} */
  public boolean isEmpty() {
    return fDescriptorProxies.length == 0;
  }

  /** {@inheritDoc} */
  public RefactoringHistory removeAll(final RefactoringHistory history) {
    final Set existing = new LinkedHashSet(Arrays.asList(fDescriptorProxies));
    final Set other = new HashSet(Arrays.asList(history.getDescriptors()));
    existing.removeAll(other);
    final RefactoringDescriptorProxy[] proxies = new RefactoringDescriptorProxy[existing.size()];
    existing.toArray(proxies);
    return new RefactoringHistoryImplementation(proxies);
  }

  /** {@inheritDoc} */
  @SuppressWarnings("ArrayToString")
  public String toString() {
    final StringBuffer buffer = new StringBuffer(256);
    buffer.append(getClass().getName());
    buffer.append("[descriptors="); // $NON-NLS-1$
    buffer.append(getDescriptors().toString());
    buffer.append(']');
    return buffer.toString();
  }
}
