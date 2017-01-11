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
package org.eclipse.che.plugin.cpp.shared;

/** @author Vitalii Parfonov */
public final class Constants {

    public static final String BINARY_NAME_ATTRIBUTE         = "binaryName";
    public static final String COMPILATION_OPTIONS_ATTRIBUTE = "compilationOptions";
    /**
     * Language attribute name
     */
    public static String LANGUAGE            = "language";
    /**
     * C Project Type ID
     */
    public static String C_PROJECT_TYPE_ID   = "c";
    /**
     * C++ Project Type ID
     */
    public static String CPP_PROJECT_TYPE_ID = "cpp";
    /**
     * C Language
     */
    public static String C_LANG = "c_lang";
    /**
     * C++ Language
     */
    public static String CPP_LANG = "cpp_lang";
    /**
     * Default extension for C files
     */
    public static String C_EXT = "c";
    /**
     * Default extension for C Headers files
     */
    public static String H_EXT   = "h";
    /**
     * Default extension for C++ files
     */
    public static String CPP_EXT = "cpp";

    private Constants() {
    }
}
