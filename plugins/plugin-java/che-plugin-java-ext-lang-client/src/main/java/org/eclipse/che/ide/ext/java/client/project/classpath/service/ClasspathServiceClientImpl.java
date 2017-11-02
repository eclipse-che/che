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
package org.eclipse.che.ide.ext.java.client.project.classpath.service;

import static org.eclipse.che.ide.resource.Path.valueOf;
import static org.eclipse.che.ide.util.PathEncoder.encodePath;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;

/**
 * The implementation of {@link ClasspathServiceClient}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ClasspathServiceClientImpl implements ClasspathServiceClient {

  private final String pathToService;
  private final MessageLoader loader;
  private final AsyncRequestFactory asyncRequestFactory;
  private final DtoUnmarshallerFactory unmarshallerFactory;
  private final AppContext appContext;

  @Inject
  public ClasspathServiceClientImpl(
      AsyncRequestFactory asyncRequestFactory,
      DtoUnmarshallerFactory unmarshallerFactory,
      AppContext appContext,
      LoaderFactory loaderFactory) {
    this.asyncRequestFactory = asyncRequestFactory;
    this.unmarshallerFactory = unmarshallerFactory;
    this.appContext = appContext;
    this.loader = loaderFactory.newLoader();

    this.pathToService = "/java/classpath";
  }

  @Override
  public Promise<List<ClasspathEntryDto>> getClasspath(String projectPath) {
    final String url =
        appContext.getWsAgentServerApiEndpoint()
            + pathToService
            + "?projectpath="
            + encodePath(valueOf(projectPath));

    return asyncRequestFactory
        .createGetRequest(url)
        .loader(loader)
        .send(unmarshallerFactory.newListUnmarshaller(ClasspathEntryDto.class));
  }
}
