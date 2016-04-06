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
package org.eclipse.che.ide.ext.java.shared;

/** @author Artem Zatsarynnyi */
public interface Constants {
    // project categories
    final String JAVA_CATEGORY       = "Java";
    // project attribute names
    final String LANGUAGE            = "language";
    final String LANGUAGE_VERSION    = "languageVersion";
    final String FRAMEWORK           = "framework";
    final String CONTAINS_JAVA_FILES = "containsJavaFiles";
    final String JAVA_ID             = "java";

    // simple java project
    String SIMPLE_JAVA_PROJECT_ID      = "simpleJava";
    String SIMPLE_JAVA_PROJECT_NAME    = "Simple Java Project";
    String LIBRARY_FOLDER              = "lib";
    //simple java project attributes
    String DEFAULT_SOURCE_FOLDER       = "java.source.folder";
    String DEFAULT_SOURCE_FOLDER_VALUE = "src";
}
