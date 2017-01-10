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
package org.eclipse.che.plugin.nodejs.shared;

/**
 * @author Dmitry Shnurenko
 */
public final class Constants {
    /**
     * Options for run command
     */
    public static final String RUN_PARAMETERS_ATTRIBUTE = "run.parameters";
    /**
     * Language attribute name
     */
    public static       String LANGUAGE                 = "language";
    /**
     * Node JS Project Type ID
     */
    public static       String NODE_JS_PROJECT_TYPE_ID  = "node-js";

    /**
     * Default extension for C files
     */
    public static String NODE_JS_FILE_EXT = "js";

    private Constants() {
        throw new UnsupportedOperationException("You can't create instance of Constants class");
    }
}
