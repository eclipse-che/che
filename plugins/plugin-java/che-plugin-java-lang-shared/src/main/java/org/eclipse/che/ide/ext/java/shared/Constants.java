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
package org.eclipse.che.ide.ext.java.shared;

/**
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 */
public final class Constants {
    // project categories
    public static String JAVA_CATEGORY               = "Java";
    public static String JAVA_ID                     = "java";
    // project attribute names
    public static String LANGUAGE                    = "language";
    public static String LANGUAGE_VERSION            = "languageVersion";
    public static String FRAMEWORK                   = "framework";
    public static String CONTAINS_JAVA_FILES         = "containsJavaFiles";
    public static String SOURCE_FOLDER               = "java.source.folder";
    public static String OUTPUT_FOLDER               = "java.output.folder";

    public static String JAVAC                       = "javac";

    private Constants() {
        throw new UnsupportedOperationException("Unused constructor.");
    }
}
