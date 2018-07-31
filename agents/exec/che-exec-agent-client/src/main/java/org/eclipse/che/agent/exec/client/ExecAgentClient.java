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
package org.eclipse.che.agent.exec.client;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.inject.assistedinject.Assisted;
import java.io.IOException;
import javax.inject.Inject;
import org.eclipse.che.agent.exec.shared.dto.GetProcessResponseDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessKillResponseDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessStartRequestDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessStartResponseDto;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.workspace.server.token.MachineTokenException;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;

/**
 * Helps to interact with exec agent via REST.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class ExecAgentClient {

  private final HttpJsonRequestFactory requestFactory;
  private final MachineTokenProvider machineTokenProvider;
  private final String serverEndpoint;

  @Inject
  public ExecAgentClient(
      HttpJsonRequestFactory requestFactory,
      MachineTokenProvider machineTokenProvider,
      @Assisted String serverEndpoint) {
    this.requestFactory = requestFactory;
    this.machineTokenProvider = machineTokenProvider;
    this.serverEndpoint = serverEndpoint;
  }

  /**
   * Starts a process within a given command.
   *
   * @param workspaceId workspace to run command
   * @param command command to execute
   * @param name command name
   * @param type command type
   * @return start process response DTO
   * @throws ServerException when submit of the process is failed
   */
  public ProcessStartResponseDto startProcess(
      String workspaceId, String command, String name, String type) throws ServerException {
    ProcessStartRequestDto commandDto =
        newDto(ProcessStartRequestDto.class).withCommandLine(command).withName(name).withType(type);
    try {
      return requestFactory
          .fromUrl(serverEndpoint)
          .addQueryParam("token", machineTokenProvider.getToken(workspaceId))
          .setAuthorizationHeader("none") // to prevent sending KC token
          .usePostMethod()
          .setBody(commandDto)
          .request()
          .asDto(ProcessStartResponseDto.class);
    } catch (IOException | ApiException | MachineTokenException e) {
      throw new ServerException(e);
    }
  }

  /**
   * Gets information about started process.
   *
   * @param workspaceId workspace to get process
   * @param pid pid of started process
   * @return process response DTO
   * @throws ServerException when get of the process is failed
   */
  public GetProcessResponseDto getProcess(String workspaceId, int pid) throws ServerException {
    try {
      return requestFactory
          .fromUrl(serverEndpoint + "/" + pid)
          .addQueryParam("token", machineTokenProvider.getToken(workspaceId))
          .setAuthorizationHeader("none") // to prevent sending KC token
          .useGetMethod()
          .request()
          .asDto(GetProcessResponseDto.class);
    } catch (IOException | ApiException | MachineTokenException e) {
      throw new ServerException(e);
    }
  }

  /**
   * Kills started process.
   *
   * @param workspaceId workspace kill process
   * @param pid pid of started process
   * @return kill process response DTO
   * @throws ServerException when kill of the process is failed
   */
  public ProcessKillResponseDto killProcess(String workspaceId, int pid) throws ServerException {
    try {
      return requestFactory
          .fromUrl(serverEndpoint + "/" + pid)
          .useDeleteMethod()
          .addQueryParam("token", machineTokenProvider.getToken(workspaceId))
          .setAuthorizationHeader("none") // to prevent sending KC token
          .request()
          .asDto(ProcessKillResponseDto.class);
    } catch (IOException | ApiException | MachineTokenException e) {
      throw new ServerException(e);
    }
  }
}
