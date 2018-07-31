/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;

/** @author Musienko Maxim */
@Singleton
public class TestCommandServiceClient {
  private final String apiEndpoint;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public TestCommandServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider, HttpJsonRequestFactory requestFactory) {
    this.apiEndpoint = apiEndpointProvider.get().toString();
    this.requestFactory = requestFactory;
  }

  public void createCommand(String commandLine, String commandName, String commandType, String wsId)
      throws Exception {
    CommandDto commandDto = DtoFactory.newDto(CommandDto.class);
    commandDto.setName(commandName);
    commandDto.setType(commandType);
    commandDto.setCommandLine(commandLine);
    commandDto.setAttributes(ImmutableMap.of("previewUrl", ""));
    createCommand(commandDto, wsId);
  }

  public void createCommand(CommandDto command, String wsId) throws Exception {
    requestFactory
        .fromUrl(apiEndpoint + "workspace/" + wsId + "/command")
        .usePostMethod()
        .setBody(command)
        .request();
  }

  public void deleteCommand(String commandName, String wsId) throws Exception {
    requestFactory
        .fromUrl(apiEndpoint + "workspace/" + wsId + "/command/" + commandName)
        .useDeleteMethod()
        .request();
  }
}
