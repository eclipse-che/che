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
package org.eclipse.che.api.factory.server.bitbucket;

import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.api.factory.shared.Constants.URL_PARAMETER_NAME;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.server.DefaultFactoryParameterResolver;
import org.eclipse.che.api.factory.server.urlfactory.URLFactoryBuilder;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.shared.dto.devfile.ProjectDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.SourceDto;

/**
 * Provides Factory Parameters resolver for bitbucket repositories.
 *
 * @author Max Shaposhnyk
 */
@Singleton
public class BitbucketServerFactoryParametersResolver extends DefaultFactoryParameterResolver {

  /** Parser which will allow to check validity of URLs and create objects. */
  private final BitbucketURLParser bitbucketURLParser;

  @Inject
  public BitbucketServerFactoryParametersResolver(
      URLFactoryBuilder urlFactoryBuilder,
      URLFetcher urlFetcher,
      BitbucketURLParser bitbucketURLParser) {
    super(urlFactoryBuilder, urlFetcher);
    this.bitbucketURLParser = bitbucketURLParser;
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
    return factoryParameters.containsKey(URL_PARAMETER_NAME)
        && bitbucketURLParser.isValid(factoryParameters.get(URL_PARAMETER_NAME));
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
    final BitbucketUrl bitbucketUrl =
        bitbucketURLParser.parse(factoryParameters.get(URL_PARAMETER_NAME));

    // create factory from the following location if location exists, else create default factory
    FactoryDto factory =
        urlFactoryBuilder
            .createFactoryFromDevfile(
                bitbucketUrl,
                fileName -> urlFetcher.fetch(bitbucketUrl.rawFileLocation(fileName)),
                extractOverrideParams(factoryParameters))
            .orElseGet(() -> newDto(FactoryDto.class).withV(CURRENT_VERSION).withSource("repo"));

    if (factory.getDevfile() == null) {
      // initialize default devfile
      factory.setDevfile(urlFactoryBuilder.buildDefaultDevfile(bitbucketUrl.getRepository()));
    }

    List<ProjectDto> projects = factory.getDevfile().getProjects();
    // if no projects set, set the default one from Bitbucket url
    if (projects.isEmpty()) {
      factory
          .getDevfile()
          .setProjects(
              Collections.singletonList(
                  newDto(ProjectDto.class)
                      .withSource(
                          newDto(SourceDto.class)
                              .withLocation(bitbucketUrl.repositoryLocation())
                              .withType("git")
                              .withBranch(bitbucketUrl.getBranch()))
                      .withName(bitbucketUrl.getRepository())));
    } else {
      // update existing project with same repository, set current branch if needed
      projects.forEach(
          project -> {
            final String location = project.getSource().getLocation();
            if (location.equals(bitbucketUrl.repositoryLocation())) {
              project.getSource().setBranch(bitbucketUrl.getBranch());
            }
          });
    }
    return factory;
  }
}
