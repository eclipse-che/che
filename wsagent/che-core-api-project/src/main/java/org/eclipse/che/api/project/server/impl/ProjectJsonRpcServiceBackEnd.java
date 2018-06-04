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
package org.eclipse.che.api.project.server.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.project.server.impl.ProjectDtoConverter.asDto;
import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_PROGRESS;
import static org.eclipse.che.api.project.shared.Constants.Services.BAD_REQUEST;
import static org.eclipse.che.api.project.shared.Constants.Services.CONFLICT;
import static org.eclipse.che.api.project.shared.Constants.Services.FORBIDDEN;
import static org.eclipse.che.api.project.shared.Constants.Services.NOT_FOUND;
import static org.eclipse.che.api.project.shared.Constants.Services.SERVER_ERROR;
import static org.eclipse.che.api.project.shared.Constants.Services.UNAUTHORIZED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.project.shared.dto.ImportProgressRecordDto;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.service.CreateRequestDto;
import org.eclipse.che.api.project.shared.dto.service.CreateResponseDto;
import org.eclipse.che.api.project.shared.dto.service.DeleteRequestDto;
import org.eclipse.che.api.project.shared.dto.service.DeleteResponseDto;
import org.eclipse.che.api.project.shared.dto.service.GetRequestDto;
import org.eclipse.che.api.project.shared.dto.service.GetResponseDto;
import org.eclipse.che.api.project.shared.dto.service.ImportRequestDto;
import org.eclipse.che.api.project.shared.dto.service.ImportResponseDto;
import org.eclipse.che.api.project.shared.dto.service.RecognizeRequestDto;
import org.eclipse.che.api.project.shared.dto.service.RecognizeResponseDto;
import org.eclipse.che.api.project.shared.dto.service.UpdateRequestDto;
import org.eclipse.che.api.project.shared.dto.service.UpdateResponseDto;
import org.eclipse.che.api.project.shared.dto.service.VerifyRequestDto;
import org.eclipse.che.api.project.shared.dto.service.VerifyResponseDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;

@Singleton
public class ProjectJsonRpcServiceBackEnd {

  private final ProjectManager projectManager;
  private final RequestTransmitter requestTransmitter;

  @Inject
  public ProjectJsonRpcServiceBackEnd(
      ProjectManager projectManager, RequestTransmitter requestTransmitter) {
    this.projectManager = projectManager;
    this.requestTransmitter = requestTransmitter;
  }

  public GetResponseDto get(GetRequestDto getRequestDto) {
    return perform(this::getInternally, getRequestDto);
  }

  public CreateResponseDto create(CreateRequestDto createRequestDto) {
    return perform(this::createInternally, createRequestDto);
  }

  public UpdateResponseDto update(UpdateRequestDto updateRequest) {
    return perform(this::updateInternally, updateRequest);
  }

  public DeleteResponseDto delete(DeleteRequestDto deleteRequestDto) {
    return perform(this::deleteInternally, deleteRequestDto);
  }

  public RecognizeResponseDto recognize(RecognizeRequestDto recognizeRequestDto) {
    return perform(this::recognizeInternally, recognizeRequestDto);
  }

  public VerifyResponseDto verify(VerifyRequestDto verifyRequestDto) {
    return perform(this::verifyInternally, verifyRequestDto);
  }

  public ImportResponseDto doImport(String endpointId, ImportRequestDto importRequestDto) {
    return perform(this::doImportInternally, endpointId, importRequestDto);
  }

  private GetResponseDto getInternally(GetRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException {
    String wsPath = request.getWsPath();

    ProjectConfig registeredProject =
        projectManager
            .get(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find project: " + wsPath));

    GetResponseDto response = newDto(GetResponseDto.class);
    ProjectConfigDto projectConfigDto = asDto(registeredProject);
    response.setConfig(projectConfigDto);

    return response;
  }

  private CreateResponseDto createInternally(CreateRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException {
    String wsPath = request.getWsPath();
    ProjectConfigDto projectConfig = request.getConfig();
    Map<String, String> options = request.getOptions();

    ProjectConfig registeredProject = projectManager.create(projectConfig, options);
    ProjectConfigDto projectConfigDto = asDto(registeredProject);
    CreateResponseDto response = newDto(CreateResponseDto.class);
    response.setConfig(projectConfigDto);

    return response;
  }

  private UpdateResponseDto updateInternally(UpdateRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException {
    String wsPath = request.getWsPath();
    ProjectConfigDto config = request.getConfig();
    Map<String, String> options = request.getOptions();

    RegisteredProject registeredProject = projectManager.update(config);
    ProjectConfigDto projectConfigDto = asDto(registeredProject);
    UpdateResponseDto response = newDto(UpdateResponseDto.class);
    response.setConfig(projectConfigDto);

    return response;
  }

  private DeleteResponseDto deleteInternally(DeleteRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException {
    String wsPath = request.getWsPath();

    RegisteredProject registeredProject =
        projectManager
            .delete(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find project: " + wsPath));

    ProjectConfigDto projectConfigDto = asDto(registeredProject);
    DeleteResponseDto response = newDto(DeleteResponseDto.class);
    response.setConfig(projectConfigDto);

    return response;
  }

  private RecognizeResponseDto recognizeInternally(RecognizeRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException {

    String projectWsPath = request.getWsPath();

    List<SourceEstimation> mutableSourceEstimations =
        projectManager
            .recognize(projectWsPath)
            .stream()
            .filter(ProjectTypeResolution::matched)
            .map(
                resolution -> {
                  Map<String, List<String>> attributes =
                      resolution
                          .getProvidedAttributes()
                          .entrySet()
                          .stream()
                          .collect(toMap(Entry::getKey, it -> it.getValue().getList()));

                  return newDto(SourceEstimation.class)
                      .withType(resolution.getType())
                      .withMatched(resolution.matched())
                      .withAttributes(attributes);
                })
            .collect(toList());
    List<SourceEstimation> immutableSourceEstimations =
        Collections.unmodifiableList(mutableSourceEstimations);

    RecognizeResponseDto response = newDto(RecognizeResponseDto.class);
    response.setSourceEstimations(immutableSourceEstimations);

    return response;
  }

  private VerifyResponseDto verifyInternally(VerifyRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException {
    String wsPath = request.getWsPath();
    String type = request.getType();

    ProjectTypeResolution resolution = projectManager.verify(wsPath, type);

    Map<String, List<String>> attributes =
        resolution
            .getProvidedAttributes()
            .entrySet()
            .stream()
            .collect(toMap(Entry::getKey, it -> it.getValue().getList()));

    SourceEstimation sourceEstimation =
        newDto(SourceEstimation.class)
            .withType(type)
            .withMatched(resolution.matched())
            .withResolution(resolution.getResolution())
            .withAttributes(attributes);

    VerifyResponseDto response = newDto(VerifyResponseDto.class);
    response.setSourceEstimation(sourceEstimation);

    return response;
  }

  private ImportResponseDto doImportInternally(String endpointId, ImportRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException, UnauthorizedException {
    String wsPath = request.getWsPath();
    SourceStorageDto sourceStorage = request.getSourceStorage();

    BiConsumer<String, String> consumer =
        (projectName, message) -> {
          ImportProgressRecordDto progressRecord =
              newDto(ImportProgressRecordDto.class).withProjectName(projectName).withLine(message);

          requestTransmitter
              .newRequest()
              .endpointId(endpointId)
              .methodName(EVENT_IMPORT_OUTPUT_PROGRESS)
              .paramsAsDto(progressRecord)
              .sendAndSkipResult();
        };

    RegisteredProject registeredProject =
        projectManager.doImport(wsPath, sourceStorage, false, consumer);
    ProjectConfigDto projectConfigDto = asDto(registeredProject);
    ImportResponseDto response = newDto(ImportResponseDto.class);
    response.setConfig(projectConfigDto);

    return response;
  }

  // TODO temporary solution, further need to make a generalized exception mapper for all json rpc
  // service
  private <Request, Response> Response perform(
      Function<Request, Response> function, Request request) {
    try {
      return function.apply(request);
    } catch (ServerException e) {
      throw new JsonRpcException(SERVER_ERROR, e.getMessage());
    } catch (ConflictException e) {
      throw new JsonRpcException(CONFLICT, e.getMessage());
    } catch (ForbiddenException e) {
      throw new JsonRpcException(FORBIDDEN, e.getMessage());
    } catch (BadRequestException e) {
      throw new JsonRpcException(BAD_REQUEST, e.getMessage());
    } catch (NotFoundException e) {
      throw new JsonRpcException(NOT_FOUND, e.getMessage());
    } catch (UnauthorizedException e) {
      throw new JsonRpcException(UNAUTHORIZED, e.getMessage());
    }
  }

  private <Request, Response> Response perform(
      BiFunction<Request, Response> function, String endpointId, Request request) {
    try {
      return function.apply(endpointId, request);
    } catch (ServerException e) {
      throw new JsonRpcException(SERVER_ERROR, e.getMessage());
    } catch (ConflictException e) {
      throw new JsonRpcException(CONFLICT, e.getMessage());
    } catch (ForbiddenException e) {
      throw new JsonRpcException(FORBIDDEN, e.getMessage());
    } catch (BadRequestException e) {
      throw new JsonRpcException(BAD_REQUEST, e.getMessage());
    } catch (NotFoundException e) {
      throw new JsonRpcException(NOT_FOUND, e.getMessage());
    } catch (UnauthorizedException e) {
      throw new JsonRpcException(UNAUTHORIZED, e.getMessage());
    }
  }

  private interface Function<Request, Response> {
    Response apply(Request t)
        throws ServerException, ConflictException, ForbiddenException, BadRequestException,
            NotFoundException, UnauthorizedException;
  }

  private interface BiFunction<Request, Response> {
    Response apply(String s, Request t)
        throws ServerException, ConflictException, ForbiddenException, BadRequestException,
            NotFoundException, UnauthorizedException;
  }
}
