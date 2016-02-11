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
import org.eclipse.che.api.machine.shared.ManagedRecipe;

//TODO add methods for 'all' 'any' strategies

/**
 * Helps to check access to {@link ManagedRecipe}.
 *
 * @author Eugene Voevodin
 */
public interface PermissionsChecker {

    /**
     * Checks that user with id {@code userId} has access to {@code recipe} with given {@code permission}
     *
     * @param recipe
     *         recipe to check permissions
     * @param userId
     *         user identifier
     * @param permission
     *         permission which user should have to access recipe
     * @return {@code true} when user with identifier {@code userId} has access
     * to {@code recipe} with given {@code permission} otherwise returns {@code false}
     * @throws ServerException
     *         when any error occurs while checking access
     */
    boolean hasAccess(ManagedRecipe recipe, String userId, String permission) throws ServerException;
}