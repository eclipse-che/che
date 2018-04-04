/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.workspace.server.jpa.listener;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.jpa.JpaStackDao;
import org.eclipse.che.multiuser.api.permission.server.jpa.listener.RemovePermissionsOnLastUserRemovedEventSubscriber;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.JpaStackPermissionsDao;

/**
 * Listens for {@link UserImpl} removal events, and checks if the removing user is the last who have
 * "setPermissions" role to particular stack, and if it is, then removes stack itself.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class RemoveStackOnLastUserRemovedEventSubscriber
    extends RemovePermissionsOnLastUserRemovedEventSubscriber<JpaStackPermissionsDao> {

  @Inject private JpaStackDao stackDao;

  @Override
  public void remove(String instanceId) throws ServerException {
    stackDao.remove(instanceId);
  }
}
