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

import java.util.Objects;

public class App {

    public static void main(String... args) {
        Book book1 = new BookImpl("java", "oracle");    // to invoke constructor BookImpl(title, author)
        Book book2 = BookImpl.create("go", "google");   // to invoke static method BookImpl.create(title, author)

        if (!book2.isEquals(book1)) {                   // to invoke default method Book.isEquals()
            Book.printInfo(book1);                      // to invoke default static method Book.printInfo(book)
        }

        // external lib ch.qos.logback with sources which are accessible by maven; need to download sources from maven repo.
        org.slf4j.Logger logbackLogger = org.slf4j.LoggerFactory.getLogger(App.class);
        logbackLogger.info(concat("Info from ", "logbackLogger", ' '));
    }

    /**
     * Returns concatination of two strings into one divided by special symbol.
     *
     * @deprecated As of version 1.0, use {@link org.apache.commons.lang.StringUtils#join(Object[], char)}
     *
     * @param part1
     *          part 1 to concat.
     * @param part2
     *          part 2 to concat.
     * @param divider
     *          divider of part1 and part2.
     * @return concatination of two strings into one.
     *
     * @throws NullPointerException
     *       if one of the part has null value.
     */
    public static String concat(String part1, String part2, char divider) throws NullPointerException {
        Objects.nonNull(part1);
        Objects.nonNull(part2);

        return part1 + divider + part2;
    }
}
