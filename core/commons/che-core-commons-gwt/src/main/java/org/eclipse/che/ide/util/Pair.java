/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.che.ide.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * An immutable, semantic-free ordered pair of nullable values. These can be
 * accessed using the {@link #getFirst} and {@link #getSecond} methods. Equality
 * and hashing are defined in the natural way.
 * <p/>
 * <p>This type is devoid of semantics, best used for simple mechanical
 * aggregations of unrelated values in implementation code. Avoid using it in
 * your APIs, preferring an explicit type that conveys the exact semantics of
 * the data. For example, instead of: <pre>   {@code
 * <p/>
 *   Pair<T, T> findMinAndMax(List<T> list) {...}}</pre>
 * <p/>
 * ... use: <pre>   {@code
 * <p/>
 *   Range<T> findRange(List<T> list) {...}}</pre>
 * <p/>
 * This usually involves creating a new custom value-object type. This is
 * difficult to do "by hand" in Java, but avoid the temptation to extend {@code
 * Pair} to accomplish this; consider using the utilities {@link
 * com.google.common.labs.misc.ComparisonKeys} or {@link
 * com.google.common.labs.misc.ValueType} to help you with this instead.
 */
public class Pair<A, B> implements Serializable {

    /** Creates a new pair containing the given elements in order. */
    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<A, B>(first, second);
    }

    /** The first element of the pair; see also {@link #getFirst}. */
    public final A first;

    /** The second element of the pair; see also {@link #getSecond}. */
    public final B second;

    /** Constructor.  It is usually easier to call {@link #of}. */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /** Returns the first element of this pair; see also {@link #first}. */
    public A getFirst() {
        return first;
    }

    /** Returns the second element of this pair; see also {@link #second}. */
    public B getSecond() {
        return second;
    }

    /** Returns a function that yields {@link #first}. */
    @SuppressWarnings("unchecked")
    public static <A, B> Function<Pair<A, B>, A> firstFunction() {
        // The safety of the unchecked conversion here is implied by the
        // implementation of the PairFirstFunction which always returns the first
        // element from a pair (which for Pair<A, B> is of type A).
        return (Function)PairFirstFunction.INSTANCE;
    }

    /** Returns a function that yields {@link #second}. */
    @SuppressWarnings("unchecked")
    public static <A, B> Function<Pair<A, B>, B> secondFunction() {
        // The safety of the unchecked conversion here is implied by the
        // implementation of the PairSecondFunction which always returns the second
        // element from a pair (which for Pair<A, B> is of type B).
        return (Function)PairSecondFunction.INSTANCE;
    }

   /*
    * If we use the enum singleton pattern for these functions, Flume's type
    * inference chokes: http://b/4863010
    */

    private static final class PairFirstFunction<A, B> implements Function<Pair<A, B>, A>, Serializable {
        static final PairFirstFunction<Object, Object> INSTANCE = new PairFirstFunction<Object, Object>();

        @Override
        public A apply(Pair<A, B> from) {
            return from.getFirst();
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }

    private static final class PairSecondFunction<A, B> implements Function<Pair<A, B>, B>, Serializable {
        static final PairSecondFunction<Object, Object> INSTANCE = new PairSecondFunction<Object, Object>();

        @Override
        public B apply(Pair<A, B> from) {
            return from.getSecond();
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }

    /**
     * Returns a comparator that compares two Pair objects by comparing the
     * result of {@link #getFirst()} for each.
     */
    @SuppressWarnings("unchecked")
    public static <A extends Comparable, B> Comparator<Pair<A, B>> compareByFirst() {
        return (Comparator)FirstComparator.FIRST_COMPARATOR;
    }

    /**
     * Returns a comparator that compares two Pair objects by comparing the
     * result of {@link #getSecond()} for each.
     */
    @SuppressWarnings("unchecked")
    public static <A, B extends Comparable> Comparator<Pair<A, B>> compareBySecond() {
        return (Comparator)SecondComparator.SECOND_COMPARATOR;
    }

    // uses raw Comparable to support classes defined without generics
    @SuppressWarnings("unchecked")
    private enum FirstComparator implements Comparator<Pair<Comparable, Object>> {
        FIRST_COMPARATOR;

        @Override
        public int compare(Pair<Comparable, Object> pair1, Pair<Comparable, Object> pair2) {
            return pair1.getFirst().compareTo(pair2.getFirst());
        }
    }

    // uses raw Comparable to support classes defined without generics
    @SuppressWarnings("unchecked")
    private enum SecondComparator implements Comparator<Pair<Object, Comparable>> {
        SECOND_COMPARATOR;

        @Override
        public int compare(Pair<Object, Comparable> pair1, Pair<Object, Comparable> pair2) {
            return pair1.getSecond().compareTo(pair2.getSecond());
        }
    }

    // TODO: decide what level of commitment to make to this impl
    @Override
    public boolean equals(Object object) {
        // TODO: it is possible we want to change this to
        // if (object != null && object.getClass() == getClass()) {
        if (object instanceof Pair) {
            Pair<?, ?> that = (Pair<?, ?>)object;
            return equal(this.first, that.first) && equal(this.second, that.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash1 = first == null ? 0 : first.hashCode();
        int hash2 = second == null ? 0 : second.hashCode();
        return 31 * hash1 + hash2;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>This implementation returns a string in the form
     * {@code (first, second)}, where {@code first} and {@code second} are the
     * String representations of the first and second elements of this pair, as
     * given by {@link String#valueOf(Object)}. Subclasses are free to override
     * this behavior.
     */
    @Override
    public String toString() {
        // GWT doesn't support String.format().
        return "(" + first + ", " + second + ")";
    }

    /**
     * Determines whether two possibly-null objects are equal. Returns:
     * <p/>
     * <ul>
     * <li>{@code true} if {@code a} and {@code b} are both null.
     * <li>{@code true} if {@code a} and {@code b} are both non-null and they are
     * equal according to {@link Object#equals(Object)}.
     * <li>{@code false} in all other situations.
     * </ul>
     * <p/>
     * <p>This assumes that any non-null objects passed to this function conform
     * to the {@code equals()} contract.
     */
    private boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    private static final long serialVersionUID = 747826592375603043L;
}
