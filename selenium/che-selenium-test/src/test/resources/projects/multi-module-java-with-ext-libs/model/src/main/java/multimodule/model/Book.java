/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package multimodule.model;

import java.io.Serializable;
import java.util.Objects;

public interface Book extends Serializable {

    /**
     * The value of this constant is {@value}.
     */
    String PREFIX = "Book:";

    /**
     * Gets title of book.
     */
    String getTitle();

    /**
     * gets author of book.
     */
    String getAuthor();

    /**
     * Returns {@code true} if the argument is equal to instance.
     * otherwise {@code false}
     *
     * @param o
     *      an object.
     *
     * @return Returns {@code true} if the argument is equal to instance.
     * otherwise {@code false}
     *
     * @see java.lang.Object#equals(Object)
     * @since 1.0
     */
    default boolean isEquals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BookImpl)) return false;

        BookImpl book = (BookImpl) o;
        return Objects.equals(getTitle(), book.getTitle())
               && Objects.equals(getAuthor(), book.getAuthor());
    }

    /**
     * Print info about the {@literal <book>}.
     * @deprecated  As of version 1.0, replaced by {@link Book#printInfo(Book)}
     * @param book
     *     instance of book to print
     * @exception RuntimeException if it is impossible to print book.
     *
     * See the <a href="{@docRoot}/copyright.html">Copyright</a>.
     */
    static void print(Book book) {
        System.out.println(book.toString());
    }

    /**
     * Print title of book.
     * @param book
     *     instance of book to print
     */
    static void printInfo(Book book) {
        System.out.println(String.format("%s '%s'.",
                                         PREFIX,
                                         book.getTitle()));
    }

}
