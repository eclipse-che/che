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
package org.eclipse.che.multiuser.organization.shared.dto;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.multiuser.organization.shared.event.EventType;
import org.eclipse.che.multiuser.organization.shared.event.OrganizationEvent;

/**
 * DTO for {@link OrganizationEvent}.
 *
 * @author Anton Korneta
 */
@DTO
@EventOrigin("organization")
public interface OrganizationEventDto extends OrganizationEvent {

  @Override
  OrganizationDto getOrganization();

  void setOrganization(OrganizationDto organization);

  OrganizationEventDto withOrganization(OrganizationDto organization);

  void setType(EventType eventType);

  OrganizationEventDto withType(EventType eventType);
}
