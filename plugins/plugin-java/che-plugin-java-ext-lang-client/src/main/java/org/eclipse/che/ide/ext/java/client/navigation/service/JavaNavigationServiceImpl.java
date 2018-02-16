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
package org.eclipse.che.ide.ext.java.client.navigation.service;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/** @author Evgen Vidolob */
@Singleton
public class JavaNavigationServiceImpl implements JavaNavigationService {

  private final AppContext appContext;
  private final LoaderFactory loaderFactory;
  private final AsyncRequestFactory requestFactory;
  private final DtoUnmarshallerFactory unmarshallerFactory;

  @Inject
  public JavaNavigationServiceImpl(
      AppContext appContext,
      LoaderFactory loaderFactory,
      DtoUnmarshallerFactory unmarshallerFactory,
      AsyncRequestFactory asyncRequestFactory) {
    this.appContext = appContext;
    this.loaderFactory = loaderFactory;
    this.requestFactory = asyncRequestFactory;
    this.unmarshallerFactory = unmarshallerFactory;
  }

  @Override
  public Promise<List<JavaProject>> getProjectsAndPackages(boolean includePackage) {
    final String url =
        appContext.getWsAgentServerApiEndpoint()
            + "/java/navigation/get/projects/and/packages"
            + "?includepackages="
            + includePackage;

    return requestFactory
        .createGetRequest(url)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader())
        .send(unmarshallerFactory.newListUnmarshaller(JavaProject.class));
  }
}
