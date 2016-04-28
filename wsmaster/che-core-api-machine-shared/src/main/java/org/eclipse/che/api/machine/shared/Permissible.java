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
package org.eclipse.che.api.machine.shared;



/**
 * Permissible interface should be implemented by data objects which require access.
 * It is commonly used with the {@link org.eclipse.che.api.machine.server.recipe.PermissionsChecker}.
 *
 * @author Alexander Andrienko
 */
public interface Permissible {
    /**
     * Returns {@link Permissions} for access to the data object.
     */
    Permissions getPermissions();
}
