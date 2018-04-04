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
