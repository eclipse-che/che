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
package multimodule.model;

/**
 * Implementation of Book interface.
 *
 * @author      Major geek
 * @author      Famous geek
 * @version     %I%, %G%
 * @since       0.5
 * @serial      include
 */
public class BookImpl implements Book {
    private String title;
    private String author;

    public BookImpl(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public boolean equals(Object o) {
        return isEquals(o);
    }

    /**
     * Returns 1 always.
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 1;
    }

    /**
     * @param title
     *     title of book
     * @param author
     *     author of book
     * @return instance of BookImpl
     *
     * @see #BookImpl(String, String)  constructor
     */
    public static Book create(String title, String author) {
        return new BookImpl(title, author);
    }
}
