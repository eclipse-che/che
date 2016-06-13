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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.regex.Pattern;

/**
 * Utils for username validation and normalization.
 *
 * @author Mihail Kuznyetsov
 */
public class UserNameValidator {
    private static final Logger LOG = LoggerFactory.getLogger(UserNameValidator.class);

    private static final Pattern ILLEGAL_USERNAME_CHARACTERS = Pattern.compile("[^a-zA-Z0-9]");
    private static final Pattern VALID_USERNAME              = Pattern.compile("^[a-zA-Z0-9]*");

    private final UserManager userManager;

    @Inject
    public UserNameValidator (UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Validate name, if it doesn't contain illegal characters
     *
     * @param name
     *        username
     * @return true if valid name, false otherwise
     */
    public boolean isValidUserName(String name) {
        return VALID_USERNAME.matcher(name).matches();
    }

    /**
     * Remove illegal characters from username, to make it URL-friendly.
     * If all characters are illegal, return automatically generated username.
     * Also ensures username is unique, if not, adds digits to it's end.
     *
     * @param name
     *        username
     * @return username without illegal characters
     */
    public String normalizeUserName(String name) throws ServerException {
        String normalized = ILLEGAL_USERNAME_CHARACTERS.matcher(name).replaceAll("");
        String candidate = normalized.isEmpty() ? NameGenerator.generate("username", 4) : normalized;

        int i = 1;
        try {
            while (userExists(candidate)) {
                candidate = normalized.isEmpty() ? NameGenerator.generate("username", 4) : normalized + String.valueOf(i++);
            }
        } catch (ServerException e) {
            LOG.warn("Error occured during username normalization", e);
            throw e;
        }
        return candidate;
    }

    private boolean userExists(String username) throws ServerException {
        try {
            userManager.getByName(username);
        } catch (NotFoundException e) {
            return false;
        } catch (ServerException e) {
            throw e;
        }
        return true;
    }
}
