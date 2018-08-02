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
package org.eclipse.che.multiuser.organization.api;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.stream.Collectors;
import org.eclipse.che.multiuser.organization.api.event.MemberAddedEvent;
import org.eclipse.che.multiuser.organization.api.event.MemberRemovedEvent;
import org.eclipse.che.multiuser.organization.api.event.OrganizationRemovedEvent;
import org.eclipse.che.multiuser.organization.api.event.OrganizationRenamedEvent;
import org.eclipse.che.multiuser.organization.shared.dto.MemberAddedEventDto;
import org.eclipse.che.multiuser.organization.shared.dto.MemberRemovedEventDto;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDistributedResourcesDto;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationEventDto;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationRemovedEventDto;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationRenamedEventDto;
import org.eclipse.che.multiuser.organization.shared.event.OrganizationEvent;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.eclipse.che.multiuser.organization.shared.model.OrganizationDistributedResources;

/**
 * Helps to convert objects related to organization to DTOs.
 *
 * @author Sergii Leschenko
 */
public final class DtoConverter {
  private DtoConverter() {}

  public static OrganizationDto asDto(Organization organization) {
    return newDto(OrganizationDto.class)
        .withId(organization.getId())
        .withName(organization.getName())
        .withQualifiedName(organization.getQualifiedName())
        .withParent(organization.getParent());
  }

  public static OrganizationDistributedResourcesDto asDto(
      OrganizationDistributedResources distributedResources) {
    return newDto(OrganizationDistributedResourcesDto.class)
        .withOrganizationId(distributedResources.getOrganizationId())
        .withResourcesCap(
            distributedResources
                .getResourcesCap()
                .stream()
                .map(org.eclipse.che.multiuser.resource.api.DtoConverter::asDto)
                .collect(Collectors.toList()));
  }

  public static OrganizationRemovedEventDto asDto(OrganizationRemovedEvent event) {
    return newDto(OrganizationRemovedEventDto.class)
        .withType(event.getType())
        .withOrganization(asDto(event.getOrganization()))
        .withMembers(event.getMembers())
        .withInitiator(event.getInitiator());
  }

  public static OrganizationRenamedEventDto asDto(OrganizationRenamedEvent event) {
    return newDto(OrganizationRenamedEventDto.class)
        .withType(event.getType())
        .withOrganization(asDto(event.getOrganization()))
        .withOldName(event.getOldName())
        .withNewName(event.getNewName())
        .withInitiator(event.getInitiator());
  }

  public static MemberAddedEventDto asDto(MemberAddedEvent event) {
    return newDto(MemberAddedEventDto.class)
        .withType(event.getType())
        .withOrganization(asDto(event.getOrganization()))
        .withInitiator(event.getInitiator())
        .withMember(org.eclipse.che.api.user.server.DtoConverter.asDto(event.getMember()));
  }

  public static MemberRemovedEventDto asDto(MemberRemovedEvent event) {
    return newDto(MemberRemovedEventDto.class)
        .withType(event.getType())
        .withOrganization(asDto(event.getOrganization()))
        .withInitiator((event.getInitiator()))
        .withMember(org.eclipse.che.api.user.server.DtoConverter.asDto(event.getMember()));
  }

  public static OrganizationEventDto asDto(OrganizationEvent event) {
    switch (event.getType()) {
      case ORGANIZATION_RENAMED:
        return asDto((OrganizationRenamedEvent) event);
      case ORGANIZATION_REMOVED:
        return asDto((OrganizationRemovedEvent) event);
      case MEMBER_ADDED:
        return asDto((MemberAddedEvent) event);
      case MEMBER_REMOVED:
        return asDto((MemberRemovedEvent) event);
      default:
        throw new IllegalArgumentException(
            "Can't convert event to dto, event type '" + event.getType() + "' is unknown");
    }
  }
}
