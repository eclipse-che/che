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
package org.eclipse.che.multiuser.organization.api.event;

import static org.eclipse.che.multiuser.organization.shared.event.EventType.ORGANIZATION_RENAMED;

import org.eclipse.che.multiuser.organization.shared.event.EventType;
import org.eclipse.che.multiuser.organization.shared.event.OrganizationEvent;
import org.eclipse.che.multiuser.organization.shared.model.Organization;

/**
 * Defines organization renamed event.
 *
 * @author Anton Korneta
 */
public class OrganizationRenamedEvent implements OrganizationEvent {

  private final String initiator;
  private final String oldName;
  private final String newName;
  private final Organization organization;

  public OrganizationRenamedEvent(
      String initiator, String oldName, String newName, Organization organization) {
    this.initiator = initiator;
    this.oldName = oldName;
    this.newName = newName;
    this.organization = organization;
  }

  @Override
  public Organization getOrganization() {
    return organization;
  }

  @Override
  public EventType getType() {
    return ORGANIZATION_RENAMED;
  }

  public String getOldName() {
    return oldName;
  }

  public String getNewName() {
    return newName;
  }

  @Override
  public String getInitiator() {
    return initiator;
  }
}
