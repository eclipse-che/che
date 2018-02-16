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

  public List<OrganizationDto> getAll() throws Exception {
    return requestFactory.fromUrl(getApiUrl()).request().asList(OrganizationDto.class);
  }

  public List<OrganizationDto> getAllRoot() throws Exception {
    return getAll(null);
  }

  public List<OrganizationDto> getAll(@Nullable String parent) throws Exception {
    List<OrganizationDto> organizations =
        requestFactory.fromUrl(getApiUrl()).request().asList(OrganizationDto.class);

    organizations.removeIf(o -> !o.getParent().equals(parent));
    return organizations;
  }

  private String getApiUrl() {
    return apiEndpoint + "organization/";
  }

  public OrganizationDto create(String name, @Nullable String parentId) throws Exception {
    OrganizationDto data = newDto(OrganizationDto.class).withName(name).withParent(parentId);

    OrganizationDto organizationDto =
        requestFactory
            .fromUrl(getApiUrl())
            .setBody(data)
            .usePostMethod()
            .request()
            .asDto(OrganizationDto.class);

    LOG.info(
        "Organization with name='{}', id='{}', parent's id='{}' created",
        name,
        organizationDto.getId(),
        parentId);

    return organizationDto;
  }

  public OrganizationDto create(String name) throws Exception {
    return create(name, null);
  }

  public void deleteById(String id) throws Exception {
    String apiUrl = format("%s%s", getApiUrl(), id);

    try {
      requestFactory.fromUrl(apiUrl).useDeleteMethod().request();
      LOG.info("Organization with id='{}' removed", id);
    } catch (NotFoundException e) {
      // ignore if there is no organization of certain id
    }
  }

  public void deleteByName(String name) throws Exception {
    try {
      String organizationId = get(name).getId();
      deleteById(organizationId);
    } catch (NotFoundException e) {
      // ignore if there is no organization of certain id
    }
  }

  public void deleteAll() throws Exception {
    getAll()
        .stream()
        .filter(organization -> organization.getParent() != null)
        .forEach(
            organization -> {
              try {
                deleteById(organization.getId());
              } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            });
  }

  public OrganizationDto get(String organizationName) throws Exception {
    String apiUrl = format("%sfind?name=%s", getApiUrl(), organizationName);
    return requestFactory.fromUrl(apiUrl).request().asDto(OrganizationDto.class);
  }

  public void addMember(String organizationId, String userId) throws Exception {
    addMember(organizationId, userId, asList("createWorkspaces"));
  }

  public void addAdmin(String organizationId, String userId) throws Exception {
    addMember(
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

  public void addMember(String organizationId, String userId, List<String> actions)
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
