/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.client.service;

import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/**
 * Implementation for {@link MavenServerServiceClient}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class MavenServerServiceClientImpl implements MavenServerServiceClient {
  private final String servicePath;
  private final AppContext appContext;
  private final LoaderFactory loaderFactory;
  private final AsyncRequestFactory asyncRequestFactory;

  @Inject
  public MavenServerServiceClientImpl(
      AppContext appContext, LoaderFactory loaderFactory, AsyncRequestFactory asyncRequestFactory) {
    this.appContext = appContext;
    this.loaderFactory = loaderFactory;
    this.asyncRequestFactory = asyncRequestFactory;
    this.servicePath = "/maven/server/";
  }

  @Override
  public Promise<String> getEffectivePom(String projectPath) {
    final String url =
        appContext.getDevMachine().getWsAgentBaseUrl()
            + servicePath
            + "effective/pom?projectpath="
            + projectPath;

    return asyncRequestFactory
        .createGetRequest(url)
        .loader(loaderFactory.newLoader("Generating effective pom..."))
        .send(new StringUnmarshaller());
  }

  @Override
  public Promise<Boolean> downloadSources(String projectPath, String fqn) {
    final String url =
        appContext.getDevMachine().getWsAgentBaseUrl()
            + servicePath
            + "download/sources?projectpath="
            + projectPath
            + "&fqn="
            + fqn;
    return asyncRequestFactory
        .createGetRequest(url)
        .loader(loaderFactory.newLoader("Generating effective pom..."))
        .send(
            new Unmarshallable<Boolean>() {
              private boolean downloaded;

              @Override
              public void unmarshal(Response response) throws UnmarshallerException {
                downloaded = Boolean.valueOf(response.getText());
              }

              @Override
              public Boolean getPayload() {
                return downloaded;
              }
            });
  }

  @Override
  public Promise<Void> reImportProjects(@NotNull List<String> projectsPaths) {
    StringBuilder queryParameters = new StringBuilder();
    for (String path : projectsPaths) {
      queryParameters.append("&projectPath=").append(path);
    }
    final String url =
        appContext.getDevMachine().getWsAgentBaseUrl()
            + servicePath
            + "reimport"
            + queryParameters.toString().replaceFirst("&", "?");

    return asyncRequestFactory.createPostRequest(url, null).send();
  }

  @Override
  public Promise<Void> reconcilePom(String pomPath) {
    final String url =
        appContext.getDevMachine().getWsAgentBaseUrl()
            + servicePath
            + "pom/reconcile?pompath="
            + pomPath;
    return asyncRequestFactory.createGetRequest(url).send();
  }
}
