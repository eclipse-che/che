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
package org.eclipse.che.ide.project;

import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static com.google.gwt.http.client.RequestBuilder.PUT;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.shared.dto.CopyOptions;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.project.shared.dto.MoveOptions;
import org.eclipse.che.api.project.shared.dto.NewProjectConfigDto;
import org.eclipse.che.api.project.shared.dto.ProjectSearchResponseDto;
import org.eclipse.che.api.project.shared.dto.SourceEstimation;
import org.eclipse.che.api.project.shared.dto.TreeElement;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.QueryExpression;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Unit test for {@link ProjectServiceClient}.
 *
 * @author Vlad Zhukovskyi
 * @author Oleksandr Andriienko
 */
@RunWith(GwtMockitoTestRunner.class)
public class ProjectServiceClientTest {

  private static final String TEXT = "to be or not to be.";
  private static final Path resourcePath =
      Path.valueOf("TestPrj/http%253A%252F%252F.org%252Fte st ");
  private static final Path targetPath = Path.valueOf("TestPrj/target here* ");

  @Mock private LoaderFactory loaderFactory;
  @Mock private AsyncRequestFactory requestFactory;
  @Mock private DtoFactory dtoFactory;
  @Mock private DtoUnmarshallerFactory unmarshaller;
  @Mock private AppContext appContext;
  @Mock private AsyncRequest asyncRequest;

  @Mock private Unmarshallable<ItemReference> unmarshallableItemRef;
  @Mock private Unmarshallable<List<ProjectConfigDto>> unmarshallablePrjsConf;
  @Mock private Unmarshallable<ProjectConfigDto> unmarshallablePrjConf;
  @Mock private Unmarshallable<ProjectSearchResponseDto> unmarshallableSearch;
  @Mock private Promise<ProjectSearchResponseDto> searchPromise;
  @Mock private Unmarshallable<List<SourceEstimation>> unmarshallbleSourcesEstimation;
  @Mock private Unmarshallable<SourceEstimation> unmarshallbleSourceEstimation;
  @Mock private Unmarshallable<TreeElement> unmarshallableTreeElem;

  @Mock private Promise<ItemReference> itemRefPromise;
  @Mock private MessageLoader messageLoader;

  @Mock private NewProjectConfigDto prjConfig1;
  @Mock private NewProjectConfigDto prjConfig2;
  @Mock private SourceStorageDto source;

  @Captor private ArgumentCaptor<List<NewProjectConfigDto>> prjsArgCaptor;

  private ProjectServiceClient client;

  @Before
  public void setUp() throws Exception {
    client =
        new ProjectServiceClient(
            loaderFactory, requestFactory, dtoFactory, unmarshaller, appContext);

    when(loaderFactory.newLoader(any(String.class))).thenReturn(messageLoader);
    when(asyncRequest.loader(messageLoader)).thenReturn(asyncRequest);
    when(asyncRequest.data(any(String.class))).thenReturn(asyncRequest);
    when(asyncRequest.send(unmarshallableItemRef)).thenReturn(itemRefPromise);
    when(asyncRequest.header(any(String.class), any(String.class))).thenReturn(asyncRequest);
    when(unmarshaller.newUnmarshaller(ItemReference.class)).thenReturn(unmarshallableItemRef);
    when(unmarshaller.newListUnmarshaller(ProjectConfigDto.class))
        .thenReturn(unmarshallablePrjsConf);
    when(unmarshaller.newListUnmarshaller(SourceEstimation.class))
        .thenReturn(unmarshallbleSourcesEstimation);
    when(unmarshaller.newUnmarshaller(SourceEstimation.class))
        .thenReturn(unmarshallbleSourceEstimation);
    when(unmarshaller.newUnmarshaller(ProjectSearchResponseDto.class))
        .thenReturn(unmarshallableSearch);
    when(unmarshaller.newUnmarshaller(TreeElement.class)).thenReturn(unmarshallableTreeElem);
    when(unmarshaller.newUnmarshaller(ProjectConfigDto.class)).thenReturn(unmarshallablePrjConf);

    when(requestFactory.createGetRequest(any(String.class))).thenReturn(asyncRequest);
    when(requestFactory.createRequest(
            any(RequestBuilder.Method.class),
            any(String.class),
            any(SourceStorageDto.class),
            any(Boolean.class)))
        .thenReturn(asyncRequest);
    when(requestFactory.createRequest(
            any(RequestBuilder.Method.class), any(String.class), anyList(), any(Boolean.class)))
        .thenReturn(asyncRequest);
    when(requestFactory.createRequest(
            any(RequestBuilder.Method.class), any(String.class), any(), any(Boolean.class)))
        .thenReturn(asyncRequest);
    when(requestFactory.createPostRequest(any(String.class), anyList())).thenReturn(asyncRequest);
    when(requestFactory.createPostRequest(any(String.class), any())).thenReturn(asyncRequest);
    when(requestFactory.createPostRequest(
            any(String.class), org.mockito.ArgumentMatchers.<List<NewProjectConfigDto>>any()))
        .thenReturn(asyncRequest);
    when(requestFactory.createPostRequest(any(String.class), nullable(MimeType.class)))
        .thenReturn(asyncRequest);
    when(requestFactory.createPostRequest(any(String.class), nullable(SourceStorageDto.class)))
        .thenReturn(asyncRequest);
    when(requestFactory.createPostRequest(any(String.class), nullable(CopyOptions.class)))
        .thenReturn(asyncRequest);
    when(requestFactory.createPostRequest(any(String.class), nullable(MoveOptions.class)))
        .thenReturn(asyncRequest);
    when(requestFactory.createRequest(
            any(RequestBuilder.Method.class),
            any(String.class),
            any(CopyOptions.class),
            any(Boolean.class)))
        .thenReturn(asyncRequest);
  }

  @Test
  public void testShouldNotSetupLoaderForTheGetTreeMethod() throws Exception {
    when(asyncRequest.header(anyString(), anyString())).thenReturn(asyncRequest);

    client.getTree(Path.EMPTY, 1, true);

    verify(asyncRequest, never()).loader(any(AsyncRequestLoader.class)); // see CHE-3467
  }

  @Test
  public void shouldReturnListProjects() {
    client.getProjects();

    verify(requestFactory).createGetRequest(any());
    verify(asyncRequest).header(ACCEPT, APPLICATION_JSON);
    verify(unmarshaller).newListUnmarshaller(ProjectConfigDto.class);
    verify(asyncRequest).send(unmarshallablePrjsConf);
  }

  @Test
  public void shouldEncodeUrlAndEstimateProject() {
    String prjType = "java";

    client.estimate(resourcePath, prjType);

    verify(requestFactory).createGetRequest(any());
    verify(asyncRequest).header(ACCEPT, APPLICATION_JSON);
    verify(loaderFactory).newLoader("Estimating project...");
    verify(asyncRequest).loader(messageLoader);
    verify(asyncRequest).send(unmarshallbleSourceEstimation);
  }

  @Test
  public void shouldEncodeUrlAndResolveProjectSources() {
    client.resolveSources(resourcePath);

    verify(requestFactory).createGetRequest(any());
    verify(asyncRequest).header(ACCEPT, APPLICATION_JSON);
    verify(loaderFactory).newLoader("Resolving sources...");
    verify(asyncRequest).loader(messageLoader);
    verify(unmarshaller).newListUnmarshaller(SourceEstimation.class);
    verify(asyncRequest).send(unmarshallbleSourcesEstimation);
  }

  @Test
  public void shouldEncodeUrlAndImportProject() {
    client.importProject(resourcePath, source);

    verify(requestFactory).createPostRequest(any(), eq(source));
    verify(asyncRequest).header(CONTENT_TYPE, APPLICATION_JSON);
    verify(asyncRequest).send();
  }

  @Test
  public void shouldEncodeUrlAndSearchResourceReferences() {
    QueryExpression expression = new QueryExpression();
    expression.setName(TEXT);
    expression.setText(TEXT);
    expression.setPath(resourcePath.toString());
    expression.setMaxItems(100);
    expression.setSkipCount(10);
    when(asyncRequest.send(unmarshallableSearch)).thenReturn(searchPromise);

    client.search(expression);

    verify(requestFactory).createGetRequest(any());
    verify(asyncRequest).header(ACCEPT, APPLICATION_JSON);
    verify(loaderFactory).newLoader("Searching...");
    verify(asyncRequest).loader(messageLoader);
    verify(unmarshaller).newUnmarshaller(ProjectSearchResponseDto.class);
    verify(asyncRequest).send(unmarshallableSearch);
  }

  @Test
  public void shouldCreateOneProjectByBatch() {
    List<NewProjectConfigDto> configs = singletonList(prjConfig1);

    client.createBatchProjects(configs);

    verify(requestFactory).createPostRequest(anyString(), prjsArgCaptor.capture());
    verify(asyncRequest).header(ACCEPT, APPLICATION_JSON);
    verify(loaderFactory).newLoader("Creating project...");
    verify(asyncRequest).loader(messageLoader);
    verify(asyncRequest).send(unmarshallablePrjsConf);
    verify(unmarshaller).newListUnmarshaller(ProjectConfigDto.class);

    assertEquals(1, prjsArgCaptor.getValue().size());
  }

  @Test
  public void shouldCreateFewProjectByBatch() {
    List<NewProjectConfigDto> configs = Arrays.asList(prjConfig1, prjConfig2);

    client.createBatchProjects(configs);

    verify(requestFactory).createPostRequest(anyString(), prjsArgCaptor.capture());
    verify(asyncRequest).header(ACCEPT, APPLICATION_JSON);
    verify(loaderFactory).newLoader("Creating the batch of projects...");
    verify(asyncRequest).loader(messageLoader);
    verify(asyncRequest).send(unmarshallablePrjsConf);
    verify(unmarshaller).newListUnmarshaller(ProjectConfigDto.class);

    assertEquals(2, prjsArgCaptor.getValue().size());
  }

  @Test
  public void shouldEncodeUrlAndCreateFile() {
    client.createFile(resourcePath, TEXT);

    verify(requestFactory).createPostRequest(any(), any());
    verify(asyncRequest).data(TEXT);
    verify(loaderFactory).newLoader("Creating file...");
    verify(asyncRequest).loader(messageLoader);
    verify(asyncRequest).send(unmarshallableItemRef);
  }

  @Test
  public void shouldEncodeUrlAndGetFileContent() {
    client.getFileContent(resourcePath);

    verify(requestFactory).createGetRequest(any());
    verify(asyncRequest).send(any(StringUnmarshaller.class));
  }

  @Test
  public void shouldEncodeUrlAndSetFileContent() {
    client.setFileContent(resourcePath, TEXT);

    verify(requestFactory).createRequest(eq(PUT), any(), any(), eq(false));
    verify(asyncRequest).data(TEXT);
    verify(asyncRequest).send();
  }

  @Test
  public void shouldEncodeUrlAndCreateFolder() {
    client.createFolder(resourcePath);

    verify(requestFactory).createPostRequest(any(), any());
    verify(loaderFactory).newLoader("Creating folder...");
    verify(asyncRequest).loader(messageLoader);
    verify(unmarshaller).newUnmarshaller(ItemReference.class);
    verify(asyncRequest).send(unmarshallableItemRef);
  }

  @Test
  public void shouldEncodeUrlAndDeleteFolder() {
    client.deleteItem(resourcePath);

    verify(requestFactory).createRequest(eq(DELETE), any(), any(), eq(false));
    verify(loaderFactory).newLoader("Deleting resource...");
    verify(asyncRequest).loader(messageLoader);
    verify(asyncRequest).send();
  }

  @Test
  public void shouldEncodeUrlAndCopyResource() {
    CopyOptions copyOptions = mock(CopyOptions.class);
    when(dtoFactory.createDto(CopyOptions.class)).thenReturn(copyOptions);

    client.copy(resourcePath, targetPath, TEXT, true);

    verify(dtoFactory).createDto(CopyOptions.class);
    verify(copyOptions).setName(any());
    verify(copyOptions).setOverWrite(true);

    verify(requestFactory).createPostRequest(any(), eq(copyOptions));
    verify(loaderFactory).newLoader("Copying...");
    verify(asyncRequest).loader(messageLoader);
    verify(asyncRequest).send();
  }

  @Test
  public void shouldEncodeUrlAndMoveResource() {
    MoveOptions moveOptions = mock(MoveOptions.class);
    when(dtoFactory.createDto(MoveOptions.class)).thenReturn(moveOptions);

    client.move(resourcePath, targetPath, TEXT, true);

    verify(dtoFactory).createDto(MoveOptions.class);
    verify(moveOptions).setName(any());
    verify(moveOptions).setOverWrite(true);
    verify(requestFactory).createPostRequest(any(), eq(moveOptions));
    verify(loaderFactory).newLoader("Moving...");
    verify(asyncRequest).loader(messageLoader);
    verify(asyncRequest).send();
  }

  @Test
  public void shouldEncodeUrlAndGetTree() {
    client.getTree(resourcePath, 2, true);

    verify(requestFactory).createGetRequest(any());
    verify(asyncRequest).header(ACCEPT, APPLICATION_JSON);
    verify(unmarshaller).newUnmarshaller(TreeElement.class);
    verify(asyncRequest).send(unmarshallableTreeElem);
  }

  @Test
  public void shouldEncodeUrlAndGetItem() {
    client.getItem(resourcePath);

    verify(requestFactory).createGetRequest(any());
    verify(asyncRequest).header(ACCEPT, APPLICATION_JSON);
    verify(loaderFactory).newLoader("Getting item...");
    verify(unmarshaller).newUnmarshaller(ItemReference.class);
    verify(asyncRequest).send(unmarshallableItemRef);
  }

  @Test
  public void shouldEncodeUrlAndGetProject() {
    client.getProject(Path.valueOf(TEXT));

    verify(requestFactory).createGetRequest(any());
    verify(asyncRequest).header(ACCEPT, APPLICATION_JSON);
    verify(loaderFactory).newLoader("Getting project...");
    verify(asyncRequest).loader(messageLoader);
    verify(unmarshaller).newUnmarshaller(ProjectConfigDto.class);
    verify(asyncRequest).send(unmarshallablePrjConf);
  }

  @Test
  public void shouldEncodeUrlAndUpdateProject() {
    when(requestFactory.createRequest(
            any(RequestBuilder.Method.class), anyString(), any(ProjectConfig.class), anyBoolean()))
        .thenReturn(asyncRequest);
    when(prjConfig1.getPath()).thenReturn(TEXT);

    client.updateProject(prjConfig1);

    verify(requestFactory).createRequest(eq(PUT), anyString(), eq(prjConfig1), eq(false));
    verify(asyncRequest).header(CONTENT_TYPE, APPLICATION_JSON);
    verify(asyncRequest).header(ACCEPT, APPLICATION_JSON);
    verify(loaderFactory).newLoader("Updating project...");
    verify(asyncRequest).loader(messageLoader);
    verify(unmarshaller).newUnmarshaller(ProjectConfigDto.class);
    verify(asyncRequest).send(unmarshallablePrjConf);
  }
}
