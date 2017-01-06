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
package org.eclipse.che.ide.util;

import com.google.gwt.regexp.shared.RegExp;

/**
 * Utility methods for validating file/folder/project name.
 *
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 */
public class NameUtils {
    private static RegExp FILE_NAME    = RegExp.compile("^((?![*:\\/\\\\\"?<>|\0]).)+$");
    private static RegExp FOLDER_NAME  = FILE_NAME;
    private static RegExp PROJECT_NAME = RegExp.compile("^[A-Za-z0-9_\\-\\.]+$");

    private NameUtils() {
    }

    /**
     * Check file name.
     *
     * @param name
     *         the name
     * @return {@code true} if name is valid and {@code false} otherwise
     */
    public static boolean checkFileName(String name) {
        return FILE_NAME.test(name);
    }

    /**
     * Check folder name.
     *
     * @param name
     *         the name
     * @return {@code true} if name is valid and {@code false} otherwise
     */
    public static boolean checkFolderName(String name) {
        return FOLDER_NAME.test(name);
    }

    /**
     * Check project name.
     *
     * @param name
     *         the name
     * @return {@code true} if name is valid and {@code false} otherwise
     */
    public static boolean checkProjectName(String name) {
        return PROJECT_NAME.test(name);
    }
}
