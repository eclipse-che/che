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
package org.eclipse.che.api.factory.server;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.factory.shared.Constants.URL_PARAMETER_NAME;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.server.urlfactory.DefaultFactoryUrl;
import org.eclipse.che.api.factory.server.urlfactory.URLFactoryBuilder;
import org.eclipse.che.api.factory.shared.dto.FactoryMetaDto;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.URLFileContentProvider;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.ProjectDto;

/**
 * Default {@link FactoryParametersResolver} implementation. Tries to resolve factory based on
 * provided parameters. Presumes url parameter as direct URL to a devfile content. Extracts and
 * applies devfile values override parameters.
 */
@Singleton
public class DefaultFactoryParameterResolver implements FactoryParametersResolver {

  private static final String OVERRIDE_PREFIX = "override.";

  protected final URLFactoryBuilder urlFactoryBuilder;
  protected final URLFetcher urlFetcher;

  @Inject
  public DefaultFactoryParameterResolver(
      URLFactoryBuilder urlFactoryBuilder, URLFetcher urlFetcher) {
    this.urlFactoryBuilder = urlFactoryBuilder;
    this.urlFetcher = urlFetcher;
  }

  @Override
  public boolean accept(Map<String, String> factoryParameters) {
    String url = factoryParameters.get(URL_PARAMETER_NAME);
    return url != null && !url.isEmpty();
  }

  /**
   * Creates factory based on provided parameters. Presumes url parameter as direct URL to a devfile
   * content.
   *
   * @param factoryParameters map containing factory data parameters provided through URL
   */
  @Override
  public FactoryMetaDto createFactory(@NotNull final Map<String, String> factoryParameters)
      throws BadRequestException, ServerException {
    // This should never be null, because our contract in #accept prohibits that
    String devfileLocation = factoryParameters.get(URL_PARAMETER_NAME);

    URI devfileURI;
    try {
      devfileURI = new URL(devfileLocation).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new BadRequestException(
          format(
              "Unable to process provided factory URL. Please check its validity and try again. Parser message: %s",
              e.getMessage()));
    }
    return urlFactoryBuilder
        .createFactoryFromDevfile(
            new DefaultFactoryUrl().withDevfileFileLocation(devfileLocation),
            new URLFileContentProvider(devfileURI, urlFetcher),
            extractOverrideParams(factoryParameters))
        .orElse(null);
  }

  /**
   * Finds and returns devfile override parameters in general factory parameters map.
   *
   * @param factoryParameters map containing factory data parameters provided through URL
   * @return filtered devfile values override map
   */
  protected Map<String, String> extractOverrideParams(Map<String, String> factoryParameters) {
    return factoryParameters
        .entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(OVERRIDE_PREFIX))
        .collect(toMap(e -> e.getKey().substring(OVERRIDE_PREFIX.length()), Entry::getValue));
  }

  /**
   * If devfile has no projects, put there one provided by given `projectSupplier`. Otherwise update
   * all projects with given `projectModifier`.
   *
   * @param devfile of the projects to update
   * @param projectSupplier provides default project
   * @param projectModifier updates existing projects
   */
  protected void updateProjects(
      DevfileDto devfile,
      Supplier<ProjectDto> projectSupplier,
      Consumer<ProjectDto> projectModifier) {
    List<ProjectDto> projects = devfile.getProjects();
    if (projects.isEmpty()) {
      devfile.setProjects(Collections.singletonList(projectSupplier.get()));
    } else {
      // update existing project with same repository, set current branch if needed
      projects.forEach(projectModifier);
    }
  }
}
