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
package org.eclipse.che.plugin.github.factory.resolver;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.Map;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.server.FactoryParametersResolver;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.plugin.urlfactory.ProjectConfigDtoMerger;
import org.eclipse.che.plugin.urlfactory.URLFactoryBuilder;

/**
 * Provides Factory Parameters resolver for github repositories.
 *
 * @author Florent Benoit
 */
public class GithubFactoryParametersResolver implements FactoryParametersResolver {

  /** Parameter name. */
  protected static final String URL_PARAMETER_NAME = "url";

  /** Parser which will allow to check validity of URLs and create objects. */
  @Inject private GithubURLParser githubUrlParser;

  /** Builder allowing to build objects from github URL. */
  @Inject private GithubSourceStorageBuilder githubSourceStorageBuilder;

  @Inject private URLFactoryBuilder urlFactoryBuilder;

  /** ProjectDtoMerger */
  @Inject private ProjectConfigDtoMerger projectConfigDtoMerger;

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
      throws BadRequestException {

    // no need to check null value of url parameter as accept() method has performed the check
    final GithubUrl githubUrl = githubUrlParser.parse(factoryParameters.get("url"));

    // create factory from the following location if location exists, else create default factory
    FactoryDto factory = urlFactoryBuilder.createFactory(githubUrl.factoryJsonFileLocation());

    // add workspace configuration if not defined
    if (factory.getWorkspace() == null) {
      factory.setWorkspace(
          urlFactoryBuilder.buildWorkspaceConfig(
              githubUrl.getRepository(), githubUrl.getUsername(), githubUrl.dockerFileLocation()));
    }

    // Compute project configuration
    ProjectConfigDto projectConfigDto =
        newDto(ProjectConfigDto.class)
            .withSource(githubSourceStorageBuilder.build(githubUrl))
            .withName(githubUrl.getRepository())
            .withType("blank")
            .withPath("/".concat(githubUrl.getRepository()));

    // apply merging operation from existing and computed settings
    return projectConfigDtoMerger.merge(factory, projectConfigDto);
  }
}
