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
package org.eclipse.che.ide.api.user;

import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.validation.constraints.NotNull;

/**
 * GWT Client for User Service.
 *
 * @author Ann Shumilova
 */
public interface UserServiceClient {

    /**
     * Create new user.
     *
     * @param token
     *         user's token
     * @param isTemporary
     *         if <code>true</code> - is temporary user
     * @param callback
     */
    void createUser(@NotNull String token, boolean isTemporary, AsyncRequestCallback<UserDto> callback);

    /**
     * Get current user's information.
     *
     * @param callback
     */
    void getCurrentUser(AsyncRequestCallback<UserDto> callback);

    /**
     * Update user's password.
     *
     * @param password
     *         new password
     * @param callback
     */
    void updatePassword(@NotNull String password, AsyncRequestCallback<Void> callback);

    /**
     * Get user's information by its id.
     *
     * @param id
     *         user's id
     * @param callback
     */
    void getUserById(@NotNull String id, AsyncRequestCallback<UserDto> callback);

    /**
     * Get user's information by its alias.
     *
     * @param alias
     *         user's alias
     * @param callback
     */
    void getUserByAlias(@NotNull String alias, AsyncRequestCallback<UserDto> callback);

    /**
     * Remove user.
     *
     * @param id
     *         user's id to remove
     * @param callback
     */
    void removeUser(@NotNull String id, AsyncRequestCallback<Void> callback);
}
