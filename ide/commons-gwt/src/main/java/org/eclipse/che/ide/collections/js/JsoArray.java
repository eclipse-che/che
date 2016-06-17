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

package org.eclipse.che.ide.collections.js;

import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.ide.collections.ArrayIterator;
import com.google.gwt.core.client.JavaScriptObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Similar to {@link com.google.gwt.core.client.JsArray}, except
 * {@link com.google.gwt.core.client.JsArray} only allows you to store <T
 * extends JavaScriptObject>.
 * <p/>
 * This class is a native JS array that lets you store any Object.
 *
 * @param <T>
 *         the type of each entry in the array
 */
public class JsoArray<T> extends JavaScriptObject implements Array<T> {

    /** Concatenate 2 arrays. */
    public static final native <T> JsoArray<T> concat(JsoArray<?> a, JsoArray<?> b) /*-{
        return a.concat(b);
    }-*/;

    /**
     * Constructs a new one.
     *
     * @param <M>
     * @return the array
     */
    public static native <M> JsoArray<M> create() /*-{
        return [];
    }-*/;

    /**
     * Casts a Array to a JsoArray. Unsafe, but on client we know we always
     * have a JsoArray.
     */
    @SuppressWarnings("unchecked")
    public static <M, T extends M> JsoArray<M> from(Array<T> array) {
        return (JsoArray<M>)array;
    }

    public static <M, T extends M> JsoArray<M> from(List<T> list) {
        JsoArray<M> array = create();
        for (T item : list) {
            array.add(item);
        }

        return array;
    }

    /** Creates a JsoArray from a Java array. */
    public static <M> JsoArray<M> from(M... array) {
        JsoArray<M> result = create();
        for (M s : array) {
            result.add(s);
        }
        return result;
    }

    /**
     * Invokes the native string split on a string and returns a JavaScript array.
     * GWT's version of string.split() emulates Java behavior in JavaScript.
     */
    public static native JsoArray<String> splitString(String str, String regexp) /*-{
        return str.split(regexp);
    }-*/;

    protected JsoArray() {
    }

    /**
     * Adds a value to the end of an array.
     *
     * @param value
     */
    @Override
    public final native void add(T value) /*-{
        this.push(value);
    }-*/;

    /** Adds all of the elements in the given {@code array} to this array. */
    @Override
    public final void addAll(Array<? extends T> array) {
        for (int i = 0, n = array.size(); i < n; ++i) {
            add(array.get(i));
        }
    }

    @Override
    public final void clear() {
        setLength(0);
    }

    @Override
    public final boolean contains(T value) {
        for (int i = 0, n = size(); i < n; i++) {
            if (equalsOrNull(value, get(i))) {
                return true;
            }
        }
        return false;
    }

    /** Returns a new array with the same contents as this array. */
    @Override
    public final native JsoArray<T> copy() /*-{
        return this.slice(0);
    }-*/;

    /**
     * Standard index accessor.
     *
     * @param index
     * @return T stored at index
     */
    @Override
    public final native T get(int index) /*-{
        return this[index];
    }-*/;

    @Override
    public final native int indexOf(T item) /*-{
        return this.indexOf(item);
    }-*/;

    /** @return whether or not this collection is empty */
    @Override
    public final native boolean isEmpty() /*-{
        return (this.length == 0);
    }-*/;

    /**
     * Uses "" as the separator.
     *
     * @return returns a string using the empty string as the separator.
     */
    public final String join() {
        return join("");
    }

    /**
     * Concatenates the Array into a string using the supplied separator to
     * delimit.
     *
     * @param sep
     * @return The array converted to a String
     */
    @Override
    public final native String join(String sep) /*-{
        return this.join(sep);
    }-*/;

    /**
     * Returns the last element in the array.
     *
     * @return the last element
     */
    @Override
    public final native T peek() /*-{
        return this[this.length - 1];
    }-*/;

    /**
     * Pops the end of the array and returns is.
     *
     * @return a value T from the end of the array
     */
    @Override
    public final native T pop() /*-{
        return this.pop();
    }-*/;

    /**
     * @param index
     * @return The element that was removed.
     */
    @Override
    public final T remove(int index) {
        return splice(index, 1).get(0);
    }

    @Override
    public final boolean remove(T value) {
        for (int i = 0, n = size(); i < n; i++) {
            if ((value != null && value.equals(get(i))) || (value == null && get(i) == null)) {
                remove(i);
                return true;
            }
        }

        return false;
    }

    @Override
    public final native void reverse() /*-{
        this.reverse();
    }-*/;

    /**
     * Sets a given value at a specified index.
     *
     * @param index
     * @param value
     */
    @Override
    public final void set(int index, T value) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
        }
        nativeSet(index, value);
    }

    private native void nativeSet(int index, T value) /*-{
        this[index] = value;
    }-*/;

    /**
     * Reset the length of the array.
     *
     * @param newLength
     *         the new length of the array
     */
    public final native void setLength(int newLength) /*-{
        this.length = newLength;
    }-*/;

    public final native T shift() /*-{
        return this.shift();
    }-*/;

    /** Prepends an item onto the array */
    public final native int unshift(T value) /*-{
        return this.unshift(value);
    }-*/;

    /** @return the length of the array */
    @Override
    public final native int size() /*-{
        return this.length;
    }-*/;

    /**
     * @param start
     * @param end
     * @return A sub-array starting at start (inclusive) and endint at the end
     *         index (exclusive).
     */
    @Override
    public final native JsoArray<T> slice(int start, int end) /*-{
        return this.slice(start, end);
    }-*/;

    /**
     * Sorts the array using the default browser sorting method. Mutates the
     * array.
     */
    public final native JsoArray<T> sort() /*-{
        return this.sort();
    }-*/;

    @Override
    public final native void sort(Comparator<? super T> comparator) /*-{
        this.sort(function (a, b) {
            return comparator.@java.util.Comparator::compare(Ljava/lang/Object;Ljava/lang/Object;)(a, b);
        });
    }-*/;

    /**
     * Removes n elements found at the specified index.
     *
     * @param index
     *         index at which we are removing
     * @param n
     *         the number of elements we will remove starting at the index
     * @return an array of elements that were removed
     */
    @Override
    public final native JsoArray<T> splice(int index, int n) /*-{
        return this.splice(index, n);
    }-*/;

    public final List<T> toList() {
        List<T> list = new ArrayList<>();
        for (T t: asIterable()) {
            list.add(t);
        }
        return list;
    }

    /**
     * Removes n elements found at the specified index. And then inserts the
     * specified item at the index
     *
     * @param index
     *         index at which we are inserting/removing
     * @param n
     *         the number of elements we will remove starting at the index
     * @param item
     *         the item we want to add to the array at the index
     * @return an array of elements that were removed
     */
    @Override
    public final native JsoArray<T> splice(int index, int n, T item) /*-{
        return this.splice(index, n, item);
    }-*/;

    private boolean equalsOrNull(T a, T b) {
        return a == b || (a != null && a.equals(b));
    }

    @Override
    public final Iterable<T> asIterable() {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new ArrayIterator<T>(JsoArray.this);
            }
        };
    }
}
