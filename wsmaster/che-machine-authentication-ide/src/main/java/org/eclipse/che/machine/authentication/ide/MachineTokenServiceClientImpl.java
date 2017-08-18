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
package org.eclipse.che.machine.authentication.ide;

import com.google.inject.Inject;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.machine.authentication.shared.dto.MachineTokenDto;

/**
 * Implementation for {@link MachineTokenServiceClient}.
 *
 * @author Anton Korneta
 */
public class MachineTokenServiceClientImpl implements MachineTokenServiceClient {
  private static final String MACHINE_TOKEN_SERVICE_PATH = "/machine/token/";

  private final AppContext appContext;
  private final AsyncRequestFactory asyncRequestFactory;
  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
  private final String baseUrl;

  @Inject
  public MachineTokenServiceClientImpl(
      @RestContext String restContext,
      AppContext appContext,
      AsyncRequestFactory asyncRequestFactory,
      DtoUnmarshallerFactory dtoUnmarshallerFactory) {
    this.appContext = appContext;
    this.asyncRequestFactory = asyncRequestFactory;
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    this.baseUrl = restContext + MACHINE_TOKEN_SERVICE_PATH;
  }

  public Promise<MachineTokenDto> getMachineToken() {
    return asyncRequestFactory
        .createGetRequest(baseUrl + appContext.getWorkspaceId())
        .send(dtoUnmarshallerFactory.newUnmarshaller(MachineTokenDto.class));
  }

  @Override
  public Promise<UserDto> getUserByToken(String token) {
    return asyncRequestFactory
        .createGetRequest(baseUrl + "user/" + token)
        .send(dtoUnmarshallerFactory.newUnmarshaller(UserDto.class));
  }
}
