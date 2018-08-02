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
package org.eclipse.che.api.workspace.server.spi.environment;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;

/**
 * Handle how recipe is retrieved, either by downloading it with external location or by using the
 * provided content.
 *
 * @author Alexander Garagatyi
 */
public class RecipeRetriever {
  private static final Logger LOG = getLogger(RecipeRetriever.class);

  private final URI apiEndpoint;

  @Inject
  public RecipeRetriever(@Named("che.api") URI apiEndpoint) {
    this.apiEndpoint = apiEndpoint;
  }

  /**
   * Returns content of recipe either by getting it from provided {@link Recipe} object or by
   * retrieving it from location set in provided recipe.
   *
   * @param recipe recipe of {@link org.eclipse.che.api.core.model.workspace.config.Environment}
   * @return recipe content
   * @throws NullPointerException when recipe is null
   * @throws IllegalArgumentException when both recipe content and location are null or empty
   */
  public InternalRecipe getRecipe(Recipe recipe) throws InfrastructureException {
    Objects.requireNonNull(recipe, "Recipe should not be null");
    Objects.requireNonNull(recipe.getType(), "Recipe type should not be null");
    return new InternalRecipe(recipe.getType(), recipe.getContentType(), retrieveContent(recipe));
  }

  private String retrieveContent(Recipe recipe) throws InfrastructureException {
    if (recipe.getContent() != null && !recipe.getContent().isEmpty()) {
      // no downloading is needed
      return recipe.getContent();
    }
    if (recipe.getLocation() == null || recipe.getLocation().isEmpty()) {
      throw new IllegalArgumentException("Neither content nor location are present in recipe");
    }
    URL url = prepareURL(recipe.getLocation());
    return downloadContent(url);
  }

  private URL prepareURL(String location) throws InfrastructureException {
    URI uri;
    try {
      uri = new URI(location);
    } catch (URISyntaxException e) {
      LOG.debug(e.getLocalizedMessage(), e);
      throw new InfrastructureException(
          "Location of recipe downloading is not supported because it is not a valid URI");
    }
    // if URI to this server add token to access protected API
    boolean addToken = isTokenNeeded(uri);
    UriBuilder uriBuilder = makeURIAbsolute(uri);
    if (addToken) {
      addToken(uriBuilder);
    }
    try {
      return uriBuilder.build().toURL();
    } catch (MalformedURLException e) {
      LOG.debug(e.getLocalizedMessage(), e);
      throw new InfrastructureException("Constructing URL for downloading recipe failed");
    }
  }

  private String downloadContent(URL url) throws InfrastructureException {
    File file = null;
    try {
      file = IoUtil.downloadFileWithRedirect(null, "recipe", null, url);
      return IoUtil.readAndCloseQuietly(new FileInputStream(file));
    } catch (IOException e) {
      LOG.debug(e.getLocalizedMessage(), e);
      throw new InfrastructureException("Failed to download recipe content");
    } finally {
      if (file != null && !file.delete()) {
        FileCleaner.addFile(file);
      }
    }
  }

  private UriBuilder makeURIAbsolute(URI uri) {
    UriBuilder uriBuilder = UriBuilder.fromUri(uri);
    if (!uri.isAbsolute() && uri.getHost() == null) {
      uriBuilder
          .scheme(apiEndpoint.getScheme())
          .host(apiEndpoint.getHost())
          .port(apiEndpoint.getPort())
          .replacePath(apiEndpoint.getPath() + uri.toString());
    }
    return uriBuilder;
  }

  private boolean isTokenNeeded(URI uri) {
    // relative URI or host is the same as CHE API host - token is needed
    return (!uri.isAbsolute() && uri.getHost() == null)
        || (apiEndpoint.getHost().equals(uri.getHost()) && apiEndpoint.getPort() == uri.getPort());
  }

  private void addToken(UriBuilder ub) {
    if (EnvironmentContext.getCurrent().getSubject().getToken() != null) {
      ub.queryParam("token", EnvironmentContext.getCurrent().getSubject().getToken());
    }
  }
}
