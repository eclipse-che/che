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
package org.eclipse.che.plugin.java.plain.client.service;

import static org.eclipse.che.ide.resource.Path.valueOf;
import static org.eclipse.che.ide.util.PathEncoder.encodePath;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;

/**
 * The implementation of {@link ClasspathUpdaterServiceClient}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClasspathUpdaterServiceClientImpl implements ClasspathUpdaterServiceClient {

  private final String pathToService;
  private final MessageLoader loader;
  private final AsyncRequestFactory asyncRequestFactory;
  private final AppContext appContext;

  @Inject
  public ClasspathUpdaterServiceClientImpl(
      AsyncRequestFactory asyncRequestFactory, AppContext appContext, LoaderFactory loaderFactory) {
    this.asyncRequestFactory = asyncRequestFactory;
    this.appContext = appContext;
    this.loader = loaderFactory.newLoader();

    this.pathToService = "/jdt/classpath/";
  }

  @Override
  public Promise<Void> setRawClasspath(String projectPath, List<ClasspathEntry> entries) {
    final String url =
        appContext.getWsAgentServerApiEndpoint()
            + pathToService
            + "update?projectpath="
            + encodePath(valueOf(projectPath));
    return asyncRequestFactory.createPostRequest(url, entries).loader(loader).send();
  }
}
