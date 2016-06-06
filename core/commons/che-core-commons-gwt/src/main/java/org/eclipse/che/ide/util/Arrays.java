/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.util;

import com.google.common.annotations.Beta;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;

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
     * @param array
     *         the array to check
     * @param <T>
     *         any type of the given array
     * @return {@code true} if given array is null or empty, otherwise {@code false}
     * @since 4.3.0
     */
    public static <T> boolean isNullOrEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Adds the given {@code element} to the tail of given {@code array}.
     *
     * @param array
     *         the array to which {@code element} should be inserted
     * @param element
     *         {@code element} to insert
     * @param <T>
     *         type of given {@code array}
     * @return the copy of given {@code array} with added {@code element}
     * @throws IllegalArgumentException
     *         in case if given {@code arrays} is null
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
     * @param array
     *         arrays to check
     * @param element
     *         element to check for existence
     * @param <T>
     *         type of given {@code array}
     * @return {@code true} if input {@code element} exists in array, otherwise {@code false}
     * @throws IllegalArgumentException
     *         in case if given {@code array} is null
     * @since 4.3.0
     */
    public static <T> boolean contains(T[] array, T element) {
        checkArgument(array != null, "Input array is null");

        return indexOf(array, element) != -1;
    }

    /**
     * Returns the index of the first occurrence of the specified element {@code o}
     * in given {@code array}, or -1 if given {@code array} does not contain the element {@code o}.
     *
     * @param array
     *         input array
     * @param o
     *         element to search for
     * @param <T>
     *         type of given {@code array}
     * @return the index of the first occurrence of the specified element in
     * this list, or -1 if this list does not contain the element
     * @throws IllegalArgumentException
     *         in case if given {@code array} is null
     * @since 4.3.0
     */
    public static <T> int indexOf(T[] array, T o) {
        checkArgument(array != null, "Input array is null");

        if (o == null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null)
                    return i;
            }
        } else {
            for (int i = 0; i < array.length; i++) {
                if (o.equals(array[i]))
                    return i;
            }
        }
        return -1;
    }

    /**
     * Retains only the elements in given {@code o1} array that are contained in the
     * specified {@code o2}. In other words, removes from {@code o1} all
     * of its elements that are not contained in the specified array {@code o2}.
     *
     * @param o1
     *         input array
     * @param o2
     *         array containing elements to be retained in {@code o1}
     * @param complement
     *         true if operation should be performed with retain algorithm, false means that from {@code o1} should be removed all elements
     *         that contains in {@code o2}
     * @param <T>
     *         type of given {@code o1} and {@code o2}
     * @return copy of retained array
     * @throws IllegalArgumentException
     *         in case if given arrays null
     * @since 4.3.0
     */
    public static <T> T[] batchRemove(T[] o1, T[] o2, boolean complement) {
        checkArgument(o1 != null && o2 != null);

        int r = 0, w = 0;

        T[] o1Copy = copyOf(o1, o1.length);
        T[] o2Copy = copyOf(o2, o2.length);

        for (; r < o1Copy.length; r++)
            if ((indexOf(o2Copy, o1Copy[r]) >= 0) == complement)
                o1Copy[w++] = o1Copy[r];

        if (r != o1Copy.length) {
            arraycopy(o1Copy, r,
                      o1Copy, w,
                      o1Copy.length - r);
            w += o1Copy.length - r;
        }
        if (w != o1Copy.length) {
            for (int i = w; i < o1Copy.length; i++)
                o1Copy[i] = null;

            return copyOf(o1Copy, w);
        } else {
            return copyOf(o1Copy, o1Copy.length);
        }
    }

    /**
     * Removes the first occurrence of the specified {@code element} from the given {@code array},
     * if it is present and returns the copy of modified array.
     *
     * @param array
     *         the array from which {@code element} should be removed
     * @param element
     *         {@code element} to remove
     * @param <T>
     *         type of given {@code array}
     * @return the copy of given {@code array} without removed {@code element}
     * @throws IllegalArgumentException
     *         in case if given {@code arrays} is null
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
