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

    @Override
    public int hashCode() {
        return 1;
    }

    public static Book create(String title, String author) {
        return new BookImpl(title, author);
    }
}
