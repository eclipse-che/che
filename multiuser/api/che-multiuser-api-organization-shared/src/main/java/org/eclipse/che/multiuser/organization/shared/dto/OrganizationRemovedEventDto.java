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

import java.util.List;
import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.multiuser.organization.shared.event.EventType;

/**
 * DTO for organization removed event.
 *
 * @author Anton Korneta
 */
@DTO
@EventOrigin("organization")
public interface OrganizationRemovedEventDto extends OrganizationEventDto {

  @Override
  OrganizationRemovedEventDto withOrganization(OrganizationDto organization);

  @Override
  OrganizationRemovedEventDto withType(EventType eventType);

  /** Returns name of user who initiated organization removal */
  String getInitiator();

  void setInitiator(String initiator);

  OrganizationRemovedEventDto withInitiator(String initiator);

  List<String> getMembers();

  void setMembers(List<String> members);

  OrganizationRemovedEventDto withMembers(List<String> members);
}
