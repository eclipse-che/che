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

import static org.eclipse.che.multiuser.organization.shared.event.EventType.ORGANIZATION_REMOVED;

import java.util.List;
import org.eclipse.che.multiuser.organization.shared.event.EventType;
import org.eclipse.che.multiuser.organization.shared.event.OrganizationEvent;
import org.eclipse.che.multiuser.organization.shared.model.Organization;

/**
 * Defines organization removed event.
 *
 * @author Anton Korneta
 */
public class OrganizationRemovedEvent implements OrganizationEvent {

  private final String initiator;
  private final Organization organization;
  private final List<String> members;

  public OrganizationRemovedEvent(
      String initiator, Organization organization, List<String> members) {
    this.initiator = initiator;
    this.organization = organization;
    this.members = members;
  }

  @Override
  public EventType getType() {
    return ORGANIZATION_REMOVED;
  }

  @Override
  public Organization getOrganization() {
    return organization;
  }

  public List<String> getMembers() {
    return members;
  }

  /** Returns name of user who initiated organization removal */
  @Override
  public String getInitiator() {
    return initiator;
  }
}
