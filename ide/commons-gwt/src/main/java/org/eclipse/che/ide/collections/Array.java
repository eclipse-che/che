// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.collections;

import java.util.Comparator;
import java.util.List;

/**
 * Defines a simple interface for a list/array.
 * <p/>
 * When used with DTOs:
 * <p/>
 * On the client it is safe to cast this to a
 * {@link com.codenvy.ide.collections.js.JsoArray}.
 * <p/>
 * Native to JavaScript "sparse" arrays are not supported.
 * <p/>
 * On the server, this is an instance of
 * {@link com.codenvy.ide.collections.java.JsonArrayListAdapter} which
 * is a wrapper around a List.
 */
public interface Array<T> {

    void add(T item);

    void addAll(Array<? extends T> item);

    void clear();

    boolean contains(T item);

    Array<T> copy();

    T get(int index);

    int indexOf(T item);

    boolean isEmpty();

    String join(String separator);

    T peek();

    T pop();

    T remove(int index);

    Iterable<T> asIterable();

    boolean remove(T item);

    void reverse();

    /**
     * Assigns a new value to the slot with specified index.
     *
     * @throws IndexOutOfBoundsException
     *         if index is not in [0..length) range
     */
    void set(int index, T item);

    /** Sorts the array according to the comparator. Mutates the array. */
    void sort(Comparator<? super T> comparator);

    int size();

    Array<T> slice(int start, int end);

    Array<T> splice(int index, int deleteCount, T value);

    Array<T> splice(int index, int deleteCount);

    List<T> toList();
}
