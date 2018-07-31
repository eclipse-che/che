/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.organization.shared.dto;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.multiuser.organization.shared.event.EventType;

/**
 * DTO for organization renamed event.
 *
 * @author Anton Korneta
 */
@DTO
@EventOrigin("organization")
public interface OrganizationRenamedEventDto extends OrganizationEventDto {

  @Override
  OrganizationRenamedEventDto withOrganization(OrganizationDto organization);

  @Override
  OrganizationRenamedEventDto withType(EventType eventType);

  /** Returns organization name before renaming */
  String getOldName();

  void setOldName(String oldName);

  OrganizationRenamedEventDto withOldName(String oldName);

  /** Returns organization name after renaming */
  String getNewName();

  void setNewName(String newName);

  OrganizationRenamedEventDto withNewName(String newName);

  /** Returns name of user who initiated organization rename */
  String getInitiator();

  void setInitiator(String initiator);

  OrganizationRenamedEventDto withInitiator(String initiator);
}
