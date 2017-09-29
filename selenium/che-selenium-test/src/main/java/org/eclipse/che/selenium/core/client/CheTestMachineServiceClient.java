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
package org.eclipse.che.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;

/** @author Musienko Maxim */
@Singleton
public class CheTestMachineServiceClient implements TestMachineServiceClient {

  private final String apiEndpoint;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public CheTestMachineServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider, HttpJsonRequestFactory requestFactory) {
    this.apiEndpoint = apiEndpointProvider.get().toString();
    this.requestFactory = requestFactory;
  }

  /**
   * Returns machine token for current workspace
   *
   * @param workspaceId the workspace id
   * @return the machine token for current workspace
   */
  @Override
  public String getMachineApiToken(String workspaceId) throws Exception {
    HttpJsonResponse response =
        requestFactory
            .fromUrl(apiEndpoint + "machine/token/" + workspaceId)
            .useGetMethod()
            .request();
    return JsonHelper.parseJson(response.asString()).getElement("machineToken").getStringValue();
  }
}
