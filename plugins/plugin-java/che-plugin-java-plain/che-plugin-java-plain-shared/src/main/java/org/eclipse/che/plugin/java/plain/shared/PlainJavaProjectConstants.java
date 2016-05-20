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
package org.eclipse.che.plugin.java.plain.shared;

/**
 * @author Valeriy Svydenko
 */
public final class PlainJavaProjectConstants {
    public static String PLAIN_JAVA_PROJECT_ID       = "plainJava";
    public static String PLAIN_JAVA_PROJECT_NAME     = "Java";
    public static String DEFAULT_SOURCE_FOLDER_VALUE = "src";
    public static String DEFAULT_OUTPUT_FOLDER_VALUE = "bin";

    private PlainJavaProjectConstants() {
        throw new UnsupportedOperationException("Unused constructor");
    }
}
