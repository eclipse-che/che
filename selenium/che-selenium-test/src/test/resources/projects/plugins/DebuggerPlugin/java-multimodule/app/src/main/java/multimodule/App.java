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
