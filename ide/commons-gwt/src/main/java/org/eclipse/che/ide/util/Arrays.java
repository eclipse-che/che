/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.copyOf;

import com.google.common.annotations.Beta;
import com.google.common.base.Objects;
import com.google.common.collect.ObjectArrays;

/**
 * Utility methods to operate with arrays.
 *
 * @author Vlad Zhukovskyi
 * @since 4.3.0
 */
@Beta
public class Arrays {

  /**
   * Checks where given {@code array} is {@code null} or empty.
   *
   * @param array the array to check
   * @param <T> any type of the given array
   * @return {@code true} if given array is null or empty, otherwise {@code false}
   * @since 4.3.0
   */
  public static <T> boolean isNullOrEmpty(T[] array) {
    return array == null || array.length == 0;
  }

  /**
   * Adds the given {@code element} to the tail of given {@code array}.
   *
   * @param array the array to which {@code element} should be inserted
   * @param element {@code element} to insert
   * @param <T> type of given {@code array}
   * @return the copy of given {@code array} with added {@code element}
   * @throws IllegalArgumentException in case if given {@code arrays} is null
   * @since 4.3.0
   */
  public static <T> T[] add(T[] array, T element) {
    checkArgument(array != null, "Input array is null");

    final int index = array.length;

    array = java.util.Arrays.copyOf(array, index + 1);
    array[index] = element;

    return array;
  }

  /**
   * Checks if given {@code element} exists in {@code array}.
   *
   * @param array arrays to check
   * @param element element to check for existence
   * @param <T> type of given {@code array}
   * @return {@code true} if input {@code element} exists in array, otherwise {@code false}
   * @throws IllegalArgumentException in case if given {@code array} is null
   * @since 4.3.0
   */
  public static <T> boolean contains(T[] array, T element) {
    return indexOf(array, element) != -1;
  }

  /**
   * Returns the index of the first occurrence of the element {@code o} in given {@code array}, or
   * -1 if there is no such element exists.
   *
   * @param array input array
   * @param o element to search for
   * @param <T> type of given {@code array}
   * @return the index of the first occurrence of the specified element in this {@code array},
   *     otherwise -1
   * @throws IllegalArgumentException in case if given {@code array} is null
   * @since 4.3.0
   */
  public static <T> int indexOf(T[] array, T o) {
    checkArgument(array != null, "Input array is null");

    for (int index = 0; index < array.length; index++) {
      if (Objects.equal(o, array[index])) {
        return index;
      }
    }

    return -1;
  }

  /**
   * Remove from array {@code o1} all elements which exists in {@code o2} if {@code retain} flag is
   * set to {@code false}. If argument {@code retain} is set to {@code true}, then in result
   * collection will be common elements from both arrays.
   *
   * @param o1 input array
   * @param o2 array, elements of which should be removed from the {@code o2} array
   * @param retain true if operation should be performed with retain algorithm, false means that
   *     from {@code o1} should be removed all elements that contains in {@code o2}
   * @param <T> type of given {@code o1} and {@code o2}
   * @return new array, which contains elements based on operation type
   * @throws IllegalArgumentException in case if given arrays null
   * @since 4.3.0
   */
  public static <T> T[] removeAll(T[] o1, T[] o2, boolean retain) {
    checkArgument(o1 != null && o2 != null, "Input arrays are null");

    T[] retained = retain ? ObjectArrays.newArray(o1, 0) : o1;

    for (int index = 0; index < o1.length; index++) {
      if (indexOf(o2, o1[index]) != -1) {
        retained = retain ? add(retained, o1[index]) : remove(retained, o1[index]);
      }
    }

    return retained;
  }

  /**
   * Removes the first occurrence of the specified {@code element} from the given {@code array}, if
   * it is present and returns the copy of modified array.
   *
   * @param array the array from which {@code element} should be removed
   * @param element {@code element} to remove
   * @param <T> type of given {@code array}
   * @return the copy of given {@code array} without removed {@code element}
   * @throws IllegalArgumentException in case if given {@code arrays} is null
   * @since 4.3.0
   */
  public static <T> T[] remove(T[] array, T element) {
    checkArgument(array != null, "Input array is null");

    T[] modified = copyOf(array, array.length);

    int size = modified.length;
    int index = indexOf(modified, element);
    int numMoved = modified.length - index - 1;
    if (numMoved > 0) {
      System.arraycopy(modified, index + 1, modified, index, numMoved);
    }

    return copyOf(modified, --size);
  }
}
