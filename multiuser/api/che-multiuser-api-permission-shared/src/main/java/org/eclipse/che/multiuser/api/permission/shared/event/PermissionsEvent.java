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
package org.eclipse.che.multiuser.api.permission.shared.event;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;

/**
 * The base interface for all events related to permissions.
 *
 * @author Anton Korneta
 */
public interface PermissionsEvent {

  /** Returns the permissions related to this event. */
  Permissions getPermissions();

  /** Returns concrete event type, see {@link EventType}. */
  EventType getType();

  /** Returns name of user who acted with permission or null if user is undefined. */
  @Nullable
  String getInitiator();
}
