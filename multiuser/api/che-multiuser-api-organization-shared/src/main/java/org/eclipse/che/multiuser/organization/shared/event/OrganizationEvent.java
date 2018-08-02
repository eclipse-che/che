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
package org.eclipse.che.multiuser.organization.shared.event;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.multiuser.organization.shared.model.Organization;

/**
 * The base interface for organization event.
 *
 * @author Anton Korneta
 */
public interface OrganizationEvent {

  /** Returns organization related to this event. */
  Organization getOrganization();

  /** Returns type of this event. */
  EventType getType();

  /** Returns name of user who acted with organization or null if user is undefined. */
  @Nullable
  String getInitiator();
}
