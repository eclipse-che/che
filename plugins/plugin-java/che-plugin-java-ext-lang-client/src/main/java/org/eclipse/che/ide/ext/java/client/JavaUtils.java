/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client;

import com.google.gwt.regexp.shared.RegExp;

import org.eclipse.che.ide.runtime.IStatus;

import java.util.Arrays;
import java.util.List;

/**
 * A collection of methods for Java-specific things.
 *
 * @author Artem Zatsarynnyi
 * @@author Dmitry Shnurenko
 */
public class JavaUtils {

    /**
     * The package name will be only in lower case, the first character will be always a lower letter,
     * the rest can mix underscore, lower letters and numbers.
     */
    private static final RegExp PACKAGE_PATTERN = RegExp.compile("^([a-zA-Z_]{1}[a-zA-Z0-9_]*(\\.[a-zA-Z_]{1}[a-zA-Z0-9_]*)*)?$");

    /**
     * The Class Name will always start with an Upper Case Letter or an underscore, the rest can mix underscore,
     * letters and numbers. Inner Classes will always start with a dollar symbol ($) and must obey the class
     * name rules described previously.
     */
    private static final RegExp COMPILATION_UNIT_PATTERN =
            RegExp.compile("^(([a-zA-Z][a-zA-Z_$0-9]*(\\.[a-zA-Z][a-zA-Z$0-9]*)*)\\.)?([a-zA-Z_][a-zA-Z_$0-9]*)$");

    private static final List<String> KEY_WORDS = Arrays.asList("abstract", "assert", "boolean", "break", "byte", "case", "catch",
                                                                "char", "class", "const", "continue", "default", "do", "double",
                                                                "else", "enum", "extends", "false", "final", "finally", "float",
                                                                "for", "goto", "if", "implements", "import", "instanceof", "int",
                                                                "interface", "long", "native", "new", "null", "package", "private",
                                                                "protected", "public", "return", "short", "static", "strictfp",
                                                                "super", "switch", "synchronized", "this", "throw", "throws",
                                                                "transient", "true", "try", "void", "volatile", "while");


    private JavaUtils() {
        throw new UnsupportedOperationException("Impossible create instance of " + getClass());
    }

    /**
     * Checks if the given name is valid compilation unit name.
     * Throws {@link IllegalStateException} if the specified name isn't a valid Java compilation unit name.
     * <p>
     * A compilation unit name must obey the following rules:
     * <p/>
     * <p/>
     * <ul>
     * <li> it must not be null
     * <li> it must be suffixed by a dot ('.') followed by one of the java like extension
     * <li> its prefix must be a valid identifier
     * </ul>
     * </p>
     *
     * @param name
     *         name to check
     * @throws IllegalStateException
     *         with a detail message that describes what is wrong with the specified name
     */
    public static void checkCompilationUnitName(String name) throws IllegalStateException {
        if (!isValidCompilationUnitName(name)) {
            throw new IllegalStateException("Value is not valid.");
        }
    }

    /**
     * Checks if the specified text is a valid compilation unit name.
     *
     * @param name
     *         the text to check
     * @return <code>true</code> if the specified text is a valid compilation unit name, <code>false</code> otherwise
     */
    public static boolean isValidCompilationUnitName(String name) {
        return isNameMatchedPattern(name, COMPILATION_UNIT_PATTERN);
    }

    private static boolean isNameMatchedPattern(String name, RegExp pattern) {
        if (name == null) {
            return false;
        }

        if (isContainKeyWord(name)) {
            return false;
        }
        int statusCode = pattern.test(name) ? IStatus.OK : IStatus.ERROR;

        return !name.isEmpty() && statusCode == 0;
    }

    private static boolean isContainKeyWord(String name) {
        for (String part : name.split("\\.")) {
            if (KEY_WORDS.contains(part)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the given package name is a valid package name.
     * Throws {@link IllegalStateException} if the specified name isn't a valid Java package name.
     * <p/>
     * The syntax of a package name corresponds to PackageName as
     * defined by PackageDeclaration (JLS2 7.4). For example, <code>"java.lang"</code>.
     * <p/>
     *
     * @param name
     *         name of the package
     * @throws IllegalStateException
     *         with a detail message that describes what is wrong with the specified name
     */
    public static void checkPackageName(String name) throws IllegalStateException {
        if (!isValidPackageName(name)) {
            throw new IllegalStateException("Value is not valid.");
        }
    }

    /**
     * Checks if the specified text is a valid package name.
     *
     * @param name
     *         the text to check
     * @return <code>true</code> if the specified text is a valid package name, <code>false</code> otherwise
     */
    public static boolean isValidPackageName(String name) {
        return isNameMatchedPattern(name, PACKAGE_PATTERN);
    }
}
