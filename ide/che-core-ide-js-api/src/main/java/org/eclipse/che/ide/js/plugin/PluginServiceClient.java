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

package org.eclipse.che.ide.js.plugin;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringListUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/** @author Yevhen Vydolob */
@Singleton
public class PluginServiceClient {

  private final String servicePath;
  private final AsyncRequestFactory asyncRequestFactory;
  private final LoaderFactory loaderFactory;

  @Inject
  public PluginServiceClient(
      AppContext appContext, AsyncRequestFactory asyncRequestFactory, LoaderFactory loaderFactory) {
    servicePath = appContext.getMasterApiEndpoint() + "/plugin";
    this.asyncRequestFactory = asyncRequestFactory;
    this.loaderFactory = loaderFactory;
  }

  public Promise<List<String>> getPlugins() {
    return asyncRequestFactory
        .createGetRequest(servicePath)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting Plugins..."))
        .send(new StringListUnmarshaller());
  }
}
