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
package org.eclipse.che.api.factory.server.github;

import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.api.factory.shared.Constants.URL_PARAMETER_NAME;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.server.DefaultFactoryParameterResolver;
import org.eclipse.che.api.factory.server.urlfactory.ProjectConfigDtoMerger;
import org.eclipse.che.api.factory.server.urlfactory.URLFactoryBuilder;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.ProjectDto;

/**
 * Provides Factory Parameters resolver for github repositories.
 *
 * @author Florent Benoit
 */
@Singleton
public class GithubFactoryParametersResolver extends DefaultFactoryParameterResolver {

  /** Parser which will allow to check validity of URLs and create objects. */
  private GithubURLParser githubUrlParser;

  /** Builder allowing to build objects from github URL. */
  private GithubSourceStorageBuilder githubSourceStorageBuilder;

  /** ProjectDtoMerger */
  @Inject private ProjectConfigDtoMerger projectConfigDtoMerger;

  @Inject
  public GithubFactoryParametersResolver(
      GithubURLParser githubUrlParser,
      URLFetcher urlFetcher,
      GithubSourceStorageBuilder githubSourceStorageBuilder,
      URLFactoryBuilder urlFactoryBuilder,
      ProjectConfigDtoMerger projectConfigDtoMerger) {
    super(urlFactoryBuilder, urlFetcher);
    this.githubUrlParser = githubUrlParser;
    this.githubSourceStorageBuilder = githubSourceStorageBuilder;
    this.projectConfigDtoMerger = projectConfigDtoMerger;
  }

  /**
   * Check if this resolver can be used with the given parameters.
   *
   * @param factoryParameters map of parameters dedicated to factories
   * @return true if it will be accepted by the resolver implementation or false if it is not
   *     accepted
   */
  @Override
  public boolean accept(@NotNull final Map<String, String> factoryParameters) {
    // Check if url parameter is a github URL
    return factoryParameters.containsKey(URL_PARAMETER_NAME)
        && githubUrlParser.isValid(factoryParameters.get(URL_PARAMETER_NAME));
  }

  /**
   * Create factory object based on provided parameters
   *
   * @param factoryParameters map containing factory data parameters provided through URL
   * @throws BadRequestException when data are invalid
   */
  @Override
  public FactoryDto createFactory(@NotNull final Map<String, String> factoryParameters)
      throws BadRequestException, ServerException {

    // no need to check null value of url parameter as accept() method has performed the check
    final GithubUrl githubUrl = githubUrlParser.parse(factoryParameters.get(URL_PARAMETER_NAME));

    // create factory from the following location if location exists, else create default factory
    FactoryDto factory =
        urlFactoryBuilder
            .createFactoryFromDevfile(
                githubUrl,
                fileName -> urlFetcher.fetch(githubUrl.rawFileLocation(fileName)),
                extractOverrideParams(factoryParameters))
            .orElseGet(
                () ->
                    urlFactoryBuilder
                        .createFactoryFromJson(githubUrl)
                        .orElseGet(
                            () ->
                                newDto(FactoryDto.class)
                                    .withV(CURRENT_VERSION)
                                    .withSource("repo")));

    if (factory.getWorkspace() != null) {
      return projectConfigDtoMerger.merge(
          factory,
          () -> {
            // Compute project configuration
            return newDto(ProjectConfigDto.class)
                .withSource(githubSourceStorageBuilder.buildWorkspaceConfigSource(githubUrl))
                .withName(githubUrl.getRepository())
                .withPath("/".concat(githubUrl.getRepository()));
          });
    } else if (factory.getDevfile() == null) {
      // initialize default devfile and github project
      factory.setDevfile(urlFactoryBuilder.buildDefaultDevfile(githubUrl.getRepository()));
      factory
          .getDevfile()
          .setProjects(
              Collections.singletonList(
                  newDto(ProjectDto.class)
                      .withSource(githubSourceStorageBuilder.buildDevfileSource(githubUrl))
                      .withName(githubUrl.getRepository())));
    }
    return factory;
  }
}
