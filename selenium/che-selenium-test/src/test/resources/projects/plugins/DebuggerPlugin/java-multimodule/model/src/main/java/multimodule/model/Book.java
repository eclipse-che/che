/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package multimodule.model;

import java.util.Objects;

public interface Book {
    /**
     * Gets title of book.
     */
    String getTitle();

    /**
     * gets author of book.
     */
    String getAuthor();

    /**
     * Check is current object equals to argument o
     */
    default boolean isEquals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BookImpl)) return false;

        BookImpl book = (BookImpl) o;
        return Objects.equals(getTitle(), book.getTitle())
               && Objects.equals(getAuthor(), book.getAuthor());
    }

    /**
     * Print info about the book.
     * @param book
     */
    static void printInfo(Book book) {
        System.out.println(String.format("Book: '%s'.", book.getTitle()));
    }

}
