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

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.commons.lang.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;

// TODO extract normalization code from the validator as it is not related to the validation at all
/**
 * Utils for username validation and normalization.
 *
 * @author Mihail Kuznyetsov
 * @author Yevhenii Voevodin
 */
public class UserValidator {
    private static final Logger LOG = LoggerFactory.getLogger(UserValidator.class);

    private static final Pattern ILLEGAL_USERNAME_CHARACTERS = Pattern.compile("[^a-zA-Z0-9]");
    private static final Pattern VALID_USERNAME              = Pattern.compile("^[a-zA-Z0-9]*");

    private final UserManager userManager;

    @Inject
    public UserValidator(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Checks whether given user is valid.
     *
     * @param user
     *         user to check
     * @throws BadRequestException
     *         when user is not valid
     */
    public void checkUser(User user) throws BadRequestException {
        if (user == null) {
            throw new BadRequestException("User required");
        }
        if (isNullOrEmpty(user.getName())) {
            throw new BadRequestException("User name required");
        }
        if (!isValidName(user.getName())) {
            throw new BadRequestException("Username must contain only letters and digits");
        }
        if (isNullOrEmpty(user.getEmail())) {
            throw new BadRequestException("User email required");
        }
        if (user.getPassword() != null) {
            checkPassword(user.getPassword());
        }
    }

    /**
     * Checks whether password is ok.
     *
     * @param password
     *         password to check
     * @throws BadRequestException
     *         when password is not valid
     */
    public void checkPassword(String password) throws BadRequestException {
        if (password == null) {
            throw new BadRequestException("Password required");
        }
        if (password.length() < 8) {
            throw new BadRequestException("Password should contain at least 8 characters");
        }
        int numOfLetters = 0;
        int numOfDigits = 0;
        for (char passwordChar : password.toCharArray()) {
            if (Character.isDigit(passwordChar)) {
                numOfDigits++;
            } else if (Character.isLetter(passwordChar)) {
                numOfLetters++;
            }
        }
        if (numOfDigits == 0 || numOfLetters == 0) {
            throw new BadRequestException("Password should contain letters and digits");
        }
    }

    /**
     * Validate name, if it doesn't contain illegal characters
     *
     * @param name
     *         username
     * @return true if valid name, false otherwise
     */
    public boolean isValidName(String name) {
        return name != null && VALID_USERNAME.matcher(name).matches();
    }

    /**
     * Remove illegal characters from username, to make it URL-friendly.
     * If all characters are illegal, return automatically generated username.
     * Also ensures username is unique, if not, adds digits to it's end.
     *
     * @param name
     *         username
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
            LOG.warn("Error occurred during username normalization", e);
            throw e;
        }
        return candidate;
    }

    private boolean userExists(String username) throws ServerException {
        try {
            userManager.getByName(username);
        } catch (NotFoundException e) {
            return false;
        }
        return true;
    }
}
