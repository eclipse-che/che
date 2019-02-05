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
package org.eclipse.che.api.factory.server.urlfactory;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.server.DevfileException;
import org.eclipse.che.api.devfile.server.DevfileManager;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Handle the creation of some elements used inside a {@link FactoryDto}.
 *
 * @author Florent Benoit
 * @author Max Shaposhnyk
 */
@Singleton
public class URLFactoryBuilder {

  private final String defaultCheEditor;
  private final String defaultChePlugins;

  private final URLFetcher urlFetcher;
  private final DevfileManager devfileManager;

  @Inject
  public URLFactoryBuilder(
      @Named("che.factory.default_editor") String defaultCheEditor,
      @Named("che.factory.default_plugins") String defaultChePlugins,
      URLFetcher urlFetcher,
      DevfileManager devfileManager) {
    this.defaultCheEditor = defaultCheEditor;
    this.defaultChePlugins = defaultChePlugins;
    this.urlFetcher = urlFetcher;
    this.devfileManager = devfileManager;
  }

  /**
   * Build a factory using the provided json file or create default one
   *
   * @param jsonFileLocation location of factory json file
   * @return a factory or null if factory json in not found
   */
  public Optional<FactoryDto> createFactoryFromJson(String jsonFileLocation) {
    // Check if there is factory json file inside the repository
    if (jsonFileLocation != null) {
      final String factoryJsonContent = urlFetcher.fetchSafely(jsonFileLocation);
      if (!isNullOrEmpty(factoryJsonContent)) {
        return Optional.of(
            DtoFactory.getInstance().createDtoFromJson(factoryJsonContent, FactoryDto.class));
      }
    }
    return Optional.empty();
  }

  /**
   * Build a factory using the provided devfile
   *
   * @param devfileLocation location of devfile
   * @param fileUrlProvider optional service-specific provider of URL's to the file raw content
   * @return a factory or null if devfile is not found
   */
  public Optional<FactoryDto> createFactoryFromDevfile(
      String devfileLocation, @Nullable Function<String, String> fileUrlProvider)
      throws BadRequestException, ServerException {
    if (devfileLocation == null) {
      return Optional.empty();
    }
    final String devfileYamlContent = urlFetcher.fetchSafely(devfileLocation);
    if (isNullOrEmpty(devfileYamlContent)) {
      return Optional.empty();
    }
    try {
      Devfile devfile = devfileManager.parse(devfileYamlContent, false);
      WorkspaceConfigImpl wsConfig =
          devfileManager.createWorkspaceConfig(
              devfile,
              filename ->
                  fileUrlProvider != null
                      ? urlFetcher.fetch(fileUrlProvider.apply(filename))
                      : null);
      return Optional.of(
          newDto(FactoryDto.class)
              .withV(CURRENT_VERSION)
              .withWorkspace(DtoConverter.asDto(wsConfig)));
    } catch (DevfileException e) {
      throw new BadRequestException(e.getMessage());
    } catch (IOException x) {
      throw new ServerException(x.getLocalizedMessage(), x);
    }
  }

  /**
   * Help to generate default workspace configuration
   *
   * @param name the name of the workspace
   * @return a workspace configuration
   */
  public WorkspaceConfigDto buildDefaultWorkspaceConfig(String name) {

    Map<String, String> attributes = new HashMap<>();
    attributes.put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, defaultCheEditor);
    attributes.put(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, defaultChePlugins);

    // workspace configuration using the environment
    return newDto(WorkspaceConfigDto.class).withName(name).withAttributes(attributes);
  }
}
