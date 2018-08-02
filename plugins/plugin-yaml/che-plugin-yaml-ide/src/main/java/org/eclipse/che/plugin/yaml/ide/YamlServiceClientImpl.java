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
package org.eclipse.che.plugin.yaml.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.plugin.yaml.shared.YamlDTO;

/**
 * The yaml client service for injecting the schemas into yaml language server
 *
 * @author Joshua Pinkney
 */
@Singleton
public class YamlServiceClientImpl implements YamlServiceClient {

  private final LoaderFactory loaderFactory;
  private final AsyncRequestFactory asyncRequestFactory;
  private final AsyncRequestLoader loader;
  private final AppContext appContext;
  private final DtoFactory dtoFactory;

  @Inject
  public YamlServiceClientImpl(
      LoaderFactory loaderFactory,
      AsyncRequestFactory asyncRequestFactory,
      AppContext appContext,
      DtoFactory dtoFactory) {
    this.loaderFactory = loaderFactory;
    this.asyncRequestFactory = asyncRequestFactory;
    this.loader = loaderFactory.newLoader();
    this.appContext = appContext;
    this.dtoFactory = dtoFactory;
  }

  /**
   * Service for sending the schemas to the yaml language server
   *
   * @param schemas The schemas you want to send to the yaml language server
   * @return Promise<Void> of the request
   */
  @Override
  public Promise<Void> putSchemas(Map<String, String> schemas) {
    YamlDTO schemaAddition = dtoFactory.createDto(YamlDTO.class).withSchemas(schemas);
    String schemasLocation = getWsAgentBaseUrl() + "/yaml/schemas";
    return asyncRequestFactory
        .createPostRequest(schemasLocation, schemaAddition)
        .loader(loader)
        .send();
  }

  private String getWsAgentBaseUrl() {
    return appContext.getWsAgentServerApiEndpoint();
  }
}
