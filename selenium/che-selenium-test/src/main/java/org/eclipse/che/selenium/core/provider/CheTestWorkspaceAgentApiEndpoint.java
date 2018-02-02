/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;

public class CheTestWorkspaceAgentApiEndpoint {

  @Inject private HttpJsonRequestFactory httpJsonRequestFactory;

  public String getWorkspaceAgentApiEndpoint(TestWorkspace workspace) throws Exception {
    HttpJsonResponse response =
        httpJsonRequestFactory
            .fromUrl("http://172.19.20.13:8080/api/workspace/" + workspace.getId())
            .useGetMethod()
            .request();

    return parseResponseAsJsonObject(response)
            .getAsJsonObject()
            .getAsJsonObject("runtime")
            .getAsJsonObject("machines")
            .getAsJsonObject("dev-machine")
            .getAsJsonObject("servers")
            .getAsJsonObject("wsagent/http")
            .get("url")
            .getAsString()
        + "/";
  }

  private JsonObject parseResponseAsJsonObject(HttpJsonResponse response) {
    return new JsonParser().parse(response.asString()).getAsJsonObject();
  }
}
