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
package org.eclipse.che.multiuser.api.permission.server.event;

import static org.eclipse.che.multiuser.api.permission.shared.event.EventType.PERMISSIONS_REMOVED;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.multiuser.api.permission.shared.event.EventType;
import org.eclipse.che.multiuser.api.permission.shared.event.PermissionsEvent;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;

/**
 * Defines permissions added events.
 *
 * @author Anton Korneta
 */
public class PermissionsRemovedEvent implements PermissionsEvent {

  private final String initiator;
  private final Permissions permissions;

  public PermissionsRemovedEvent(String initiator, Permissions permissions) {
    this.initiator = initiator;
    this.permissions = permissions;
  }

  @Override
  public EventType getType() {
    return PERMISSIONS_REMOVED;
  }

  @Override
  public Permissions getPermissions() {
    return permissions;
  }

  @Nullable
  @Override
  public String getInitiator() {
    return initiator;
  }
}
