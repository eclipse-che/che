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

import static java.io.File.separator;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.fs.server.WsPathUtils.isRoot;
import static org.eclipse.che.api.fs.server.WsPathUtils.nameOf;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;
import static org.eclipse.che.api.project.server.impl.FileItemUtils.parseDir;
import static org.eclipse.che.api.project.server.impl.FileItemUtils.parseFile;
import static org.eclipse.che.api.project.server.impl.ProjectDtoConverter.asDto;
import static org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent.EventType.UPDATED;
import static org.eclipse.che.api.project.shared.Constants.CHE_DIR;
import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_PROGRESS;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.fileupload.FileItem;
import org.apache.tika.Tika;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.fs.server.FsDtoConverter;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.ProjectService;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.notification.ProjectDeletedEvent;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.ImportProgressRecordDto;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.NewProjectConfigDto;
import org.eclipse.che.api.project.shared.dto.ProjectSearchRequestDto;
import org.eclipse.che.api.project.shared.dto.ProjectSearchResponseDto;
import org.eclipse.che.api.project.shared.dto.SearchOccurrenceDto;
import org.eclipse.che.api.project.shared.dto.SearchResultDto;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.search.server.InvalidQueryException;
import org.eclipse.che.api.search.server.OffsetData;
import org.eclipse.che.api.search.server.QueryExecutionException;
import org.eclipse.che.api.search.server.QueryExpression;
import org.eclipse.che.api.search.server.SearchResult;
import org.eclipse.che.api.search.server.Searcher;
import org.eclipse.che.api.search.server.impl.SearchResultEntry;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Project service REST API back end. This class' methods are called from the {@link
 * ProjectService}.
 */
public class ProjectServiceApi {
  private static final Logger LOG = LoggerFactory.getLogger(ProjectServiceApi.class);

  private static Tika TIKA;

  private final ServiceContext serviceContext;

  private final ProjectManager projectManager;
  private final FsManager fsManager;
  private final FsDtoConverter fsDtoConverter;
  private final Searcher searcher;
  private final EventService eventService;
  private final ProjectServiceLinksInjector linksInjector;
  private final ProjectServiceVcsStatusInjector vcsStatusInjector;
  private final RequestTransmitter transmitter;

  @AssistedInject
  public ProjectServiceApi(
      @Assisted ServiceContext serviceContext,
      Searcher searcher,
      ProjectManager projectManager,
      FsManager fsManager,
      FsDtoConverter fsDtoConverter,
      EventService eventService,
      ProjectServiceLinksInjector linksInjector,
      ProjectServiceVcsStatusInjector vcsStatusInjector,
      RequestTransmitter transmitter) {
    this.serviceContext = serviceContext;
    this.projectManager = projectManager;
    this.fsManager = fsManager;
    this.fsDtoConverter = fsDtoConverter;
    this.searcher = searcher;
    this.eventService = eventService;
    this.linksInjector = linksInjector;
    this.vcsStatusInjector = vcsStatusInjector;
    this.transmitter = transmitter;
  }

  /** Get list of projects */
  public List<ProjectConfigDto> getProjects()
      throws IOException, ServerException, ConflictException, ForbiddenException {

    return projectManager
        .getAll()
        .stream()
        .map(ProjectDtoConverter::asDto)
        .map(this::injectProjectLinks)
        .collect(Collectors.toList());
  }

  /** Get project specified by the following workspace path */
  public ProjectConfigDto getProject(String wsPath)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {
    wsPath = absolutize(wsPath);

    return projectManager
        .get(wsPath)
        .map(ProjectDtoConverter::asDto)
        .map(this::injectProjectLinks)
        .orElseThrow(() -> new NotFoundException("Project is not found"));
  }

  /** Create project with specified project configuration */
  public ProjectConfigDto createProject(UriInfo uriInfo, ProjectConfigDto projectConfig)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException {

    Map<String, String> options =
        uriInfo
            .getQueryParameters()
            .entrySet()
            .stream()
            .collect(toMap(Entry::getKey, it -> it.getValue().get(0)));

    RegisteredProject project = projectManager.create(projectConfig, options);
    ProjectConfigDto asDto = asDto(project);
    ProjectConfigDto injectedLinks = injectProjectLinks(asDto);

    eventService.publish(new ProjectCreatedEvent(project.getPath()));

    return injectedLinks;
  }

  /** Create projects with specified configurations for a client with specified identifier */
  public List<ProjectConfigDto> createBatchProjects(
      List<NewProjectConfigDto> projectConfigs, boolean rewrite, String clientId)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException, IOException,
          UnauthorizedException, BadRequestException {

    projectManager.doImport(
        new HashSet<>(projectConfigs), rewrite, jsonRpcImportConsumer(clientId));

    Set<RegisteredProject> registeredProjects = new HashSet<>(projectConfigs.size());

    for (NewProjectConfigDto projectConfig : projectConfigs) {
      registeredProjects.add(projectManager.update(projectConfig));
    }

    Set<ProjectConfigDto> result =
        registeredProjects
            .stream()
            .map(ProjectDtoConverter::asDto)
            .map(this::injectProjectLinks)
            .collect(toSet());

    registeredProjects
        .stream()
        .map(RegisteredProject::getPath)
        .map(ProjectCreatedEvent::new)
        .forEach(eventService::publish);

    return new ArrayList<>(result);
  }

  /** Update project specified by workspace path with new configuration */
  public ProjectConfigDto updateProject(String wsPath, ProjectConfigDto projectConfigDto)
      throws NotFoundException, ConflictException, ForbiddenException, ServerException, IOException,
          BadRequestException {
    if (wsPath != null) {
      projectConfigDto.setPath(absolutize(wsPath));
    }

    RegisteredProject updated = projectManager.update(projectConfigDto);
    return asDto(updated);
  }

  /** Delete project with specified workspace path */
  public void delete(String wsPath)
      throws NotFoundException, ForbiddenException, ConflictException, ServerException {
    wsPath = absolutize(wsPath);

    if (projectManager.isRegistered(wsPath)) {
      projectManager
          .delete(wsPath)
          .map(RegisteredProject::getPath)
          .map(ProjectDeletedEvent::new)
          .ifPresent(eventService::publish);
    } else {
      fsManager.delete(wsPath);
    }
  }

  /** Analyze if project defined by specific workspace path is of specified type */
  public SourceEstimation estimateProject(String wsPath, String projectType)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {
    wsPath = absolutize(wsPath);

    ProjectTypeResolution resolution = projectManager.verify(wsPath, projectType);

    Map<String, List<String>> attributes =
        resolution
            .getProvidedAttributes()
            .entrySet()
            .stream()
            .collect(toMap(Entry::getKey, it -> it.getValue().getList()));

    return DtoFactory.newDto(SourceEstimation.class)
        .withType(projectType)
        .withMatched(resolution.matched())
        .withResolution(resolution.getResolution())
        .withAttributes(attributes);
  }

  /** Get list of types that corresponds to a project with specified workspace paths */
  public List<SourceEstimation> resolveSources(String wsPath)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {
    wsPath = absolutize(wsPath);

    return projectManager
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
                      .collect(toMap(Entry::getKey, it -> it.getValue().getList()));

              return newDto(SourceEstimation.class)
                  .withType(resolution.getType())
                  .withMatched(resolution.matched())
                  .withAttributes(attributes);
            })
        .collect(toList());
  }

  /** Import project from specified source storage to specified location */
  public void importProject(
      String wsPath, boolean force, String clientId, SourceStorageDto sourceStorage)
      throws ConflictException, ForbiddenException, UnauthorizedException, IOException,
          ServerException, NotFoundException, BadRequestException {

    wsPath = absolutize(wsPath);

    projectManager.doImport(wsPath, sourceStorage, force, jsonRpcImportConsumer(clientId));
  }

  /** Create file with specified path, name and content */
  public Response createFile(String parentWsPath, String fileName, InputStream content)
      throws NotFoundException, ConflictException, ForbiddenException, ServerException {
    parentWsPath = absolutize(parentWsPath);
    String wsPath = resolve(parentWsPath, fileName);

    fsManager.createFile(wsPath, content);
    String project =
        projectManager
            .getClosest(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find parent project for file"))
            .getPath();

    eventService.publish(
        new ProjectItemModifiedEvent(
            ProjectItemModifiedEvent.EventType.CREATED, project, wsPath, false));

    URI location =
        serviceContext
            .getBaseUriBuilder()
            .clone()
            .path(ProjectService.class)
            .path(ProjectService.class, "getFile")
            .build(new String[] {wsPath.substring(1)}, false);

    ItemReference asDto = fsDtoConverter.asDto(wsPath);
    ItemReference asDtoWithVcsStatus = vcsStatusInjector.injectVcsStatus(asDto);
    ItemReference asDtoWithVcsStatusAndLinks = injectFileLinks(asDtoWithVcsStatus);

    return Response.created(location).entity(asDtoWithVcsStatusAndLinks).build();
  }

  /** Create folder under specified path */
  public Response createFolder(String wsPath)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException {
    wsPath = absolutize(wsPath);
    fsManager.createDir(wsPath);

    final URI location =
        serviceContext
            .getBaseUriBuilder()
            .clone()
            .path(ProjectService.class)
            .path(ProjectService.class, "getChildren")
            .build(new String[] {wsPath.substring(1)}, false);

    String project =
        projectManager
            .getClosest(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find parent project"))
            .getPath();

    eventService.publish(
        new ProjectItemModifiedEvent(
            ProjectItemModifiedEvent.EventType.CREATED, project, wsPath, true));

    return Response.created(location)
        .entity(injectFolderLinks(fsDtoConverter.asDto(wsPath)))
        .build();
  }

  /** Upload file under specified path and form data */
  public Response uploadFile(String parentWsPath, Iterator<FileItem> formData)
      throws NotFoundException, ConflictException, ForbiddenException, ServerException {
    parentWsPath = absolutize(parentWsPath);

    FileItemParsed parsed = parseFile(formData);
    String wsPath = resolve(parentWsPath, parsed.getName());
    boolean overwrite = parsed.getOverwrite();
    InputStream content = parsed.getContent();

    fsManager.createFile(wsPath, content, overwrite, true);

    return Response.ok("", MediaType.TEXT_HTML).build();
  }

  /** Upload a folder from zip represented by form data to a specified location */
  public Response uploadFolderFromZip(String wsPath, Iterator<FileItem> formData)
      throws ServerException, ConflictException, ForbiddenException, NotFoundException {
    wsPath = absolutize(wsPath);

    FileItemParsed fileItemParsed = parseDir(formData);
    InputStream content = fileItemParsed.getContent();
    fsManager.unzip(wsPath, content, false);

    return Response.ok("", MediaType.TEXT_HTML).build();
  }

  /** Get file with specified location */
  public Response getFile(String wsPath)
      throws IOException, NotFoundException, ForbiddenException, ServerException,
          ConflictException {
    wsPath = absolutize(wsPath);

    InputStream inputStream = fsManager.read(wsPath);
    String name = wsPath.substring(wsPath.lastIndexOf(separator));
    String type = getTIKA().detect(name);

    return Response.ok().entity(inputStream).type(type).build();
  }

  /** Update file with specified location and content */
  public Response updateFile(String wsPath, InputStream content)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {
    wsPath = absolutize(wsPath);

    fsManager.update(wsPath, content);

    String project =
        projectManager
            .getClosest(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find parent project for file"))
            .getPath();

    eventService.publish(new ProjectItemModifiedEvent(UPDATED, project, wsPath, false));

    return Response.ok().build();
  }

  /** Copy file system item from specified source location to specified destination location */
  public Response copy(String wsPath, String newParentWsPath, CopyOptions copyOptions)
      throws NotFoundException, ForbiddenException, ConflictException, ServerException {
    String srcWsPath = absolutize(wsPath);
    newParentWsPath = absolutize(newParentWsPath);
    String name = getNameValue(copyOptions, wsPath);
    boolean overwrite = getOverwriteValue(copyOptions);

    String dstWsPath = resolve(newParentWsPath, name);

    if (projectManager.isRegistered(srcWsPath)) {
      projectManager.copy(srcWsPath, dstWsPath, overwrite);
    } else {
      fsManager.copy(srcWsPath, dstWsPath, overwrite, true);
    }

    String method = fsManager.existsAsFile(srcWsPath) ? "getFile" : "getChildren";

    URI location =
        serviceContext
            .getBaseUriBuilder()
            .clone()
            .path(ProjectService.class)
            .path(ProjectService.class, method)
            .build(new String[] {dstWsPath.substring(1)}, false);

    return Response.created(location).build();
  }

  private String getNameValue(CopyOptions copyOptions, String wsPath) {
    if (copyOptions != null && copyOptions.getName() != null) {
      return copyOptions.getName();
    } else {
      return nameOf(wsPath);
    }
  }

  private boolean getOverwriteValue(CopyOptions copyOptions) {
    if (copyOptions != null && copyOptions.getOverWrite() != null) {
      return copyOptions.getOverWrite();
    } else {
      return false;
    }
  }

  /** Move file system item from specified source location to specified destination location */
  public Response move(String wsPath, String newParentWsPath, MoveOptions moveOptions)
      throws NotFoundException, ForbiddenException, ConflictException, ServerException {
    wsPath = absolutize(wsPath);
    newParentWsPath = absolutize(newParentWsPath);
    String name = getNameValue(moveOptions, wsPath);
    boolean overwrite = getOverwriteValue(moveOptions);

    String dstWsPath = resolve(newParentWsPath, name);

    if (projectManager.isRegistered(wsPath)) {
      projectManager.move(wsPath, dstWsPath, overwrite);
    } else {
      fsManager.move(wsPath, dstWsPath, overwrite, true);
    }

    String method = fsManager.existsAsFile(wsPath) ? "getFile" : "getChildren";
    final URI location =
        serviceContext
            .getBaseUriBuilder()
            .clone()
            .path(ProjectService.class)
            .path(ProjectService.class, method)
            .build(new String[] {dstWsPath.substring(1)}, false);

    return Response.created(location).build();
  }

  private String getNameValue(MoveOptions moveOptions, String wsPath) {
    if (moveOptions != null && moveOptions.getName() != null) {
      return moveOptions.getName();
    } else {
      return nameOf(wsPath);
    }
  }

  private boolean getOverwriteValue(MoveOptions moveOptions) {
    if (moveOptions != null && moveOptions.getOverWrite() != null) {
      return moveOptions.getOverWrite();
    } else {
      return false;
    }
  }

  /** Upload file from zip represented as form data into specified location */
  public List<SourceEstimation> uploadProjectFromZip(
      String wsPath, boolean force, Iterator<FileItem> formData)
      throws ServerException, ConflictException, ForbiddenException, NotFoundException,
          BadRequestException {
    wsPath = absolutize(wsPath);

    FileItemParsed fileItemParsed = parseDir(formData);

    fsManager.unzip(wsPath, fileItemParsed.getContent(), false);

    return resolveSources(wsPath);
  }

  /** Import zipped data into specified location */
  public Response importZip(String wsPath, InputStream zip, Boolean skipFirstLevel)
      throws NotFoundException, ConflictException, ForbiddenException, ServerException {
    wsPath = absolutize(wsPath);

    fsManager.unzip(wsPath, zip, skipFirstLevel);

    return Response.created(
            serviceContext
                .getBaseUriBuilder()
                .clone()
                .path(ProjectService.class)
                .path(ProjectService.class, "getChildren")
                .build(new String[] {wsPath.substring(1)}, false))
        .build();
  }

  /** Zip content under specified location */
  public InputStream exportZip(String wsPath)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {
    wsPath = absolutize(wsPath);

    return fsManager.zip(wsPath);
  }

  public Response exportFile(String wsPath)
      throws NotFoundException, ForbiddenException, ServerException, ConflictException {
    wsPath = absolutize(wsPath);

    InputStream inputStream = fsManager.read(wsPath);
    long length = fsManager.length(wsPath);
    long lastModified = fsManager.lastModified(wsPath);
    String name = nameOf(wsPath);

    return Response.ok(inputStream, getTIKA().detect(name))
        .lastModified(new Date(lastModified))
        .header(HttpHeaders.CONTENT_LENGTH, Long.toString(length))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + '"')
        .build();
  }

  /** Get children list defined by specified location */
  public List<ItemReference> getChildren(String wsPath)
      throws NotFoundException, ForbiddenException, ServerException, IOException {
    wsPath = absolutize(wsPath);

    Set<String> wsPaths = applyTreeFilter(wsPath, fsManager.getAllChildrenWsPaths(wsPath));
    Set<ItemReference> itemReferences = fsDtoConverter.asDto(wsPaths);

    List<ItemReference> result =
        itemReferences
            .stream()
            .map(it -> "file".equals(it.getType()) ? injectFileLinks(it) : injectFolderLinks(it))
            .collect(Collectors.toList());

    return vcsStatusInjector.injectVcsStatus(result);
  }

  /** Get file system tree under specified location and depth */
  public TreeElement getTree(String wsPath, int depth, boolean includeFiles)
      throws NotFoundException, ForbiddenException, ServerException {
    wsPath = absolutize(wsPath);

    ItemReference asDto = fsDtoConverter.asDto(wsPath);
    ItemReference asLinkedDto =
        fsManager.isFile(wsPath)
            ? injectFileLinks(vcsStatusInjector.injectVcsStatus(asDto))
            : injectFolderLinks(asDto);
    return newDto(TreeElement.class)
        .withNode(asLinkedDto)
        .withChildren(getTreeRecursively(wsPath, depth, includeFiles));
  }

  /** Get file system item defined by specific location */
  public ItemReference getItem(String wsPath)
      throws NotFoundException, ForbiddenException, ServerException {
    wsPath = absolutize(wsPath);

    ItemReference asDto = fsDtoConverter.asDto(wsPath);
    return fsManager.isFile(wsPath)
        ? injectFileLinks(vcsStatusInjector.injectVcsStatus(asDto))
        : injectFolderLinks(asDto);
  }

  /**
   * Preform search with specified parameters
   *
   * @param wsPath search root
   * @param name file name
   * @param text text
   * @param maxItems maximum number of items
   * @param skipCount number of items to be skipped
   */
  public ProjectSearchResponseDto search(
      String wsPath, String name, String text, int maxItems, int skipCount)
      throws BadRequestException, ServerException, NotFoundException {
    if (skipCount < 0) {
      throw new BadRequestException(String.format("Invalid 'skipCount' parameter: %d.", skipCount));
    }
    wsPath = absolutize(wsPath);

    QueryExpression expr =
        new QueryExpression()
            .setPath(wsPath)
            .setName(name)
            .setText(text)
            .setMaxItems(maxItems)
            .setSkipCount(skipCount)
            .setIncludePositions(true);

    try {
      SearchResult result = searcher.search(expr);
      List<SearchResultEntry> searchResultEntries = result.getResults();
      return DtoFactory.newDto(ProjectSearchResponseDto.class)
          .withTotalHits(result.getTotalHits())
          .withItemReferences(prepareResults(searchResultEntries));
    } catch (InvalidQueryException e) {
      throw new BadRequestException(e.getMessage());
    } catch (QueryExecutionException e) {
      LOG.warn(e.getLocalizedMessage());
      throw new ServerException(e.getMessage());
    }
  }

  /**
   * Prepare result for client, add additional information like line number and line content where
   * found given text
   */
  private List<SearchResultDto> prepareResults(List<SearchResultEntry> searchResultEntries)
      throws NotFoundException {
    List<SearchResultDto> results = new ArrayList<>(searchResultEntries.size());
    for (SearchResultEntry searchResultEntry : searchResultEntries) {
      String path = searchResultEntry.getFilePath();
      if (fsManager.existsAsFile(path)) {
        ItemReference asDto = fsDtoConverter.asDto(path);
        ItemReference itemReference = injectFileLinks(asDto);
        List<OffsetData> datas = searchResultEntry.getData();
        List<SearchOccurrenceDto> searchOccurrences = new ArrayList<>(datas.size());
        for (OffsetData data : datas) {
          SearchOccurrenceDto searchOccurrenceDto =
              DtoFactory.getInstance()
                  .createDto(SearchOccurrenceDto.class)
                  .withPhrase(data.getPhrase())
                  .withScore(data.getScore())
                  .withStartOffset(data.getStartOffset())
                  .withEndOffset(data.getEndOffset())
                  .withLineNumber(data.getLineNum())
                  .withLineContent(data.getLine());
          searchOccurrences.add(searchOccurrenceDto);
        }
        SearchResultDto searchResultDto = DtoFactory.getInstance().createDto(SearchResultDto.class);
        results.add(
            searchResultDto
                .withItemReference(itemReference)
                .withSearchOccurrences(searchOccurrences));
      }
    }
    return results;
  }

  @Inject
  private void configureProjectSearchRequestHandler(
      RequestHandlerConfigurator requestHandlerConfigurator) {
    requestHandlerConfigurator
        .newConfiguration()
        .methodName("project/search")
        .paramsAsDto(ProjectSearchRequestDto.class)
        .resultAsDto(ProjectSearchResponseDto.class)
        .withFunction(this::search);
  }

  public ProjectSearchResponseDto search(ProjectSearchRequestDto request) {
    String path = request.getPath();
    String name = request.getName();
    String text = request.getText();
    int maxItems = request.getMaxItems();
    int skipCount = request.getSkipCount();

    try {
      return search(path, name, text, maxItems, skipCount);
    } catch (ServerException | NotFoundException | BadRequestException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private BiConsumer<String, String> jsonRpcImportConsumer(String clientId) {
    return (projectName, message) -> {
      ImportProgressRecordDto progressRecord =
          newDto(ImportProgressRecordDto.class).withProjectName(projectName).withLine(message);

      transmitter
          .newRequest()
          // TODO will be fixed after we start properly distinguish server side endpoints
          .endpointId(clientId + "<-:->ws-agent-websocket-endpoint")
          .methodName(EVENT_IMPORT_OUTPUT_PROGRESS)
          .paramsAsDto(progressRecord)
          .sendAndSkipResult();
    };
  }

  private List<TreeElement> getTreeRecursively(String wsPath, int depth, boolean includeFiles)
      throws ServerException, NotFoundException {
    if (depth == 0) {
      return null;
    }

    Set<String> childrenWsPaths =
        includeFiles
            ? applyTreeFilter(wsPath, fsManager.getAllChildrenWsPaths(wsPath))
            : applyTreeFilter(wsPath, fsManager.getDirWsPaths(wsPath));

    List<TreeElement> nodes = new ArrayList<>(childrenWsPaths.size());
    for (String childWsPath : childrenWsPaths) {
      ItemReference asDto = fsDtoConverter.asDto(childWsPath);
      ItemReference asLinkedDto =
          fsManager.isDir(childWsPath) ? injectFolderLinks(asDto) : injectFileLinks(asDto);
      TreeElement treeElement = newDto(TreeElement.class).withNode(asLinkedDto);
      nodes.add(treeElement);

      if (fsManager.isDir(childWsPath)) {
        List<TreeElement> treeElements = getTreeRecursively(childWsPath, depth - 1, includeFiles);
        if (treeElements != null) {
          treeElement.setChildren(treeElements);
        }
      }
    }

    return vcsStatusInjector.injectVcsStatusTreeElements(nodes);
  }

  private Set<String> applyTreeFilter(String parentWsPath, Set<String> childrenWsPaths) {
    if (!isRoot(parentWsPath)) {
      return childrenWsPaths;
    }

    String rootCheDir = absolutize(CHE_DIR);
    Set<String> copy = new HashSet<>(childrenWsPaths);
    copy.removeIf(rootCheDir::equals);
    return unmodifiableSet(copy);
  }

  private ItemReference injectFileLinks(ItemReference itemReference) {
    return linksInjector.injectFileLinks(itemReference, serviceContext);
  }

  private ItemReference injectFolderLinks(ItemReference itemReference) {
    return linksInjector.injectFolderLinks(itemReference, serviceContext);
  }

  private ProjectConfigDto injectProjectLinks(ProjectConfigDto projectConfig) {
    return linksInjector.injectProjectLinks(projectConfig, serviceContext);
  }

  /** Lazy init of Tika. */
  private synchronized Tika getTIKA() {
    if (TIKA == null) {
      TIKA = new Tika();
    }
    return TIKA;
  }
}
