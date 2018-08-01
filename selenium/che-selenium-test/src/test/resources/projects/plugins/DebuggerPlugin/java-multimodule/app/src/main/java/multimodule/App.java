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
package multimodule;

import multimodule.model.Book;
import multimodule.model.BookImpl;

public class App {

    public static void main(String... args) {
        Book book1 = new BookImpl("java", "oracle");    // to invoke constructor BookImpl(title, author)
        Book book2 = BookImpl.create("go", "google");   // to invoke static method BookImpl.create(title, author)

        if (!book2.isEquals(book1)) {                   // to invoke default method Book.isEquals()
            Book.printInfo(book1);                      // to invoke default static method Book.printInfo(book)
        }
    }

}
