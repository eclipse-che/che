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
package org.eclipse.che.api.machine.server.recipe;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.shared.Group;
import org.eclipse.che.api.machine.shared.Permissible;
import org.eclipse.che.api.machine.shared.dto.recipe.GroupDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.PermissionsDescriptor;

//TODO add methods for 'all' 'any' strategies

/**
 * Helps to check access to {@link Permissible}.
 *
 * @author Eugene Voevodin
 * @author Alexander Andrienko
 */
public interface PermissionsChecker {

    /**
     * Checks that user with id {@code userId} has access to {@code permissible} with given {@code permission}
     *
     * @param permissible
     *         data object to check permissions
     * @param userId
     *         user identifier
     * @param permission
     *          permission which user should have to access data object
     * @return {@code true} when user with identifier {@code userId} has access
     *          to {@code permissible} with given {@code permission} otherwise returns {@code false}
     * @throws ServerException
     *         when any error occurs while checking access
     */
    boolean hasAccess(Permissible permissible, String userId, String permission) throws ServerException;

    /**
     * Checks that {@code permissions} contains "public: search" permission {@link Group}
     *
     * @param permissions
     *          permissions which allows access to data object {@see PermissionDescriptor}
     *
     * @return {@code true} when {@link Group} list of {@code permissions} contains "public: search" group, otherwise returns {@code false}
     */
    default boolean hasPublicSearchPermission(PermissionsDescriptor permissions) {
        for (GroupDescriptor group : permissions.getGroups()) {
            if ("public".equalsIgnoreCase(group.getName()) && group.getAcl().contains("search")) {
                return true;
            }
        }
        return false;
    }
}
