/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ui.smartTree;

import java.util.Comparator;

/**
 * Sort direction enumeration.
 *
 * @author Vlad Zhukovskyi
 */
public enum SortDir {
    /**
     * Ascending sort order.
     */
    ASC {
        @Override
        public <X> Comparator<X> comparator(final Comparator<X> c) {
            return new Comparator<X>() {
                public int compare(X o1, X o2) {
                    return c.compare(o1, o2);
                }
            };
        }
    },

    /**
     * Descending sort order.
     */
    DESC {
        @Override
        public <X> Comparator<X> comparator(final Comparator<X> c) {
            return new Comparator<X>() {
                public int compare(X o1, X o2) {
                    return c.compare(o2, o1);
                }
            };
        }
    };

    /**
     * Toggles the given sort direction, that is given one sort direction, it
     * returns the other.
     *
     * @param sortDir the sort direction to toggle
     * @return the toggled sort direction
     */
    public static SortDir toggle(SortDir sortDir) {
        return (sortDir == ASC) ? DESC : ASC;
    }

    /**
     * An example of how to use this :
     *
     * List<Something> list = ...
     *
     * Collections.sort(list, SortDir.ASC.comparator(new Comparator() { public int
     * compare(Object o1, Object o2) { return ... } });
     *
     *
     * @return a Comparator that wraps the specific comparator that orders the
     *         results according to the sort direction
     */
    public abstract <X> Comparator<X> comparator(Comparator<X> c);
}