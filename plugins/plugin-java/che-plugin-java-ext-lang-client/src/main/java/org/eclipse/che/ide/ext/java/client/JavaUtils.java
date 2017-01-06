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
package org.eclipse.che.ide.ext.java.client;

import org.eclipse.che.ide.runtime.IStatus;
import org.eclipse.che.ide.runtime.Status;

/**
 * A collection of methods for Java-specific things.
 *
 * @author Artem Zatsarynnyi
 */
public class JavaUtils {
    private JavaUtils() {
    }

    /**
     * Checks if the given name is valid compilation unit name.
     * Throws {@link IllegalStateException} if the specified name isn't a valid Java compilation unit name.
     * <p>
     * A compilation unit name must obey the following rules:
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
        IStatus status = validateCompilationUnitName(name);
        if (status.getSeverity() == IStatus.ERROR) {
            throw new IllegalStateException(status.getMessage());
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
        IStatus status = validateCompilationUnitName(name);
        switch (status.getSeverity()) {
            case Status.WARNING:
            case Status.OK:
                return true;
            default:
                return false;
        }
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
        IStatus status = validatePackageName(name);
        if (status.getSeverity() == IStatus.ERROR) {
            throw new IllegalStateException(status.getMessage());
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
        IStatus status = validatePackageName(name);
        switch (status.getSeverity()) {
            case Status.WARNING:
            case Status.OK:
                return true;
            default:
                return false;
        }
    }

    private static IStatus validateCompilationUnitName(String name) {
//        return JavaConventions.validateCompilationUnitName(name, JavaCore.getOption(JavaCore.COMPILER_SOURCE),
//                                                           JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
        //TODO provide more simple way to check java names
        return Status.OK_STATUS;
    }

    private static IStatus validatePackageName(String name) {
//        return JavaConventions.validatePackageName(name, JavaCore.getOption(JavaCore.COMPILER_SOURCE),
//                                                   JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE));
        //TODO provide more simple way to check java names
        return Status.OK_STATUS;
    }

}