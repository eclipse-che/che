/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.organization.api.listener;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.multiuser.api.permission.server.jpa.listener.RemovePermissionsOnLastUserRemovedEventSubscriber;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.spi.jpa.JpaMemberDao;

/**
 * Listens for {@link UserImpl} removal events, and checks if the removing user is the last who has
 * "setPermissions" permission to particular organization, and if it is, then removes organization
 * itself.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class RemoveOrganizationOnLastUserRemovedEventSubscriber
    extends RemovePermissionsOnLastUserRemovedEventSubscriber<JpaMemberDao> {

  @Inject private OrganizationManager organizationManager;

  @Override
  public void remove(String instanceId) throws ServerException {
    organizationManager.remove(instanceId);
  }
}
