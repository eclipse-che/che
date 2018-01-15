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
package org.eclipse.che.api.installer.server.impl;

import static org.eclipse.che.api.installer.server.DtoConverter.asDto;
import static org.eclipse.che.api.installer.server.InstallerRegistryService.TOTAL_ITEMS_COUNT_HEADER;

import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.InstallerRegistryService;
import org.eclipse.che.api.installer.server.exception.IllegalInstallerKeyException;
import org.eclipse.che.api.installer.server.exception.InstallerAlreadyExistsException;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.shared.dto.InstallerDto;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote implementation of the {@link InstallerRegistry}.
 *
 * <p>It is designed to fetch data from remote {@link InstallerRegistryService} which is configured
 * by registry.installer.remote property.
 *
 * @author Sergii Leshchenko
 * @author Anatolii Bazko
 */
@Singleton
public class RemoteInstallerRegistry implements InstallerRegistry {
  private static final Logger LOG = LoggerFactory.getLogger(RemoteInstallerRegistry.class);

  private String registryServiceUrl;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public RemoteInstallerRegistry(
      @Nullable @Named("che.installer.registry.remote") String remoteInstallerUrl,
      HttpJsonRequestFactory requestFactory) {
    this.requestFactory = requestFactory;
    if (remoteInstallerUrl != null) {
      try {
        new URL(remoteInstallerUrl);
        this.registryServiceUrl =
            UriBuilder.fromUri(remoteInstallerUrl)
                .path(InstallerRegistryService.class)
                .build()
                .toString();
      } catch (MalformedURLException e) {
        LOG.warn("Configured 'che.installer.registry.remote' is invalid URL.");
        this.registryServiceUrl = null;
      }
    }
  }

  @Override
  public void add(Installer installer) throws InstallerException {
    checkConfiguration();

    try {
      requestFactory
          .fromUrl(
              UriBuilder.fromUri(registryServiceUrl)
                  .path(InstallerRegistryService.class, "add")
                  .build()
                  .toString())
          .setBody(asDto(installer))
          .usePostMethod()
          .request();
    } catch (ConflictException e) {
      throw new InstallerAlreadyExistsException(e.getMessage(), e);
    } catch (IOException | ApiException e) {
      throw new InstallerException(e.getMessage(), e);
    }
  }

  @Override
  public void update(Installer installer) throws InstallerException {
    checkConfiguration();

    try {
      requestFactory
          .fromUrl(
              UriBuilder.fromUri(registryServiceUrl)
                  .path(InstallerRegistryService.class, "update")
                  .build()
                  .toString())
          .setBody(asDto(installer))
          .usePutMethod()
          .request();
    } catch (NotFoundException e) {
      throw new InstallerNotFoundException(e.getMessage(), e);
    } catch (IOException | ApiException e) {
      throw new InstallerException(e.getMessage(), e);
    }
  }

  @Override
  public void remove(String installerKey) throws InstallerException {
    checkConfiguration();

    try {
      requestFactory
          .fromUrl(
              UriBuilder.fromUri(registryServiceUrl)
                  .path(InstallerRegistryService.class, "remove")
                  .build(installerKey)
                  .toString())
          .useDeleteMethod()
          .request();
    } catch (IOException | ApiException e) {
      throw new InstallerException(e.getMessage(), e);
    }
  }

  @Override
  public Installer getInstaller(String installerKey) throws InstallerException {
    checkConfiguration();

    try {
      return requestFactory
          .fromUrl(
              UriBuilder.fromUri(registryServiceUrl)
                  .path(InstallerRegistryService.class, "getInstaller")
                  .build(installerKey)
                  .toString())
          .useGetMethod()
          .request()
          .asDto(InstallerDto.class);
    } catch (NotFoundException e) {
      throw new InstallerNotFoundException(e.getMessage(), e);
    } catch (BadRequestException e) {
      throw new IllegalInstallerKeyException(e.getMessage(), e);
    } catch (IOException | ApiException e) {
      throw new InstallerException(e.getMessage(), e);
    }
  }

  @Override
  public List<String> getVersions(String id) throws InstallerException {
    checkConfiguration();

    try {
      @SuppressWarnings("unchecked")
      List<String> result =
          requestFactory
              .fromUrl(
                  UriBuilder.fromUri(registryServiceUrl)
                      .path(InstallerRegistryService.class, "getVersions")
                      .build(id)
                      .toString())
              .useGetMethod()
              .request()
              .as(List.class, new TypeToken<List<String>>() {}.getType());
      return result;
    } catch (NotFoundException e) {
      throw new InstallerNotFoundException(e.getMessage(), e);
    } catch (IOException | ApiException e) {
      throw new InstallerException(e.getMessage(), e);
    }
  }

  @Override
  public Page<? extends Installer> getInstallers(int maxItems, int skipCount)
      throws InstallerException {
    checkConfiguration();

    try {
      HttpJsonResponse response =
          requestFactory
              .fromUrl(
                  UriBuilder.fromUri(registryServiceUrl)
                      .path(InstallerRegistryService.class, "getInstallers")
                      .queryParam("maxItems", maxItems)
                      .queryParam("skipCount", skipCount)
                      .build()
                      .toString())
              .useGetMethod()
              .request();

      int totalCount = -1;
      List<String> totalItemsCountHeader = response.getHeaders().get(TOTAL_ITEMS_COUNT_HEADER);

      if (totalItemsCountHeader != null && !totalItemsCountHeader.isEmpty()) {
        totalCount = Integer.valueOf(totalItemsCountHeader.get(0));
      }
      return new Page<>(response.asList(InstallerDto.class), skipCount, maxItems, totalCount);
    } catch (BadRequestException e) {
      throw new IllegalArgumentException(e);
    } catch (IOException | ApiException e) {
      throw new InstallerException(e.getMessage(), e);
    }
  }

  @Override
  public List<Installer> getOrderedInstallers(List<String> installerKeys)
      throws InstallerException {
    checkConfiguration();

    try {
      return new ArrayList<>(
          requestFactory
              .fromUrl(
                  UriBuilder.fromUri(registryServiceUrl)
                      .path(InstallerRegistryService.class, "getOrderedInstallers")
                      .build()
                      .toString())
              .usePostMethod()
              .setBody(installerKeys)
              .request()
              .asList(InstallerDto.class));
    } catch (NotFoundException e) {
      throw new InstallerNotFoundException(e.getMessage(), e);
    } catch (BadRequestException e) {
      throw new IllegalInstallerKeyException(e.getMessage(), e);
    } catch (IOException | ApiException e) {
      throw new InstallerException(e.getMessage(), e);
    }
  }

  public boolean isConfigured() {
    return registryServiceUrl != null;
  }

  private void checkConfiguration() {
    if (!isConfigured()) {
      throw new IllegalStateException("Remote installer registry is not configured.");
    }
  }
}
