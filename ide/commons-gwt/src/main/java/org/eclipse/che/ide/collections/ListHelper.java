/*
 * Copyright (c) 2012-2015 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.che.commons.annotation.Nullable;

public class ListHelper {

  private ListHelper() {}

  /**
   * Concatenates the List into a string using the supplied separator to delimit.
   *
   * @param list elements
   * @param separator separator for delimiting
   * @return The list converted to a String
   */
  public static <T> String join(List<T> list, String separator) {
    StringBuilder b = new StringBuilder();
    Iterator<T> iterator = list.iterator();
    if (iterator.hasNext()) {
      b.append(iterator.next().toString());
      while (iterator.hasNext()) {
        T t = iterator.next();
        b.append(separator);
        b.append(t.toString());
      }
    }
    return b.toString();
  }

  /**
   * Removes n elements found at the specified index.
   *
   * @param list elements
   * @param index index at which we are removing
   * @param deleteCount the number of elements we will remove starting at the index
   * @return an array of elements that were removed
   */
  public static <T> List<T> splice(List<T> list, int index, int deleteCount) {
    return spliceImpl(list, index, deleteCount, false, null);
  }

  /**
   * Removes n elements found at the specified index. And then inserts the specified item at the
   * index
   *
   * @param list elements
   * @param index index at which we are inserting/removing
   * @param deleteCount the number of elements we will remove starting at the index
   * @param value the item we want to add to the array at the index
   * @return an array of elements that were removed
   */
  public static <T> List<T> splice(List<T> list, int index, int deleteCount, T value) {
    return spliceImpl(list, index, deleteCount, true, value);
  }

  private static <T> List<T> spliceImpl(
      List<T> list, int index, int deleteCount, boolean hasValue, T value) {
    List<T> removedArray = new ArrayList<>();
    for (int i = deleteCount; i > 0; i--) {
      T removedElem = list.remove(index);
      removedArray.add(removedElem);
    }

    if (hasValue) {
      list.add(index, value);
    }

    return removedArray;
  }

  public static <T> List<T> slice(List<T> list, int start, int end) {
    List<T> sliced = new ArrayList<>();
    for (int i = start; i < end && i < list.size(); i++) {
      sliced.add(list.get(i));
    }

    return sliced;
  }

  /**
   * Check if two lists are equal. The lists are equal if they are both the same size, and the items
   * at every index are equal according to the provided equator. Returns true if both lists are
   * null.
   *
   * @param <T> the data type of the arrays
   */
  public static <T> boolean equals(List<T> a, List<T> b) {
    if (a == b) {
      // Same list or both null.
      return true;
    } else if (a == null || b == null) {
      // One list is null, the other is not.
      return false;
    } else if (a.size() != b.size()) {
      // Different sizes.
      return false;
    } else {
      // Check the elements in the array.
      for (int i = 0; i < a.size(); i++) {
        T itemA = a.get(i);
        T itemB = b.get(i);
        // if the equator is null we just the equals method and some null checking
        if (!equal(itemA, itemB)) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Determines whether two possibly-null objects are equal. Returns:
   *
   * <p>
   *
   * <ul>
   *   <li>{@code true} if {@code a} and {@code b} are both null.
   *   <li>{@code true} if {@code a} and {@code b} are both non-null and they are equal according to
   *       {@link Object#equals(Object)}.
   *   <li>{@code false} in all other situations.
   * </ul>
   *
   * <p>
   *
   * <p>This assumes that any non-null objects passed to this function conform to the {@code
   * equals()} contract.
   */
  public static boolean equal(@Nullable Object a, @Nullable Object b) {
    return a == b || (a != null && a.equals(b));
  }
}
