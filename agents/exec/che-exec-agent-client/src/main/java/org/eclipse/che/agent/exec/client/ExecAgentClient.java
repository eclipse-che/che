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
package org.eclipse.che.agent.exec.client;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.inject.assistedinject.Assisted;
import java.io.IOException;
import javax.inject.Inject;
import org.eclipse.che.agent.exec.shared.dto.GetProcessResponseDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessKillResponseDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessStartRequestDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessStartResponseDto;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;

/**
 * Helps to interact with exec agent via REST.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class ExecAgentClient {

  private final HttpJsonRequestFactory requestFactory;
  private final String serverEndpoint;

  @Inject
  public ExecAgentClient(HttpJsonRequestFactory requestFactory, @Assisted String serverEndpoint) {
    this.requestFactory = requestFactory;
    this.serverEndpoint = serverEndpoint;
  }

  public ProcessStartResponseDto startProcess(String command, String name, String type) {
    ProcessStartRequestDto commandDto =
        newDto(ProcessStartRequestDto.class).withCommandLine(command).withName(name).withType(type);
    try {
      return requestFactory
          .fromUrl(serverEndpoint)
          .usePostMethod()
          .setBody(commandDto)
          .request()
          .asDto(ProcessStartResponseDto.class);
    } catch (ServerException
        | IOException
        | ConflictException
        | BadRequestException
        | UnauthorizedException
        | NotFoundException
        | ForbiddenException e) {
      e.printStackTrace();
    }
    return null;
  }

  public GetProcessResponseDto getProcess(int pid) {
    try {
      return requestFactory
          .fromUrl(serverEndpoint + "/" + pid)
          .useGetMethod()
          .request()
          .asDto(GetProcessResponseDto.class);
    } catch (ServerException
        | IOException
        | ConflictException
        | BadRequestException
        | UnauthorizedException
        | NotFoundException
        | ForbiddenException e) {
      e.printStackTrace();
    }
    return null;
  }

  public ProcessKillResponseDto killProcess(int pid) {
    try {
      return requestFactory
          .fromUrl(serverEndpoint + "/" + pid)
          .useDeleteMethod()
          .request()
          .asDto(ProcessKillResponseDto.class);
    } catch (ServerException
        | IOException
        | ConflictException
        | BadRequestException
        | UnauthorizedException
        | NotFoundException
        | ForbiddenException e) {
      e.printStackTrace();
    }
    return null;
  }
}
