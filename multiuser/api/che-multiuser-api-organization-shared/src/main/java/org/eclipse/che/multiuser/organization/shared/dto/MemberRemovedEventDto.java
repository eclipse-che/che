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
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.multiuser.organization.shared.event.EventType;

/**
 * DTO for organization member removed event.
 *
 * @author Anton Korneta
 */
@DTO
@EventOrigin("organization")
public interface MemberRemovedEventDto extends OrganizationEventDto {

  @Override
  MemberRemovedEventDto withOrganization(OrganizationDto organization);

  @Override
  MemberRemovedEventDto withType(EventType eventType);

  UserDto getMember();

  void setMember(UserDto member);

  MemberRemovedEventDto withMember(UserDto member);

  /** Returns name of user who initiated member removal */
  String getInitiator();

  void setInitiator(String initiator);

  MemberRemovedEventDto withInitiator(String initiator);
}
