/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.machine.jpa.listener;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.recipe.JpaRecipeDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.multiuser.api.permission.server.jpa.listener.RemovePermissionsOnLastUserRemovedEventSubscriber;
import org.eclipse.che.multiuser.permission.machine.jpa.JpaRecipePermissionsDao;

/**
 * Listens for {@link UserImpl} removal events, and checks if the removing user is the last who have
 * "setPermissions" role to particular recipe, and if it is, then removes recipe itself.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class RemoveRecipeOnLastUserRemovedEventSubscriber
    extends RemovePermissionsOnLastUserRemovedEventSubscriber<JpaRecipePermissionsDao> {

  @Inject private JpaRecipeDao recipeDao;

  @Override
  public void remove(String instanceId) throws ServerException {
    recipeDao.remove(instanceId);
  }
}
