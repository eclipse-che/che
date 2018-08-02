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
package org.eclipse.che.multiuser.organization.api.event;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.core.db.cascade.event.PersistEvent;
import org.eclipse.che.multiuser.organization.shared.model.Organization;

/**
 * Published after organization instance is persisted.
 *
 * @author Sergii Leschenko
 */
@EventOrigin("organization")
public class OrganizationPersistedEvent extends PersistEvent {
  private final Organization organization;

  public OrganizationPersistedEvent(Organization organization) {
    this.organization = organization;
  }

  public Organization getOrganization() {
    return organization;
  }
}
