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
package org.eclipse.che.ide.api.project;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static com.google.gwt.http.client.RequestBuilder.PUT;
import static com.google.gwt.http.client.URL.encodePathSegment;
import static com.google.gwt.http.client.URL.encodeQueryString;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;
import static org.eclipse.che.ide.util.PathEncoder.encodePath;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.ProjectSearchResponseDto;
import org.eclipse.che.api.project.shared.dto.SearchResultDto;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.NewProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.SearchItemReference;
import org.eclipse.che.ide.api.resources.SearchResult;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.rest.UrlBuilder;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/**
 * Implementation of {@link ProjectServiceClient}.
 *
 * <p>TODO need to remove interface as this component is internal one and couldn't have more than
 * one instance
 *
 * @author Vitaly Parfonov
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @see ProjectServiceClient
 */
public class ProjectServiceClientImpl implements ProjectServiceClient {

  private static final String PROJECT = "/project";
  private static final String BATCH_PROJECTS = "/batch";

  private static final String ITEM = "/item";
  private static final String TREE = "/tree";
  private static final String MOVE = "/move";
  private static final String COPY = "/copy";
  private static final String FOLDER = "/folder";
  private static final String FILE = "/file";
  private static final String SEARCH = "/search";
  private static final String IMPORT = "/import";
  private static final String RESOLVE = "/resolve";
  private static final String ESTIMATE = "/estimate";

  private final LoaderFactory loaderFactory;
  private final AsyncRequestFactory reqFactory;
  private final DtoFactory dtoFactory;
  private final DtoUnmarshallerFactory unmarshaller;
  private final AppContext appContext;

  @Inject
  protected ProjectServiceClientImpl(
      LoaderFactory loaderFactory,
      AsyncRequestFactory reqFactory,
      DtoFactory dtoFactory,
      DtoUnmarshallerFactory unmarshaller,
      AppContext appContext) {
    this.loaderFactory = loaderFactory;
    this.reqFactory = reqFactory;
    this.dtoFactory = dtoFactory;
    this.unmarshaller = unmarshaller;
    this.appContext = appContext;
  }

  /** {@inheritDoc} */
  @Override
  public Promise<List<ProjectConfigDto>> getProjects() {
    final String url = getBaseUrl();

    return reqFactory
        .createGetRequest(url)
        .header(ACCEPT, MimeType.APPLICATION_JSON)
        .send(unmarshaller.newListUnmarshaller(ProjectConfigDto.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<SourceEstimation> estimate(Path path, String pType) {
    final String url = getBaseUrl() + ESTIMATE + encodePath(path) + "?type=" + pType;

    return reqFactory
        .createGetRequest(url)
        .header(ACCEPT, MimeType.APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Estimating project..."))
        .send(unmarshaller.newUnmarshaller(SourceEstimation.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<List<SourceEstimation>> resolveSources(Path path) {
    final String url = getBaseUrl() + RESOLVE + encodePath(path);

    return reqFactory
        .createGetRequest(url)
        .header(ACCEPT, MimeType.APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Resolving sources..."))
        .send(unmarshaller.newListUnmarshaller(SourceEstimation.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<Void> importProject(Path path, SourceStorageDto source) {
    String url = getBaseUrl() + IMPORT + encodePath(path);

    return reqFactory.createPostRequest(url, source).header(CONTENT_TYPE, APPLICATION_JSON).send();
  }

  /** {@inheritDoc} */
  @Override
  public Promise<SearchResult> search(QueryExpression expression) {
    Path prjPath = isNullOrEmpty(expression.getPath()) ? Path.ROOT : new Path(expression.getPath());
    final String url = getBaseUrl() + SEARCH + encodePath(prjPath.addLeadingSeparator());

    StringBuilder queryParameters = new StringBuilder();
    if (expression.getName() != null && !expression.getName().isEmpty()) {
      queryParameters.append("&name=").append(encodeQueryString(expression.getName()));
    }
    if (expression.getText() != null && !expression.getText().isEmpty()) {
      queryParameters.append("&text=").append(encodeQueryString(expression.getText()));
    }
    if (expression.getMaxItems() == 0) {
      expression.setMaxItems(
          100); // for avoiding block client by huge response until search not support pagination
      // will limit result here
    }
    queryParameters.append("&maxItems=").append(expression.getMaxItems());
    if (expression.getSkipCount() != 0) {
      queryParameters.append("&skipCount=").append(expression.getSkipCount());
    }

    return reqFactory
        .createGetRequest(url + queryParameters.toString().replaceFirst("&", "?"))
        .header(ACCEPT, MimeType.APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Searching..."))
        .send(unmarshaller.newUnmarshaller(ProjectSearchResponseDto.class))
        .then(
            (Function<ProjectSearchResponseDto, SearchResult>)
                searchResultDto -> {
                  List<SearchResultDto> itemReferences = searchResultDto.getItemReferences();
                  if (itemReferences == null || itemReferences.isEmpty()) {
                    return new SearchResult(
                        Collections.emptyList(), searchResultDto.getTotalHits());
                  }
                  return new SearchResult(
                      itemReferences
                          .stream()
                          .map(SearchItemReference::new)
                          .collect(Collectors.toList()),
                      searchResultDto.getTotalHits());
                });
  }

  /** {@inheritDoc} */
  @Override
  public Promise<ProjectConfigDto> createProject(
      ProjectConfigDto configuration, Map<String, String> options) {
    UrlBuilder urlBuilder = new UrlBuilder(getBaseUrl());
    for (String key : options.keySet()) {
      urlBuilder.setParameter(key, options.get(key));
    }
    return reqFactory
        .createPostRequest(urlBuilder.buildString(), configuration)
        .header(ACCEPT, MimeType.APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Creating project..."))
        .send(unmarshaller.newUnmarshaller(ProjectConfigDto.class));
  }

  @Override
  public Promise<List<ProjectConfigDto>> createBatchProjects(
      List<NewProjectConfigDto> configurations) {
    final String url = getBaseUrl() + BATCH_PROJECTS;
    final String loaderMessage =
        configurations.size() > 1 ? "Creating the batch of projects..." : "Creating project...";
    return reqFactory
        .createPostRequest(url, configurations)
        .header(ACCEPT, MimeType.APPLICATION_JSON)
        .loader(loaderFactory.newLoader(loaderMessage))
        .send(unmarshaller.newListUnmarshaller(ProjectConfigDto.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<ItemReference> createFile(Path path, String content) {
    final String url =
        getBaseUrl()
            + FILE
            + encodePath(path.parent())
            + "?name="
            + encodePathSegment(path.lastSegment());

    return reqFactory
        .createPostRequest(url, null)
        .data(content)
        .loader(loaderFactory.newLoader("Creating file..."))
        .send(unmarshaller.newUnmarshaller(ItemReference.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<String> getFileContent(Path path) {
    final String url = getBaseUrl() + FILE + encodePath(path);

    return reqFactory.createGetRequest(url).send(new StringUnmarshaller());
  }

  /** {@inheritDoc} */
  @Override
  public Promise<Void> setFileContent(Path path, String content) {
    final String url = getBaseUrl() + FILE + encodePath(path);

    return reqFactory.createRequest(PUT, url, null, false).data(content).send();
  }

  /** {@inheritDoc} */
  @Override
  public Promise<ItemReference> createFolder(Path path) {
    final String url = getBaseUrl() + FOLDER + encodePath(path);

    return reqFactory
        .createPostRequest(url, null)
        .loader(loaderFactory.newLoader("Creating folder..."))
        .send(unmarshaller.newUnmarshaller(ItemReference.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<Void> deleteItem(Path path) {
    final String url = getBaseUrl() + encodePath(path);

    return reqFactory
        .createRequest(DELETE, url, null, false)
        .loader(loaderFactory.newLoader("Deleting resource..."))
        .send();
  }

  /** {@inheritDoc} */
  @Override
  public Promise<Void> copy(Path source, Path target, String newName, boolean overwrite) {
    final String url = getBaseUrl() + COPY + encodePath(source) + "?to=" + encodePath(target);

    final CopyOptions copyOptions = dtoFactory.createDto(CopyOptions.class);
    copyOptions.setName(newName);
    copyOptions.setOverWrite(overwrite);

    return reqFactory
        .createPostRequest(url, copyOptions)
        .loader(loaderFactory.newLoader("Copying..."))
        .send();
  }

  /** {@inheritDoc} */
  @Override
  public Promise<Void> move(Path source, Path target, String newName, boolean overwrite) {
    final String url = getBaseUrl() + MOVE + encodePath(source) + "?to=" + encodePath(target);

    final MoveOptions moveOptions = dtoFactory.createDto(MoveOptions.class);
    moveOptions.setName(newName);
    moveOptions.setOverWrite(overwrite);

    return reqFactory
        .createPostRequest(url, moveOptions)
        .loader(loaderFactory.newLoader("Moving..."))
        .send();
  }

  /** {@inheritDoc} */
  @Override
  public Promise<TreeElement> getTree(Path path, int depth, boolean includeFiles) {
    final String url =
        getBaseUrl()
            + TREE
            + encodePath(path.addLeadingSeparator())
            + "?depth="
            + depth
            + "&includeFiles="
            + includeFiles;

    // temporary workaround for CHE-3467, remove loader for disable UI blocking
    // later this loader should be added with the new mechanism of client-server synchronization

    return reqFactory
        .createGetRequest(url)
        .header(ACCEPT, MimeType.APPLICATION_JSON)
        .send(unmarshaller.newUnmarshaller(TreeElement.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<ItemReference> getItem(Path path) {
    final String url = getBaseUrl() + ITEM + encodePath(path);

    return reqFactory
        .createGetRequest(url)
        .header(ACCEPT, MimeType.APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting item..."))
        .send(unmarshaller.newUnmarshaller(ItemReference.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<ProjectConfigDto> getProject(Path path) {
    final String url = getBaseUrl() + encodePath(path);

    return reqFactory
        .createGetRequest(url)
        .header(ACCEPT, MimeType.APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting project..."))
        .send(unmarshaller.newUnmarshaller(ProjectConfigDto.class));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<ProjectConfigDto> updateProject(ProjectConfigDto configuration) {
    Path prjPath = new Path(configuration.getPath());
    final String url = getBaseUrl() + encodePath(prjPath.addLeadingSeparator());

    return reqFactory
        .createRequest(PUT, url, configuration, false)
        .header(CONTENT_TYPE, MimeType.APPLICATION_JSON)
        .header(ACCEPT, MimeType.APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Updating project..."))
        .send(unmarshaller.newUnmarshaller(ProjectConfigDto.class));
  }

  /**
   * Returns the base url for the project service. It consists of workspace agent base url plus
   * project prefix.
   *
   * @return base url for project service
   * @since 4.4.0
   */
  private String getBaseUrl() {
    return appContext.getDevMachine().getWsAgentBaseUrl() + PROJECT;
  }
}
