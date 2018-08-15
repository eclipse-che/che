/*
 * Copyright (c) 2012-2014 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.test;

/**
 * Test javadoc for class.
 */
public class MyClass {

    /**
     * My test method javadoc;
     */
    public void myMethod() {
        return "";
    }

    /**
     * My test field javadoc.
     */
    public String myField;

    /**
     * Verifies that the specified name is valid for our service.
     * <p/>
     * In this example, we only require that the name is at least four
     * characters. In your application, you can use more complex checks to ensure
     * that usernames, passwords, email addresses, URLs, and other fields have the
     * proper syntax.
     *
     * @param name
     *         the name to validate
     * @return true if valid, false if invalid
     */
    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        return name.length() > 3;
    }

    /**
     * Method with param and exception
     * @param input
     * @return
     * @throws IllegalArgumentException
     */
    public String greetServer(String input) throws IllegalArgumentException {
        return "";
    }
}
