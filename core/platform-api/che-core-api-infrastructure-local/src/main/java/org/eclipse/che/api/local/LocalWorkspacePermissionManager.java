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
package org.eclipse.che.api.local;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.permission.Operation;
import org.eclipse.che.api.core.rest.permission.PermissionManager;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author Eugene Voevodin
 */
@Singleton
public class LocalWorkspacePermissionManager implements PermissionManager {
    @Override
    public void checkPermission(@NotNull Operation operation,
                                @NotNull String userId,
                                @NotNull Map<String, String> params) throws ForbiddenException, ServerException {}
}
