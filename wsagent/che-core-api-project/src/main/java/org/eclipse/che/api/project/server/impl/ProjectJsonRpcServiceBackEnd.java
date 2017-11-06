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
package org.eclipse.che.api.project.server.impl;

import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.project.server.impl.ProjectDtoConverter.asDto;
import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_PROGRESS;
import static org.eclipse.che.api.project.shared.Constants.Services.*;
import static org.eclipse.che.api.project.shared.Constants.WS_PATH_STRICT;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.*;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.dto.ImportProgressRecordDto;
import org.eclipse.che.api.project.shared.dto.ProjectMatcherDto;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.service.*;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.dto.server.DtoFactory;

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
    ProjectMatcherDto projectMatcherDto = request.getProjectMatcher();
    String type = projectMatcherDto.getType();

    if (!WS_PATH_STRICT.equals(type)) {
      throw new BadRequestException(
          "Currently only project strict workspace path matcher is supported");
    }

    String matcher = projectMatcherDto.getMatcher();

    List<ProjectConfigDto> mutableProjectSet =
        projectManager
            .getAll()
            .stream()
            .filter(it -> it.getPath().equals(matcher))
            .map(ProjectDtoConverter::asDto)
            .collect(Collectors.toList());
    List<ProjectConfigDto> immutableProjectSet = unmodifiableList(mutableProjectSet);

    GetResponseDto response = newDto(GetResponseDto.class);
    response.setProjectConfigs(immutableProjectSet);

    return response;
  }

  private CreateResponseDto createInternally(CreateRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException {
    List<ProjectConfigDto> projectConfigs = request.getProjectConfigs();
    Map<String, String> options =
        request.getOptions() == null ? Collections.emptyMap() : request.getOptions();

    List<ProjectConfigDto> mutableProjectConfigs = new LinkedList<>();
    for (ProjectConfig projectConfig : projectConfigs) {
      ProjectConfig registeredProject = projectManager.create(projectConfig, options);
      mutableProjectConfigs.add(asDto(registeredProject));
    }
    List<ProjectConfigDto> immutableProjectConfigs = unmodifiableList(mutableProjectConfigs);

    CreateResponseDto response = newDto(CreateResponseDto.class);
    response.setProjectConfigs(immutableProjectConfigs);

    return response;
  }

  private UpdateResponseDto updateInternally(UpdateRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException {
    List<ProjectConfigDto> projectConfigs = request.getProjectConfigs();

    List<ProjectConfigDto> mutableProjectConfigs = new LinkedList<>();
    for (ProjectConfigDto projectConfig : projectConfigs) {
      ProjectConfig updatedProjectConfig = projectManager.update(projectConfig);
      mutableProjectConfigs.add(asDto(updatedProjectConfig));
    }
    List<ProjectConfigDto> immutableProjectConfigs = unmodifiableList(mutableProjectConfigs);

    UpdateResponseDto response = newDto(UpdateResponseDto.class);
    response.setProjectConfigs(immutableProjectConfigs);

    return response;
  }

  private DeleteResponseDto deleteInternally(DeleteRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException {
    List<ProjectMatcherDto> projectMatcherDtos = request.getProjectMatchers();

    for (ProjectMatcherDto projectMatcherDto : projectMatcherDtos) {
      String type = projectMatcherDto.getType();
      if (!WS_PATH_STRICT.equals(type)) {
        throw new BadRequestException(
            "Currently only project strict workspace path matcher is supported");
      }
    }

    Set<String> wsPaths =
        projectMatcherDtos.stream().map(ProjectMatcherDto::getMatcher).collect(Collectors.toSet());
    List<ProjectConfigDto> registeredProjects =
        projectManager
            .deleteAll(wsPaths)
            .stream()
            .map(ProjectDtoConverter::asDto)
            .collect(toList());

    List<ProjectConfigDto> immutableDeletedProjects =
        unmodifiableList(new ArrayList<>(registeredProjects));

    DeleteResponseDto response = newDto(DeleteResponseDto.class);
    response.setProjectConfigs(immutableDeletedProjects);

    return response;
  }

  private RecognizeResponseDto recognizeInternally(RecognizeRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException {
    List<ProjectMatcherDto> projectMatcherDtos = request.getProjectMatchers();

    List<List<SourceEstimation>> estimations = new LinkedList<>();
    for (ProjectMatcherDto projectMatcherDto : projectMatcherDtos) {
      String type = projectMatcherDto.getType();
      if (!WS_PATH_STRICT.equals(type)) {
        throw new BadRequestException(
            "Currently only project strict workspace path matcher is supported");
      }

      String wsPath = projectMatcherDto.getMatcher();

      List<SourceEstimation> sourceEstimations =
          projectManager
              .recognize(wsPath)
              .stream()
              .filter(ProjectTypeResolution::matched)
              .map(
                  resolution -> {
                    Map<String, List<String>> attributes =
                        resolution
                            .getProvidedAttributes()
                            .entrySet()
                            .stream()
                            .collect(toMap(Map.Entry::getKey, it -> it.getValue().getList()));

                    return newDto(SourceEstimation.class)
                        .withType(resolution.getType())
                        .withMatched(resolution.matched())
                        .withAttributes(attributes);
                  })
              .collect(toList());

      estimations.add(sourceEstimations);
    }

    RecognizeResponseDto response = newDto(RecognizeResponseDto.class);
    response.setSourceEstimations(estimations);

    return response;
  }

  private VerifyResponseDto verifyInternally(VerifyRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException {
    List<ProjectMatcherDto> projectMatcherDtos = request.getProjectMatchers();
    List<String> projectTypes = request.getProjectTypes();

    List<SourceEstimation> mutableEstimations = new LinkedList<>();
    for (int i = 0; i < projectMatcherDtos.size(); i++) {
      ProjectMatcherDto projectMatcherDto = projectMatcherDtos.get(i);
      String type = projectMatcherDto.getType();
      if (!WS_PATH_STRICT.equals(type)) {
        throw new BadRequestException(
            "Currently only project strict workspace path matcher is supported");
      }
      String wsPath = projectMatcherDto.getMatcher();

      String projectType = projectTypes.get(i);

      ProjectTypeResolution resolution = projectManager.verify(wsPath, projectType);

      Map<String, List<String>> attributes =
          resolution
              .getProvidedAttributes()
              .entrySet()
              .stream()
              .collect(toMap(Map.Entry::getKey, it -> it.getValue().getList()));

      SourceEstimation sourceEstimation =
          DtoFactory.newDto(SourceEstimation.class)
              .withType(projectType)
              .withMatched(resolution.matched())
              .withResolution(resolution.getResolution())
              .withAttributes(attributes);

      mutableEstimations.add(sourceEstimation);
    }
    List<SourceEstimation> immutableEstimations = unmodifiableList(mutableEstimations);

    VerifyResponseDto response = newDto(VerifyResponseDto.class);
    response.setSourceEstimations(immutableEstimations);

    return response;
  }

  private ImportResponseDto doImportInternally(String endpointId, ImportRequestDto request)
      throws ServerException, ConflictException, ForbiddenException, BadRequestException,
          NotFoundException, UnauthorizedException {

    List<ProjectMatcherDto> projectMatcherDtos = request.getProjectMatchers();
    List<SourceStorageDto> sourceStorages = request.getSourceStorages();

    List<ProjectConfigDto> mutableProjectConfigs = new LinkedList<>();
    for (int i = 0; i < projectMatcherDtos.size(); i++) {
      ProjectMatcherDto projectMatcherDto = projectMatcherDtos.get(i);
      String type = projectMatcherDto.getType();

      if (!WS_PATH_STRICT.equals(type)) {
        throw new BadRequestException(
            "Currently only project strict workspace path matcher is supported");
      }

      String wsPath = projectMatcherDto.getMatcher();
      SourceStorage sourceStorage = sourceStorages.get(i);
      Map<String, SourceStorage> projectLocations = singletonMap(wsPath, sourceStorage);
      List<ProjectConfigDto> registeredProjects =
          projectManager
              .doImport(
                  projectLocations,
                  false,
                  (projectName, message) -> {
                    ImportProgressRecordDto progressRecord =
                        newDto(ImportProgressRecordDto.class)
                            .withProjectName(projectName)
                            .withLine(message);

                    requestTransmitter
                        .newRequest()
                        .endpointId(endpointId)
                        .methodName(EVENT_IMPORT_OUTPUT_PROGRESS)
                        .paramsAsDto(progressRecord)
                        .sendAndSkipResult();
                  })
              .stream()
              .map(ProjectDtoConverter::asDto)
              .collect(toList());
      mutableProjectConfigs.addAll(registeredProjects);
    }
    List<ProjectConfigDto> immutableProjectConfigs = unmodifiableList(mutableProjectConfigs);

    ImportResponseDto response = newDto(ImportResponseDto.class);
    response.setProjectConfigs(immutableProjectConfigs);

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
