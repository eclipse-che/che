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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.multiuser.organization.shared.Constants;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;

/**
 * Helps to inject {@link OrganizationService} related links.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class OrganizationLinksInjector {
  public OrganizationDto injectLinks(
      OrganizationDto organizationDto, ServiceContext serviceContext) {
    final UriBuilder uriBuilder = serviceContext.getBaseUriBuilder();
    final List<Link> links = new ArrayList<>(2);
    links.add(
        LinksHelper.createLink(
            HttpMethod.GET,
            uriBuilder
                .clone()
                .path(OrganizationService.class)
                .path(OrganizationService.class, "getById")
                .build(organizationDto.getId())
                .toString(),
            null,
            APPLICATION_JSON,
            Constants.LINK_REL_SELF));
    links.add(
        LinksHelper.createLink(
            HttpMethod.GET,
            uriBuilder
                .clone()
                .path(OrganizationService.class)
                .path(OrganizationService.class, "getByParent")
                .build(organizationDto.getId())
                .toString(),
            null,
            APPLICATION_JSON,
            Constants.LINK_REL_SUBORGANIZATIONS));
    return organizationDto.withLinks(links);
  }
}
