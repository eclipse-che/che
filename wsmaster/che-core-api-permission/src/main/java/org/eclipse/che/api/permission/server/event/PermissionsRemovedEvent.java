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
package org.eclipse.che.api.permission.server.event;

import static org.eclipse.che.api.permission.shared.event.EventType.PERMISSIONS_REMOVED;

import org.eclipse.che.api.permission.shared.event.EventType;
import org.eclipse.che.api.permission.shared.event.PermissionsEvent;
import org.eclipse.che.api.permission.shared.model.Permissions;
import org.eclipse.che.commons.annotation.Nullable;

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
