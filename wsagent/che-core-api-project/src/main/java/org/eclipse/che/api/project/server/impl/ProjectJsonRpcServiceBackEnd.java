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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.project.shared.dto.ImportProgressRecordDto;
import org.eclipse.che.api.project.shared.dto.NewProjectConfigDto;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.service.*;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;

@Singleton
public class ProjectJsonRpcServiceBackEnd {

  private final ProjectManager projectManager;
  private final RequestTransmitter requestTransmitter;
  private final EventService eventService;

  @Inject
  public ProjectJsonRpcServiceBackEnd(
      ProjectManager projectManager,
      RequestTransmitter requestTransmitter,
      EventService eventService) {
    this.projectManager = projectManager;
    this.requestTransmitter = requestTransmitter;
    this.eventService = eventService;
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

  public List<ProjectConfigDto> createBatchProjects(
      String endpointId, CreateBatchProjectsRequestDto createProjectsRequest) {
    return perform(this::createBatchProjectsInternally, endpointId, createProjectsRequest);
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
      throws ServerException, ConflictException, ForbiddenException, NotFoundException,
          UnauthorizedException {
    String wsPath = request.getWsPath();
    SourceStorageDto sourceStorage = request.getSourceStorage();

    RegisteredProject registeredProject =
        projectManager.doImport(wsPath, sourceStorage, false, getImportConsumer(endpointId));
    eventService.publish(new ProjectCreatedEvent(wsPath));
    ProjectConfigDto projectConfigDto = asDto(registeredProject);
    ImportResponseDto response = newDto(ImportResponseDto.class);
    response.setConfig(projectConfigDto);

    return response;
  }

  private List<ProjectConfigDto> createBatchProjectsInternally(
      String endpointId, CreateBatchProjectsRequestDto createProjectsRequest)
      throws ForbiddenException, BadRequestException, ConflictException, NotFoundException,
          ServerException, UnauthorizedException {

    List<NewProjectConfigDto> newProjectConfigs = createProjectsRequest.getNewProjectConfigs();
    projectManager.doImport(
        new HashSet<>(newProjectConfigs),
        createProjectsRequest.isRewrite(),
        getImportConsumer(endpointId));

    Set<RegisteredProject> registeredProjects = new HashSet<>(newProjectConfigs.size());

    for (NewProjectConfigDto projectConfig : newProjectConfigs) {
      registeredProjects.add(projectManager.update(projectConfig));
    }

    registeredProjects
        .stream()
        .map(RegisteredProject::getPath)
        .map(ProjectCreatedEvent::new)
        .forEach(eventService::publish);

    return registeredProjects.stream().map(ProjectDtoConverter::asDto).collect(toList());
  }

  private BiConsumer<String, String> getImportConsumer(String endpointId) {
    return (projectName, message) -> {
      ImportProgressRecordDto progressRecord =
          newDto(ImportProgressRecordDto.class).withProjectName(projectName).withLine(message);

      requestTransmitter
          .newRequest()
          .endpointId(endpointId)
          .methodName(EVENT_IMPORT_OUTPUT_PROGRESS)
          .paramsAsDto(progressRecord)
          .sendAndSkipResult();
    };
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
