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
package org.eclipse.che.multiuser.machine.authentication.server;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.agent.server.WsAgentHealthCheckerImpl;
import org.eclipse.che.api.agent.server.WsAgentPingRequestFactory;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.multiuser.machine.authentication.shared.dto.MachineTokenDto;

/** @author Max Shaposhnik (mshaposhnik@redhat.com) */
@Singleton
public class AuthWsAgentHealthChecker extends WsAgentHealthCheckerImpl {

  private final HttpJsonRequestFactory httpJsonRequestFactory;
  private final String apiEndpoint;

  @Inject
  public AuthWsAgentHealthChecker(
      WsAgentPingRequestFactory pingRequestFactory,
      HttpJsonRequestFactory httpJsonRequestFactory,
      @Named("che.api") String apiEndpoint) {
    super(pingRequestFactory);
    this.apiEndpoint = apiEndpoint;
    this.httpJsonRequestFactory = httpJsonRequestFactory;
  }

  // modifies the ping request if it is possible to get the machine token.
  protected HttpJsonRequest createPingRequest(Machine devMachine) throws ServerException {
    final HttpJsonRequest pingRequest = super.createPingRequest(devMachine);
    final String tokenServiceUrl =
        UriBuilder.fromUri(apiEndpoint)
            .replacePath("api/machine/token/" + devMachine.getWorkspaceId())
            .build()
            .toString();
    String machineToken = null;
    try {
      machineToken =
          httpJsonRequestFactory
              .fromUrl(tokenServiceUrl)
              .setMethod(HttpMethod.GET)
              .request()
              .asDto(MachineTokenDto.class)
              .getMachineToken();
    } catch (ApiException | IOException ex) {
      LOG.warn("Failed to get machine token", ex);
    }
    return machineToken == null ? pingRequest : pingRequest.setAuthorizationHeader(machineToken);
  }
}
