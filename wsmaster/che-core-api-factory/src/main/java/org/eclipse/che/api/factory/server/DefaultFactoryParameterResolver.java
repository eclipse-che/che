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

import static org.eclipse.che.api.factory.shared.Constants.URL_PARAMETER_NAME;

import java.net.URI;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.devfile.server.URLFetcher;
import org.eclipse.che.api.devfile.server.URLFileContentProvider;
import org.eclipse.che.api.factory.server.urlfactory.DefaultFactoryUrl;
import org.eclipse.che.api.factory.server.urlfactory.URLFactoryBuilder;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;

/**
 * Default {@link FactoryParametersResolver} implementation. Tries to resolve factory based on
 * provided parameters. Presumes url parameters as direct URL to a devfile content.
 */
@Singleton
public class DefaultFactoryParameterResolver implements FactoryParametersResolver {

  private URLFactoryBuilder urlFactoryBuilder;
  private final URLFetcher urlFetcher;

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
  public FactoryDto createFactory(@NotNull final Map<String, String> factoryParameters)
      throws BadRequestException, ServerException {
    // This should never be null, because our contract in #accept prohibits that
    String devfileLocation = factoryParameters.get(URL_PARAMETER_NAME);

    return urlFactoryBuilder
        .createFactoryFromDevfile(
            new DefaultFactoryUrl()
                .withDevfileFileLocation(devfileLocation)
                .withDevfileFilename(null),
            new URLFileContentProvider(URI.create(devfileLocation), urlFetcher))
        .orElse(null);
  }
}
