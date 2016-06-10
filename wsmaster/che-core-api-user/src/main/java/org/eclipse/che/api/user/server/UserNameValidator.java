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
package org.eclipse.che.api.user.server;

import java.util.regex.Pattern;

/**
 * Utils for username validation and normalization.
 *
 * @author Mihail Kuznyetsov
 */
public class UserNameValidator {
    private static final Pattern ILLEGAL_USERNAME_CHARACTERS = Pattern.compile("[^a-zA-Z0-9]");
    private static final Pattern VALID_USERNAME              = Pattern.compile("^[a-zA-Z0-9]*");

    /**
     * Validate name, if it doesn't contain illegal characters
     *
     * @param name
     *        username
     * @return true if valid name, false otherwise
     */
    public static boolean isValidUserName(String name) {
        return VALID_USERNAME.matcher(name).matches();
    }

    /**
     * Remove illegal characters from username, to make it URL-friendly.
     *
     * @param name
     *        username
     * @return username without illegal characters
     */
    public static String normalizeUserName(String name) {
        return ILLEGAL_USERNAME_CHARACTERS.matcher(name).replaceAll("");
    }
}
