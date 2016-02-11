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
package org.eclipse.che.api.core.rest.permission;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.annotation.Nullable;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;

/**
 * Manages access to named operations.
 *
 * @author Eugene Voevodin
 */
public interface PermissionManager {

    /**
     * Checks that user with given id has access to perform the operation.
     *
     * @param operation
     *         operation for which access should be allowed or rejected
     * @param params
     *         additional parameters for access check
     * @param userId
     *         id of the user who requests access to the operation
     * @throws NullPointerException
     *         when either operation, or params, or user identifier is null
     * @throws ForbiddenException
     *         when user doesn't have access to operation
     * @throws ServerException
     *         when any other error occurs
     */
    void checkPermission(@NotNull Operation operation,
                         @NotNull String userId,
                         @NotNull Map<String, String> params) throws ForbiddenException, ServerException;

    /**
     * Checks that user with given id has access to perform the operation.
     *
     * <p>Checks permission without additional parameters
     *
     * @param operation
     *         operation for which access should be allowed or rejected
     * @param userId
     *         id of the user who requests access to the operation
     * @throws NullPointerException
     *         when either operation or user identifier is null
     * @throws ForbiddenException
     *         when user doesn't have access to operation
     * @throws ServerException
     *         when any other error occurs
     */
    default void checkPermission(@NotNull Operation operation, @NotNull String userId) throws ForbiddenException, ServerException {
        checkPermission(operation, userId, Collections.emptyMap());
    }

    /**
     * Checks that user with given id has access to perform the operation.
     *
     * <p>This method is the same to {@link #checkPermission(Operation, String, Map)},
     * the only difference is that it provides more convenient way to check permission
     * when single parameter appears.
     *
     * <p>For instance:
     * <pre>{@code
     *     manager.checkPermission(new Operation("start-workspace"), "user123", "workspaceId", workspaceId)
     * }</pre>
     *
     * @param operation
     *         operation for which access should be allowed or rejected
     * @param userId
     *         id of the user who requests access to the operation
     * @param paramKey
     *         single parameter key
     * @param paramValue
     *         single parameter value
     * @throws NullPointerException
     *         when either operation or user identifier is null
     * @throws ForbiddenException
     *         when user doesn't have access to operation
     * @throws ServerException
     *         when any other error occurs
     */
    default void checkPermission(@NotNull Operation operation,
                                 @NotNull String userId,
                                 @Nullable String paramKey,
                                 @Nullable String paramValue) throws ForbiddenException, ServerException {
        checkPermission(operation, userId, Collections.singletonMap(paramKey, paramValue));
    }

    /**
     * Returns true when user has permission to perform given operation, otherwise returns false.
     *
     * @param operation
     *         operation for which access should be allowed or rejected
     * @param params
     *         additional parameters for access check
     * @param userId
     *         id of the user who requests access to the operation
     * @throws NullPointerException
     *         when either operation, or params, or user identifier is null
     * @throws ServerException
     *         when any other error occurs
     */
    default boolean hasPermission(Operation operation, String userId, Map<String, String> params) throws ServerException {
        try {
            checkPermission(operation, userId, params);
            return true;
        } catch (ForbiddenException ignored) {
            return false;
        }
    }
}
