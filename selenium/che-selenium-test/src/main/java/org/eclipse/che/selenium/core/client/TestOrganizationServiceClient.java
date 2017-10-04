/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.multiuser.api.permission.shared.dto.PermissionsDto;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This util is handling the requests to Organization API. */
@Singleton
public class TestOrganizationServiceClient {
  private static final Logger LOG = LoggerFactory.getLogger(TestOrganizationServiceClient.class);

  private final String apiEndpoint;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public TestOrganizationServiceClient(
      TestApiEndpointUrlProvider apiEndpointUrlProvider, HttpJsonRequestFactory requestFactory) {
    this.apiEndpoint = apiEndpointUrlProvider.get().toString();
    this.requestFactory = requestFactory;
  }

  public List<OrganizationDto> getOrganizations() throws Exception {
    return getOrganizations(null);
  }

  public List<OrganizationDto> getOrganizations(@Nullable String parent) throws Exception {
    List<OrganizationDto> organizations =
        requestFactory.fromUrl(getApiUrl()).request().asList(OrganizationDto.class);

    if (parent == null) {
      organizations.removeIf(o -> o.getParent() != null);
    }

    return organizations;
  }

  private String getApiUrl() {
    return apiEndpoint + "organization/";
  }

  public OrganizationDto createOrganization(String name, String parentId) throws Exception {
    OrganizationDto data = newDto(OrganizationDto.class).withName(name).withParent(parentId);

    OrganizationDto organizationDto =
        requestFactory
            .fromUrl(getApiUrl())
            .setBody(data)
            .usePostMethod()
            .request()
            .asDto(OrganizationDto.class);

    LOG.debug(
        "Organization with name='{}', id='{}' and parent's id='{}' created",
        name,
        organizationDto.getId(),
        parentId);

    return organizationDto;
  }

  public OrganizationDto createOrganization(String name) throws Exception {
    return createOrganization(name, null);
  }

  public void deleteOrganizationById(String id) throws Exception {
    String apiUrl = format("%s%s", getApiUrl(), id);

    try {
      requestFactory.fromUrl(apiUrl).useDeleteMethod().request();
    } catch (NotFoundException e) {
      // ignore if there is no organization of certain id
    }

    LOG.debug("Organization with id='{}' removed", id);
  }

  public void deleteOrganizationByName(String name) throws Exception {
    OrganizationDto organization = getOrganization(name);

    if (organization != null) {
      deleteOrganizationById(organization.getId());
    }
  }

  public void deleteAllOrganizations(String user) throws Exception {
    getOrganizations(user)
        .stream()
        .filter(organization -> organization.getParent() != null)
        .forEach(
            organization -> {
              try {
                deleteOrganizationById(organization.getId());
              } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            });
  }

  public OrganizationDto getOrganization(String organizationName) throws Exception {
    String apiUrl = format("%sfind?name=%s", getApiUrl(), organizationName);
    return requestFactory.fromUrl(apiUrl).request().asDto(OrganizationDto.class);
  }

  public void addOrganizationMember(String organizationId, String userId) throws Exception {
    addOrganizationMember(organizationId, userId, asList("createWorkspaces"));
  }

  public void addOrganizationAdmin(String organizationId, String userId) throws Exception {
    addOrganizationMember(
        organizationId,
        userId,
        asList(
            "update",
            "setPermissions",
            "manageResources",
            "manageWorkspaces",
            "createWorkspaces",
            "delete",
            "manageSuborganizations"));
  }

  public void addOrganizationMember(String organizationId, String userId, List<String> actions)
      throws Exception {
    String apiUrl = apiEndpoint + "permissions";
    PermissionsDto data =
        newDto(PermissionsDto.class)
            .withDomainId("organization")
            .withInstanceId(organizationId)
            .withUserId(userId)
            .withActions(actions);

    requestFactory.fromUrl(apiUrl).setBody(data).usePostMethod().request();
  }
}
