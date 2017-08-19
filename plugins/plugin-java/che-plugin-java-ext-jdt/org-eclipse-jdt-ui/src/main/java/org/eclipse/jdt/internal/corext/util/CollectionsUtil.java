/**
 * ***************************************************************************** Copyright (c) 2011
 * IBM Corporation and others. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.util;

import java.lang.reflect.Array;
import java.util.Collection;

public class CollectionsUtil {
  /**
   * Returns an array containing all of the elements in the given collection. This is a compile-time
   * type-safe alternative to {@link java.util.Collection#toArray(Object[])}.
   *
   * @param collection the source collection
   * @param clazz the type of the array elements
   * @param <A> the type of the array elements
   * @return an array of type <code>A</code> containing all of the elements in the given collection
   * @throws NullPointerException if the specified collection or class is null
   * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7023484">Sun bug 7023484</a>
   */
  public static <A> A[] toArray(Collection<? extends A> collection, Class<A> clazz) {
    Object array = Array.newInstance(clazz, collection.size());
    @SuppressWarnings("unchecked")
    A[] typedArray = collection.toArray((A[]) array);
    return typedArray;
  }
}
