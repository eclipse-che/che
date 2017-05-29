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
package org.eclipse.che.plugin.pullrequest.client.vcs.hosting;

import javax.validation.constraints.NotNull;

/**
 * Exception raised when trying to get a fork of a repository for a user and no fork being found.
 */
public class NoUserForkException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an instance of {@link NoUserForkException}.
     *
     * @param user
     *         the user.
     */
    public NoUserForkException(@NotNull final String user) {
        super("No fork for user: " + user);
    }
}
