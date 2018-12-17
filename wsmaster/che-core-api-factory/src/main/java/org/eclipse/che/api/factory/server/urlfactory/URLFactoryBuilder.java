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

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.devfile.server.DevfileFormatException;
import org.eclipse.che.api.devfile.server.DevfileManager;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Handle the creation of some elements used inside a {@link FactoryDto}.
 *
 * @author Florent Benoit
 */
@Singleton
public class URLFactoryBuilder {

  /** Default docker image (if repository has no dockerfile) */
  protected static final String DEFAULT_DOCKER_IMAGE = "eclipse/ubuntu_jdk8";

  /** Default docker type (if repository has no dockerfile) */
  protected static final String DEFAULT_MEMORY_LIMIT_BYTES = Long.toString(2000L * 1024L * 1024L);

  protected static final String MACHINE_NAME = "ws-machine";

  private final URLChecker urlChecker;
  private final URLFetcher urlFetcher;
  private final DevfileManager devfileManager;

  @Inject
  public URLFactoryBuilder(
      URLChecker urlChecker, URLFetcher urlFetcher, DevfileManager devfileManager) {
    this.urlChecker = urlChecker;
    this.urlFetcher = urlFetcher;
    this.devfileManager = devfileManager;
  }

  /**
   * Build a default factory using the provided json file or create default one
   *
   * @param jsonFileLocation location of factory json file
   * @return a factory or null if factory json in not found
   */
  public FactoryDto createFactoryFromJson(String jsonFileLocation) {
    // Check if there is factory json file inside the repository
    if (jsonFileLocation != null) {
      String factoryJsonContent = urlFetcher.fetch(jsonFileLocation);
      if (!Strings.isNullOrEmpty(factoryJsonContent)) {
        return DtoFactory.getInstance().createDtoFromJson(factoryJsonContent, FactoryDto.class);
      }
    }
    return null;
  }

  /**
   * Build a default factory using the provided devfile
   *
   * @param devfileLocation location of devfile
   * @return a factory or null if devfile is not found
   */
  public FactoryDto createFactoryFromDevfile(String devfileLocation) throws BadRequestException {
    if (devfileLocation != null) {
      String devfileYamlContent = urlFetcher.fetch(devfileLocation);
      if (!Strings.isNullOrEmpty(devfileYamlContent)) {
        try {
          WorkspaceConfigImpl wsConfig =
              devfileManager.validateAndConvert(devfileYamlContent, false);
          return newDto(FactoryDto.class)
              .withV(CURRENT_VERSION)
              .withWorkspace(DtoConverter.asDto(wsConfig));
        } catch (DevfileFormatException e) {
          throw new BadRequestException(e.getMessage());
        } catch (IOException x) {
          throw new IllegalStateException(x.getLocalizedMessage(), x);
        }
      }
    }
    return null;
  }

  /**
   * Help to generate default workspace configuration
   *
   * @param environmentName the name of the environment to create
   * @param name the name of the workspace
   * @param dockerFileLocation the optional location for codenvy dockerfile to use
   * @return a workspace configuration
   */
  public WorkspaceConfigDto buildWorkspaceConfig(
      String environmentName, String name, String dockerFileLocation) {

    // if remote repository contains a docker file, use it
    // else use the default image.
    RecipeDto recipeDto;
    if (dockerFileLocation != null && urlChecker.exists(dockerFileLocation)) {
      recipeDto =
          newDto(RecipeDto.class)
              .withLocation(dockerFileLocation)
              .withType("dockerfile")
              .withContentType("text/x-dockerfile");
    } else {
      recipeDto = newDto(RecipeDto.class).withContent(DEFAULT_DOCKER_IMAGE).withType("dockerimage");
    }
    MachineConfigDto machine =
        newDto(MachineConfigDto.class)
            .withInstallers(
                ImmutableList.of(
                    "org.eclipse.che.ws-agent", "org.eclipse.che.exec", "org.eclipse.che.terminal"))
            .withAttributes(singletonMap(MEMORY_LIMIT_ATTRIBUTE, DEFAULT_MEMORY_LIMIT_BYTES));

    // setup environment
    EnvironmentDto environmentDto =
        newDto(EnvironmentDto.class)
            .withRecipe(recipeDto)
            .withMachines(singletonMap(MACHINE_NAME, machine));

    // workspace configuration using the environment
    return newDto(WorkspaceConfigDto.class)
        .withDefaultEnv(environmentName)
        .withEnvironments(singletonMap(environmentName, environmentDto))
        .withName(name);
  }
}
