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
package org.eclipse.che.infrastructure.docker.client;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.infrastructure.docker.client.connection.CloseConnectionInputStream;
import org.eclipse.che.infrastructure.docker.client.connection.DockerConnection;
import org.eclipse.che.infrastructure.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.infrastructure.docker.client.connection.DockerResponse;
import org.eclipse.che.infrastructure.docker.client.dto.AuthConfig;
import org.eclipse.che.infrastructure.docker.client.dto.AuthConfigs;
import org.eclipse.che.infrastructure.docker.client.exception.ContainerNotFoundException;
import org.eclipse.che.infrastructure.docker.client.exception.DockerException;
import org.eclipse.che.infrastructure.docker.client.exception.ExecNotFoundException;
import org.eclipse.che.infrastructure.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.infrastructure.docker.client.exception.NetworkNotFoundException;
import org.eclipse.che.infrastructure.docker.client.exception.VolumeNotFoundException;
import org.eclipse.che.infrastructure.docker.client.json.ContainerCommitted;
import org.eclipse.che.infrastructure.docker.client.json.ContainerConfig;
import org.eclipse.che.infrastructure.docker.client.json.ContainerCreated;
import org.eclipse.che.infrastructure.docker.client.json.ContainerExitStatus;
import org.eclipse.che.infrastructure.docker.client.json.ContainerInfo;
import org.eclipse.che.infrastructure.docker.client.json.ContainerListEntry;
import org.eclipse.che.infrastructure.docker.client.json.ContainerProcesses;
import org.eclipse.che.infrastructure.docker.client.json.Event;
import org.eclipse.che.infrastructure.docker.client.json.ExecCreated;
import org.eclipse.che.infrastructure.docker.client.json.ExecInfo;
import org.eclipse.che.infrastructure.docker.client.json.Filters;
import org.eclipse.che.infrastructure.docker.client.json.Image;
import org.eclipse.che.infrastructure.docker.client.json.ImageInfo;
import org.eclipse.che.infrastructure.docker.client.json.NetworkCreated;
import org.eclipse.che.infrastructure.docker.client.json.SystemInfo;
import org.eclipse.che.infrastructure.docker.client.json.Version;
import org.eclipse.che.infrastructure.docker.client.json.network.ConnectContainer;
import org.eclipse.che.infrastructure.docker.client.json.network.ContainerInNetwork;
import org.eclipse.che.infrastructure.docker.client.json.network.DisconnectContainer;
import org.eclipse.che.infrastructure.docker.client.json.network.EndpointConfig;
import org.eclipse.che.infrastructure.docker.client.json.network.Ipam;
import org.eclipse.che.infrastructure.docker.client.json.network.IpamConfig;
import org.eclipse.che.infrastructure.docker.client.json.network.Network;
import org.eclipse.che.infrastructure.docker.client.json.network.NewIpamConfig;
import org.eclipse.che.infrastructure.docker.client.json.network.NewNetwork;
import org.eclipse.che.infrastructure.docker.client.json.volume.Volume;
import org.eclipse.che.infrastructure.docker.client.json.volume.Volumes;
import org.eclipse.che.infrastructure.docker.client.params.AttachContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.BuildImageParams;
import org.eclipse.che.infrastructure.docker.client.params.CommitParams;
import org.eclipse.che.infrastructure.docker.client.params.CreateContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.CreateExecParams;
import org.eclipse.che.infrastructure.docker.client.params.GetContainerLogsParams;
import org.eclipse.che.infrastructure.docker.client.params.GetEventsParams;
import org.eclipse.che.infrastructure.docker.client.params.GetExecInfoParams;
import org.eclipse.che.infrastructure.docker.client.params.GetResourceParams;
import org.eclipse.che.infrastructure.docker.client.params.InspectContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.InspectImageParams;
import org.eclipse.che.infrastructure.docker.client.params.KillContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.ListContainersParams;
import org.eclipse.che.infrastructure.docker.client.params.ListImagesParams;
import org.eclipse.che.infrastructure.docker.client.params.PullParams;
import org.eclipse.che.infrastructure.docker.client.params.PushParams;
import org.eclipse.che.infrastructure.docker.client.params.PutResourceParams;
import org.eclipse.che.infrastructure.docker.client.params.RemoveContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.RemoveImageParams;
import org.eclipse.che.infrastructure.docker.client.params.StartContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.StartExecParams;
import org.eclipse.che.infrastructure.docker.client.params.StopContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.TagParams;
import org.eclipse.che.infrastructure.docker.client.params.TopParams;
import org.eclipse.che.infrastructure.docker.client.params.WaitContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.network.ConnectContainerToNetworkParams;
import org.eclipse.che.infrastructure.docker.client.params.network.CreateNetworkParams;
import org.eclipse.che.infrastructure.docker.client.params.network.DisconnectContainerFromNetworkParams;
import org.eclipse.che.infrastructure.docker.client.params.network.GetNetworksParams;
import org.eclipse.che.infrastructure.docker.client.params.network.InspectNetworkParams;
import org.eclipse.che.infrastructure.docker.client.params.network.RemoveNetworkParams;
import org.eclipse.che.infrastructure.docker.client.params.volume.GetVolumesParams;
import org.eclipse.che.infrastructure.docker.client.params.volume.RemoveVolumeParams;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * @author Anton Korneta
 * @author Mykola Morhun
 */
@Listeners(MockitoTestNGListener.class)
public class DockerConnectorTest {
  private static final Gson GSON =
      new GsonBuilder()
          .disableHtmlEscaping()
          .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
          .create();

  private static final String ERROR_MESSAGE = "some error occurs";
  private static final String EXCEPTION_ERROR_MESSAGE =
      "Error response from docker API, status: 500, message: " + ERROR_MESSAGE;
  private static final int RESPONSE_ERROR_CODE = 500;
  private static final int RESPONSE_NOT_FOUND_CODE = 404;
  private static final int RESPONSE_SUCCESS_CODE = 200;
  private static final int RESPONSE_NO_CONTENT_CODE = 204;
  private static final int RESPONSE_CREATED_CODE = 201;

  private static final String REQUEST_METHOD_GET = "GET";
  private static final String REQUEST_METHOD_POST = "POST";
  private static final String REQUEST_METHOD_PUT = "PUT";
  private static final String REQUEST_METHOD_DELETE = "DELETE";

  private static final String IMAGE = "image";
  private static final String REPOSITORY = "repository";
  private static final String CONTAINER = "container";
  private static final String REGISTRY = "registry";
  private static final String TAG = "tag";
  private static final String DIGEST = "hash:1234567890";
  private static final String EXEC_ID = "exec_id";
  private static final String PATH_TO_FILE = "/home/user/path/file.ext";
  private static final String STREAM_DATA = "stream data";
  private static final String DOCKER_RESPONSE = "stream";
  private static final String API_VERSION_PREFIX = "";
  private static final String[] CMD_WITH_ARGS = {"command", "arg1", "arg2"};
  private static final String[] CMD_ARGS = {"arg1", "arg2"};
  private static final byte[] STREAM_DATA_BYTES = STREAM_DATA.getBytes();
  private static final byte[] DOCKER_RESPONSE_BYTES = DOCKER_RESPONSE.getBytes();

  private static final int CONTAINER_EXIT_CODE = 0;
  private static final int IMAGE_BUILD_TIMEOUT = 300;

  @Mock private DockerConnectorConfiguration dockerConnectorConfiguration;
  @Mock private DockerConnectionFactory dockerConnectionFactory;
  @Mock private DockerResponse dockerResponse;
  @Mock private ProgressMonitor progressMonitor;
  @Mock private InitialAuthConfig initialAuthConfig;
  @Mock private AuthConfigs authConfigs;
  @Mock private MessageProcessor<LogMessage> logMessageProcessor;
  @Mock private MessageProcessor<Event> eventMessageProcessor;
  @Mock private File dockerfile;
  @Mock private DockerRegistryAuthResolver authManager;
  @Mock private DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider;

  @Captor private ArgumentCaptor<Object> captor;

  private InputStream inputStream;
  private DockerConnector dockerConnector;
  private DockerConnection dockerConnection;

  @BeforeMethod
  public void setup() throws IOException, URISyntaxException {
    dockerConnection = mock(DockerConnection.class, new SelfReturningAnswer());
    when(dockerConnectionFactory.openConnection(nullable(URI.class))).thenReturn(dockerConnection);
    when(dockerConnection.request()).thenReturn(dockerResponse);
    when(dockerConnectorConfiguration.getAuthConfigs()).thenReturn(initialAuthConfig);
    when(dockerResponse.getStatus()).thenReturn(RESPONSE_SUCCESS_CODE);
    when(initialAuthConfig.getAuthConfigs()).thenReturn(authConfigs);
    when(authConfigs.getConfigs()).thenReturn(new HashMap<>());
    when(dockerApiVersionPathPrefixProvider.get()).thenReturn(API_VERSION_PREFIX);

    dockerConnector = newConnectorSpy(IMAGE_BUILD_TIMEOUT, dockerApiVersionPathPrefixProvider);

    inputStream = spy(new ByteArrayInputStream(ERROR_MESSAGE.getBytes()));
    when(dockerResponse.getInputStream()).thenReturn(inputStream);
  }

  @Test
  public void shouldBeAbleToGetSystemInfo() throws IOException, JsonParseException {
    SystemInfo systemInfo = mock(SystemInfo.class);

    doReturn(systemInfo)
        .when(dockerConnector)
        .parseResponseStreamAndClose(inputStream, SystemInfo.class);

    SystemInfo returnedSystemInfo = dockerConnector.getSystemInfo();

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/info");
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedSystemInfo, systemInfo);
  }

  @Test
  public void shouldBeAbleToGetSystemInfoByUrlWithConcreteApiVersion()
      throws IOException, JsonParseException {
    String apiVersion = "/v1.18";
    when(dockerApiVersionPathPrefixProvider.get()).thenReturn(apiVersion);
    dockerConnector = newConnectorSpy(IMAGE_BUILD_TIMEOUT, dockerApiVersionPathPrefixProvider);
    SystemInfo systemInfo = mock(SystemInfo.class);
    doReturn(systemInfo)
        .when(dockerConnector)
        .parseResponseStreamAndClose(inputStream, SystemInfo.class);

    SystemInfo returnedSystemInfo = dockerConnector.getSystemInfo();

    verify(dockerConnection).path(apiVersion + "/info");
    assertEquals(returnedSystemInfo, systemInfo);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileGettingSystemInfoIfResponseCodeIsNotSuccess()
      throws IOException {
    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.getSystemInfo();

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToGetVersion() throws IOException, JsonParseException {
    Version version = mock(Version.class);

    doReturn(version).when(dockerConnector).parseResponseStreamAndClose(inputStream, Version.class);

    Version returnedVersion = dockerConnector.getVersion();

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/version");
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedVersion, version);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileGettingVersionIfResponseCodeIsNotSuccess()
      throws IOException {
    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.getVersion();

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToGetListImages() throws IOException, JsonParseException {
    ListImagesParams listImagesParams = ListImagesParams.create();
    List<Image> images = new ArrayList<>();
    images.add(mock(Image.class));

    doReturn(images)
        .when(dockerConnector)
        .parseResponseStreamAndClose(
            eq(inputStream), org.mockito.ArgumentMatchers.<TypeToken<List<Image>>>any());

    List<Image> returnedImages = dockerConnector.listImages(listImagesParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/images/json");
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedImages, images);
  }

  @Test
  public void shouldInvokeGetListImagesWithDefaultParamsWhenGetListImagesCalledWithoutParams()
      throws IOException, JsonParseException {
    List<Image> images = new ArrayList<>();
    images.add(mock(Image.class));

    doReturn(images)
        .when(dockerConnector)
        .parseResponseStreamAndClose(
            eq(inputStream), org.mockito.ArgumentMatchers.<TypeToken<List<Image>>>any());

    List<Image> returnedImages = dockerConnector.listImages();

    verify(dockerConnector).listImages((ListImagesParams) captor.capture());

    assertEquals(captor.getValue(), ListImagesParams.create());
    assertEquals(returnedImages, images);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileListingImagesIfResponseCodeIsNotSuccess()
      throws IOException {
    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.listImages();

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToGetListContainersWithListContainersParams()
      throws IOException, JsonParseException {
    ListContainersParams listContainersParams =
        ListContainersParams.create().withAll(true).withSize(true);
    ContainerListEntry containerListEntry = mock(ContainerListEntry.class);
    List<ContainerListEntry> expectedListContainers = singletonList(containerListEntry);

    doReturn(expectedListContainers)
        .when(dockerConnector)
        .parseResponseStreamAndClose(
            eq(inputStream),
            org.mockito.ArgumentMatchers.<TypeToken<List<ContainerListEntry>>>any());

    List<ContainerListEntry> containers = dockerConnector.listContainers(listContainersParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/containers/json");
    verify(dockerConnection).query("all", 1);
    verify(dockerConnection).query("size", 1);
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
    verify(dockerConnector)
        .parseResponseStreamAndClose(
            eq(inputStream),
            org.mockito.ArgumentMatchers.<TypeToken<List<ContainerListEntry>>>any());

    assertEquals(containers, expectedListContainers);
  }

  @Test
  public void shouldBeAbleToGetListContainersByFiltersInTheListContainersParams()
      throws IOException, JsonParseException {
    Filters filters = new Filters().withFilter("testKey", "testValue");
    ListContainersParams listContainersParams = ListContainersParams.create().withFilters(filters);
    ContainerListEntry containerListEntry = mock(ContainerListEntry.class);
    List<ContainerListEntry> expectedListContainers = singletonList(containerListEntry);

    doReturn(expectedListContainers)
        .when(dockerConnector)
        .parseResponseStreamAndClose(
            eq(inputStream),
            org.mockito.ArgumentMatchers.<TypeToken<List<ContainerListEntry>>>any());

    List<ContainerListEntry> containers = dockerConnector.listContainers(listContainersParams);

    verify(dockerConnection).query(eq("filters"), any());
    assertEquals(containers, expectedListContainers);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void
      shouldThrowDockerExceptionWhileGettingListContainersByParamsObjectIfResponseCodeIsNotSuccess()
          throws IOException, JsonParseException {
    ListContainersParams listContainersParams = ListContainersParams.create();

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.listContainers(listContainersParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldCallListContainersWithParametersObject() throws IOException {
    ListContainersParams listContainersParams = ListContainersParams.create().withAll(true);
    ContainerListEntry containerListEntry = mock(ContainerListEntry.class);
    List<ContainerListEntry> expectedListContainers = singletonList(containerListEntry);

    doReturn(expectedListContainers).when(dockerConnector).listContainers(listContainersParams);

    List<ContainerListEntry> result = dockerConnector.listContainers();

    ArgumentCaptor<ListContainersParams> listContainersParamsArgumentCaptor =
        ArgumentCaptor.forClass(ListContainersParams.class);
    verify(dockerConnector).listContainers(listContainersParamsArgumentCaptor.capture());

    assertEquals(result, expectedListContainers);
    assertEquals(listContainersParamsArgumentCaptor.getValue(), listContainersParams);
  }

  @Test
  public void shouldCallInspectImageWithParametersObject() throws IOException {
    InspectImageParams inspectImageParams = InspectImageParams.create(IMAGE);

    ImageInfo imageInfo = mock(ImageInfo.class);

    doReturn(imageInfo).when(dockerConnector).inspectImage(any(InspectImageParams.class));

    ImageInfo returnedImageInfo = dockerConnector.inspectImage(IMAGE);

    verify(dockerConnector).inspectImage((InspectImageParams) captor.capture());

    assertEquals(captor.getValue(), inspectImageParams);
    assertEquals(returnedImageInfo, imageInfo);
  }

  @Test
  public void shouldBeAbleToInspectImage() throws IOException, JsonParseException {
    InspectImageParams inspectImageParams = InspectImageParams.create(IMAGE);

    ImageInfo imageInfo = mock(ImageInfo.class);

    doReturn(imageInfo)
        .when(dockerConnector)
        .parseResponseStreamAndClose(inputStream, ImageInfo.class);

    ImageInfo returnedImageInfo = dockerConnector.inspectImage(inspectImageParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/images/" + inspectImageParams.getImage() + "/json");
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedImageInfo, imageInfo);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileGettingImageInfoIfResponseCodeIsNotSuccess()
      throws IOException {
    InspectImageParams inspectImageParams = InspectImageParams.create(IMAGE);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.inspectImage(inspectImageParams);

    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = ImageNotFoundException.class,
    expectedExceptionsMessageRegExp = ERROR_MESSAGE
  )
  public void shouldThrowImageNotFoundExceptionOnGettingImageInfoIfResponseCodeIs404()
      throws IOException {
    InspectImageParams inspectImageParams = InspectImageParams.create(IMAGE);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_NOT_FOUND_CODE);

    dockerConnector.inspectImage(inspectImageParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToStopContainer() throws IOException {
    StopContainerParams stopContainerParams = StopContainerParams.create(CONTAINER);

    dockerConnector.stopContainer(stopContainerParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/containers/" + stopContainerParams.getContainer() + "/stop");
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileStoppingContainerIfResponseCodeIsNotSuccess()
      throws IOException {
    StopContainerParams stopContainerParams = StopContainerParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.stopContainer(stopContainerParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldCallKillContainerWithParametersObject() throws IOException {
    KillContainerParams killContainerParams = KillContainerParams.create(CONTAINER);

    doNothing().when(dockerConnector).killContainer(killContainerParams);

    dockerConnector.killContainer(CONTAINER);

    verify(dockerConnector).killContainer((KillContainerParams) captor.capture());

    assertEquals(captor.getValue(), killContainerParams);
  }

  @Test
  public void shouldBeAbleToKillContainer() throws IOException {
    KillContainerParams killContainerParams = KillContainerParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_NO_CONTENT_CODE);

    dockerConnector.killContainer(killContainerParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/containers/" + killContainerParams.getContainer() + "/kill");
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileKillingContainerIfResponseCodeIsNotSuccess()
      throws IOException {
    KillContainerParams killContainerParams = KillContainerParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.killContainer(killContainerParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToRemoveContainer() throws IOException {
    RemoveContainerParams removeContainerParams = RemoveContainerParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_NO_CONTENT_CODE);

    dockerConnector.removeContainer(removeContainerParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_DELETE);
    verify(dockerConnection).path("/containers/" + removeContainerParams.getContainer());
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileRemovingContainerIfResponseCodeIsNotSuccess()
      throws IOException {
    RemoveContainerParams removeContainerParams = RemoveContainerParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.removeContainer(removeContainerParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldCallWaitContainerWithParametersObject() throws IOException {
    WaitContainerParams waitContainerParams = WaitContainerParams.create(CONTAINER);

    doReturn(CONTAINER_EXIT_CODE).when(dockerConnector).waitContainer(waitContainerParams);

    int returnedExitCode = dockerConnector.waitContainer(CONTAINER);

    verify(dockerConnector).waitContainer((WaitContainerParams) captor.capture());

    assertEquals(captor.getValue(), waitContainerParams);
    assertEquals(returnedExitCode, CONTAINER_EXIT_CODE);
  }

  @Test
  public void shouldBeAbleToWaitContainer() throws IOException, JsonParseException {
    WaitContainerParams waitContainerParams = WaitContainerParams.create(CONTAINER);

    ContainerExitStatus containerExitStatus = new ContainerExitStatus();
    containerExitStatus.setStatusCode(CONTAINER_EXIT_CODE);

    doReturn(containerExitStatus)
        .when(dockerConnector)
        .parseResponseStreamAndClose(inputStream, ContainerExitStatus.class);

    int returnedExitCode = dockerConnector.waitContainer(waitContainerParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/containers/" + waitContainerParams.getContainer() + "/wait");
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedExitCode, containerExitStatus.getStatusCode());
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileWaitingContainerIfResponseCodeIsNotSuccess()
      throws IOException {
    WaitContainerParams waitContainerParams = WaitContainerParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.waitContainer(waitContainerParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldCallInspectContainerWithParametersObject() throws IOException {
    InspectContainerParams inspectContainerParams = InspectContainerParams.create(CONTAINER);

    ContainerInfo containerInfo = mock(ContainerInfo.class);

    doReturn(containerInfo).when(dockerConnector).inspectContainer(inspectContainerParams);

    ContainerInfo returnedContainerInfo = dockerConnector.inspectContainer(CONTAINER);

    verify(dockerConnector).inspectContainer((InspectContainerParams) captor.capture());

    assertEquals(captor.getValue(), inspectContainerParams);
    assertEquals(returnedContainerInfo, containerInfo);
  }

  @Test
  public void shouldBeAbleToInspectContainer() throws IOException, JsonParseException {
    InspectContainerParams inspectContainerParams = InspectContainerParams.create(CONTAINER);

    ContainerInfo containerInfo = mock(ContainerInfo.class);

    doReturn(containerInfo)
        .when(dockerConnector)
        .parseResponseStreamAndClose(inputStream, ContainerInfo.class);

    ContainerInfo returnedContainerInfo = dockerConnector.inspectContainer(inspectContainerParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/containers/" + inspectContainerParams.getContainer() + "/json");
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedContainerInfo, containerInfo);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileInspectingContainerIfResponseCodeIsNotSuccess()
      throws IOException {
    InspectContainerParams inspectContainerParams = InspectContainerParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.inspectContainer(inspectContainerParams);

    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = ContainerNotFoundException.class,
    expectedExceptionsMessageRegExp = ERROR_MESSAGE
  )
  public void shouldThrowContainerNotFoundExceptionOnInspectingContainerIfResponseCodeIs404()
      throws IOException {
    InspectContainerParams inspectContainerParams = InspectContainerParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_NOT_FOUND_CODE);

    dockerConnector.inspectContainer(inspectContainerParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToAttachContainer() throws IOException {
    AttachContainerParams attachContainerParams = AttachContainerParams.create(CONTAINER);

    when(dockerResponse.getInputStream())
        .thenReturn(new ByteArrayInputStream(DOCKER_RESPONSE_BYTES));

    dockerConnector.attachContainer(attachContainerParams, logMessageProcessor);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection)
        .path("/containers/" + attachContainerParams.getContainer() + "/attach");
    verify(dockerConnection).query("stdout", 1);
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileAttachingContainerIfResponseCodeIsNotSuccess()
      throws IOException {
    AttachContainerParams attachContainerParams = AttachContainerParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.attachContainer(attachContainerParams, logMessageProcessor);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToGetContainerLogs() throws IOException {
    GetContainerLogsParams getContainerLogsParams = GetContainerLogsParams.create(CONTAINER);

    when(dockerResponse.getInputStream())
        .thenReturn(new ByteArrayInputStream(DOCKER_RESPONSE_BYTES));

    dockerConnector.getContainerLogs(getContainerLogsParams, logMessageProcessor);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/containers/" + getContainerLogsParams.getContainer() + "/logs");
    verify(dockerConnection).query("stdout", 1);
    verify(dockerConnection).query("stderr", 1);
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileGettingContainerLogsIfResponseCodeIs5xx()
      throws IOException {
    GetContainerLogsParams getContainerLogsParams = GetContainerLogsParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.getContainerLogs(getContainerLogsParams, logMessageProcessor);

    verify(dockerResponse).getStatus();
  }

  @Test(expectedExceptions = ContainerNotFoundException.class)
  public void shouldThrowContainerNotFoundExceptionWhileGettingContainerLogsIfResponseCodeIs404()
      throws IOException {
    GetContainerLogsParams getContainerLogsParams = GetContainerLogsParams.create(CONTAINER);

    when(dockerResponse.getInputStream())
        .thenReturn(new ByteArrayInputStream("container not found".getBytes()));
    when(dockerResponse.getStatus()).thenReturn(RESPONSE_NOT_FOUND_CODE);

    dockerConnector.getContainerLogs(getContainerLogsParams, logMessageProcessor);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToCreateExec() throws IOException, JsonParseException {
    CreateExecParams createExecParams = CreateExecParams.create(CONTAINER, CMD_WITH_ARGS);
    Exec exec = new Exec(CMD_WITH_ARGS, EXEC_ID);

    ExecCreated execCreated = mock(ExecCreated.class);

    doReturn(execCreated)
        .when(dockerConnector)
        .parseResponseStreamAndClose(inputStream, ExecCreated.class);
    when(execCreated.getId()).thenReturn(EXEC_ID);

    Exec returnedExec = dockerConnector.createExec(createExecParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/containers/" + createExecParams.getContainer() + "/exec");
    verify(dockerConnection).header("Content-Type", MediaType.APPLICATION_JSON);
    verify(dockerConnection).header(eq("Content-Length"), anyInt());
    verify(dockerConnection).entity(any(byte[].class));
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedExec, exec);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileCreatingExecIfResponseCodeIsNotSuccess()
      throws IOException {
    CreateExecParams inspectContainerParams = CreateExecParams.create(CONTAINER, CMD_WITH_ARGS);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.createExec(inspectContainerParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToStartExec() throws IOException {
    StartExecParams startExecParams = StartExecParams.create(EXEC_ID);

    doReturn(new ByteArrayInputStream(DOCKER_RESPONSE_BYTES)).when(dockerResponse).getInputStream();

    dockerConnector.startExec(startExecParams, logMessageProcessor);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/exec/" + startExecParams.getExecId() + "/start");
    verify(dockerConnection).header("Content-Type", MediaType.APPLICATION_JSON);
    verify(dockerConnection).header(eq("Content-Length"), anyInt());
    verify(dockerConnection).entity(any(byte[].class));
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test(
    expectedExceptions = ExecNotFoundException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void execStartShouldThrowExecNotFoundIf404Received() throws IOException {
    StartExecParams startExecParams = StartExecParams.create(EXEC_ID);

    doReturn(new ByteArrayInputStream(EXCEPTION_ERROR_MESSAGE.getBytes()))
        .when(dockerResponse)
        .getInputStream();
    when(dockerResponse.getStatus()).thenReturn(RESPONSE_NOT_FOUND_CODE);

    dockerConnector.startExec(startExecParams, logMessageProcessor);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileStartingExecIfResponseCodeIsNotSuccess()
      throws IOException {
    StartExecParams startExecParams = StartExecParams.create(EXEC_ID);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.startExec(startExecParams, logMessageProcessor);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldCallGetExecInfoWithParametersObject() throws IOException {
    GetExecInfoParams getExecInfoParams = GetExecInfoParams.create(EXEC_ID);

    ExecInfo execInfo = mock(ExecInfo.class);

    doReturn(execInfo).when(dockerConnector).getExecInfo(any(GetExecInfoParams.class));

    dockerConnector.getExecInfo(EXEC_ID);

    verify(dockerConnector).getExecInfo((GetExecInfoParams) captor.capture());

    assertEquals(captor.getValue(), getExecInfoParams);
  }

  @Test
  public void shouldBeAbleToGetExecInfo() throws IOException, JsonParseException {
    GetExecInfoParams getExecInfoParams = GetExecInfoParams.create(EXEC_ID);

    ExecInfo execInfo = mock(ExecInfo.class);

    doReturn(execInfo)
        .when(dockerConnector)
        .parseResponseStreamAndClose(inputStream, ExecInfo.class);

    ExecInfo returnedExecInfo = dockerConnector.getExecInfo(getExecInfoParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/exec/" + getExecInfoParams.getExecId() + "/json");
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedExecInfo, execInfo);
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowIOExceptionWhileGettingExecInfoIfResponseCodeIsNotSuccess()
      throws IOException {
    GetExecInfoParams getExecInfoParams = GetExecInfoParams.create(EXEC_ID);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.getExecInfo(getExecInfoParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void topShouldBeAbleToGetProcesses() throws IOException, JsonParseException {
    TopParams topParams = TopParams.create(CONTAINER);

    ContainerProcesses containerProcesses = mock(ContainerProcesses.class);

    doReturn(containerProcesses)
        .when(dockerConnector)
        .parseResponseStreamAndClose(inputStream, ContainerProcesses.class);

    ContainerProcesses returnedContainerProcesses = dockerConnector.top(topParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/containers/" + topParams.getContainer() + "/top");
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedContainerProcesses, containerProcesses);
  }

  @Test
  public void topWithPsArgsShouldBeAbleToGetProcesses() throws IOException, JsonParseException {
    TopParams topParams = TopParams.create(CONTAINER).withPsArgs(CMD_ARGS);

    ContainerProcesses containerProcesses = mock(ContainerProcesses.class);

    doReturn(containerProcesses)
        .when(dockerConnector)
        .parseResponseStreamAndClose(inputStream, ContainerProcesses.class);

    ContainerProcesses returnedContainerProcesses = dockerConnector.top(topParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/containers/" + topParams.getContainer() + "/top");
    verify(dockerConnection).query(eq("ps_args"), anyString());
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedContainerProcesses, containerProcesses);
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void topShouldThrowIOExceptionWhileGettingProcessesIfResponseCodeIsNotSuccess()
      throws IOException {
    TopParams topParams = TopParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.top(topParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToGetResourcesFromContainer() throws IOException {
    GetResourceParams getResourceParams = GetResourceParams.create(CONTAINER, PATH_TO_FILE);

    when(dockerResponse.getInputStream())
        .thenReturn(
            new CloseConnectionInputStream(
                new ByteArrayInputStream(STREAM_DATA_BYTES), dockerConnection));

    String response =
        CharStreams.toString(new InputStreamReader(dockerConnector.getResource(getResourceParams)));

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/containers/" + getResourceParams.getContainer() + "/archive");
    verify(dockerConnection).query(eq("path"), eq(PATH_TO_FILE));
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(response, STREAM_DATA);
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldProduceErrorWhenGetsResourcesFromContainerIfResponseCodeIsNotSuccess()
      throws IOException {
    GetResourceParams getResourceParams = GetResourceParams.create(CONTAINER, PATH_TO_FILE);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);
    when(dockerResponse.getInputStream())
        .thenReturn(
            new CloseConnectionInputStream(
                new ByteArrayInputStream(ERROR_MESSAGE.getBytes()), dockerConnection));

    dockerConnector.getResource(getResourceParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToPutResourcesIntoContainer() throws IOException {
    InputStream source =
        new CloseConnectionInputStream(
            new ByteArrayInputStream(STREAM_DATA_BYTES), dockerConnection);
    PutResourceParams putResourceParams = PutResourceParams.create(CONTAINER, PATH_TO_FILE, source);

    dockerConnector.putResource(putResourceParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_PUT);
    verify(dockerConnection).path("/containers/" + putResourceParams.getContainer() + "/archive");
    verify(dockerConnection).query(eq("path"), eq(PATH_TO_FILE));
    verify(dockerConnection).header("Content-Type", ExtMediaType.APPLICATION_X_TAR);
    verify(dockerConnection).header(eq("Content-Length"), anyLong());
    verify(dockerConnection).entity(any(InputStream.class));
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldProduceErrorWhenPutsResourcesIntoContainerIfResponseCodeIsNotSuccess()
      throws IOException {
    InputStream source =
        new CloseConnectionInputStream(
            new ByteArrayInputStream(ERROR_MESSAGE.getBytes()), dockerConnection);
    PutResourceParams putResourceParams = PutResourceParams.create(CONTAINER, PATH_TO_FILE, source);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);
    when(dockerResponse.getInputStream())
        .thenReturn(new ByteArrayInputStream(ERROR_MESSAGE.getBytes()));

    dockerConnector.putResource(putResourceParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToGetEvents() throws IOException {
    GetEventsParams getEventsParams = GetEventsParams.create();

    doReturn(new ByteArrayInputStream(STREAM_DATA_BYTES)).when(dockerResponse).getInputStream();

    dockerConnector.getEvents(getEventsParams, eventMessageProcessor);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/events");
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileGettingEventsIfResponseCodeIsNotSuccess()
      throws IOException {
    GetEventsParams getExecInfoParams = GetEventsParams.create();

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.getEvents(getExecInfoParams, eventMessageProcessor);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToBuildImage() throws IOException, InterruptedException {
    AuthConfigs authConfigs = DtoFactory.newDto(AuthConfigs.class);
    AuthConfig authConfig = DtoFactory.newDto(AuthConfig.class);
    Map<String, AuthConfig> auth = new HashMap<>();
    auth.put("auth", authConfig);
    authConfigs.setConfigs(auth);

    final String imageId = "37a7da3b7edc";

    BuildImageParams buildImageParams =
        BuildImageParams.create(dockerfile).withAuthConfigs(authConfigs);

    doReturn(
            new ByteArrayInputStream(
                ("{\"stream\":\"Successfully built " + imageId + "\"}").getBytes()))
        .when(dockerResponse)
        .getInputStream();

    String returnedImageId = dockerConnector.buildImage(buildImageParams, progressMonitor);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/build");

    verify(dockerConnection).header("Content-Type", "application/x-compressed-tar");
    verify(dockerConnection).header(eq("Content-Length"), anyLong());
    verify(dockerConnection).entity(any(InputStream.class));
    verify(dockerConnection, never()).header(eq("remote"), anyString());

    verify(dockerConnection).header(eq("X-Registry-Config"), nullable(byte[].class));
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedImageId, imageId);
  }

  @Test
  public void shouldBeAbleToBuildImageWithRemoteContext() throws IOException, InterruptedException {
    AuthConfigs authConfigs = DtoFactory.newDto(AuthConfigs.class);
    AuthConfig authConfig = DtoFactory.newDto(AuthConfig.class);
    Map<String, AuthConfig> auth = new HashMap<>();
    auth.put("auth", authConfig);
    authConfigs.setConfigs(auth);

    final String imageId = "37a7da3b7edc";
    final String remote = "https://some.host.com/path/tarball.tar";

    BuildImageParams buildImageParams =
        BuildImageParams.create(remote).withAuthConfigs(authConfigs);

    doReturn(
            new ByteArrayInputStream(
                ("{\"stream\":\"Successfully built " + imageId + "\"}").getBytes()))
        .when(dockerResponse)
        .getInputStream();

    String returnedImageId = dockerConnector.buildImage(buildImageParams, progressMonitor);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/build");

    verify(dockerConnection).query(eq("remote"), eq(remote));
    verify(dockerConnection, never()).header("Content-Type", "application/x-compressed-tar");
    verify(dockerConnection, never()).header(eq("Content-Length"), anyInt());
    verify(dockerConnection, never()).entity(any(InputStream.class));

    verify(dockerConnection).header(eq("X-Registry-Config"), nullable(byte[].class));
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedImageId, imageId);
  }

  @Test
  public void shouldBeAbleToBuildImageWithAdditionalQueryParameters()
      throws IOException, InterruptedException {
    AuthConfigs authConfigs = DtoFactory.newDto(AuthConfigs.class);
    AuthConfig authConfig = DtoFactory.newDto(AuthConfig.class);
    Map<String, AuthConfig> auth = new HashMap<>();
    auth.put("auth", authConfig);
    authConfigs.setConfigs(auth);

    final String imageId = "37a7da3b7edc";
    final String repository = "repo/name";
    final String tag = "tag";
    final boolean rm = true;
    final boolean forcerm = true;
    final long memory = 2147483648L;
    final long memswap = 3221225472L;
    final boolean pull = true;
    final String dockerfile = "path/Dockerfile";
    final boolean nocache = true;
    final boolean q = true;
    final String cpusetcpus = "4-5";
    final long cpuperiod = 10000L;
    final long cpuquota = 5000L;
    final Map<String, String> buildargs = new HashMap<>();
    buildargs.put("constraint:label.com!", "value");

    BuildImageParams buildImageParams =
        BuildImageParams.create(this.dockerfile)
            .withAuthConfigs(authConfigs)
            .withRepository(repository)
            .withTag(tag)
            .withRemoveIntermediateContainers(rm)
            .withForceRemoveIntermediateContainers(forcerm)
            .withMemoryLimit(memory)
            .withMemorySwapLimit(memswap)
            .withDoForcePull(pull)
            .withDockerfile(dockerfile)
            .withNoCache(nocache)
            .withQuiet(q)
            .withCpusetCpus(cpusetcpus)
            .withCpuPeriod(cpuperiod)
            .withCpuQuota(cpuquota)
            .withBuildArgs(buildargs);

    doReturn(
            new ByteArrayInputStream(
                ("{\"stream\":\"Successfully built " + imageId + "\"}").getBytes()))
        .when(dockerResponse)
        .getInputStream();

    String returnedImageId = dockerConnector.buildImage(buildImageParams, progressMonitor);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/build");

    verify(dockerConnection).header("Content-Type", "application/x-compressed-tar");
    verify(dockerConnection).header(eq("Content-Length"), anyLong());
    verify(dockerConnection).entity(any(InputStream.class));
    verify(dockerConnection, never()).header(eq("remote"), anyString());

    verify(dockerConnection).header(eq("X-Registry-Config"), nullable(byte[].class));

    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    verify(dockerConnection).query(eq("rm"), eq(1));
    verify(dockerConnection).query(eq("forcerm"), eq(1));
    verify(dockerConnection).query(eq("memory"), eq(memory));
    verify(dockerConnection).query(eq("memswap"), eq(memswap));
    verify(dockerConnection).query(eq("pull"), eq(1));
    verify(dockerConnection).query(eq("dockerfile"), eq(dockerfile));
    verify(dockerConnection).query(eq("nocache"), eq(1));
    verify(dockerConnection).query(eq("q"), eq(1));
    verify(dockerConnection).query(eq("cpusetcpus"), eq(cpusetcpus));
    verify(dockerConnection).query(eq("cpuperiod"), eq(cpuperiod));
    verify(dockerConnection).query(eq("cpuquota"), eq(cpuquota));
    verify(dockerConnection).query(eq("t"), eq(repository + ':' + tag));
    verify(dockerConnection)
        .query(eq("buildargs"), eq(URLEncoder.encode(GSON.toJson(buildargs), "UTF-8")));

    assertEquals(returnedImageId, imageId);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileBuildingImageIfResponseCodeIsNotSuccess()
      throws IOException, InterruptedException {
    AuthConfigs authConfigs = DtoFactory.newDto(AuthConfigs.class);
    AuthConfig authConfig = DtoFactory.newDto(AuthConfig.class);
    Map<String, AuthConfig> auth = new HashMap<>();
    auth.put("auth", authConfig);
    authConfigs.setConfigs(auth);

    BuildImageParams getExecInfoParams =
        BuildImageParams.create(dockerfile).withAuthConfigs(authConfigs);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.buildImage(getExecInfoParams, progressMonitor);

    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp =
        "Docker image build failed. Image id not found in build output."
  )
  public void shouldThrowIOExceptionWhenBuildImageButNoSuccessMessageInResponse()
      throws IOException, InterruptedException {
    AuthConfigs authConfigs = DtoFactory.newDto(AuthConfigs.class);
    AuthConfig authConfig = DtoFactory.newDto(AuthConfig.class);
    Map<String, AuthConfig> auth = new HashMap<>();
    auth.put("auth", authConfig);
    authConfigs.setConfigs(auth);

    BuildImageParams getEventsParams =
        BuildImageParams.create(dockerfile).withAuthConfigs(authConfigs);

    doReturn(new ByteArrayInputStream("c96d378b4911: Already exists".getBytes()))
        .when(dockerResponse)
        .getInputStream();

    dockerConnector.buildImage(getEventsParams, progressMonitor);

    verify(dockerResponse).getInputStream();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = "Docker image build exceed timeout .* seconds."
  )
  public void testThrowsDockerExceptionWhenImageBuildExceedTimeout() throws Exception {
    dockerConnector = newConnectorSpy(0, dockerApiVersionPathPrefixProvider);
    final AuthConfigs authConfigs =
        DtoFactory.newDto(AuthConfigs.class)
            .withConfigs(ImmutableMap.of("auth", DtoFactory.newDto(AuthConfig.class)));

    final BuildImageParams buildImageParams =
        BuildImageParams.create(dockerfile).withAuthConfigs(authConfigs);
    final InputStream slowStream =
        new InputStream() {
          @Override
          public int read() {
            try {
              Thread.sleep(373);
            } catch (Exception ignored) {
            }
            return 43;
          }
        };
    doReturn(slowStream).when(dockerResponse).getInputStream();

    dockerConnector.buildImage(buildImageParams, progressMonitor);
  }

  @Test
  public void shouldCallRemoveImageWithParametersObject() throws IOException {
    RemoveImageParams removeImageParams = RemoveImageParams.create(IMAGE);

    doNothing().when(dockerConnector).removeImage(removeImageParams);

    dockerConnector.removeImage(IMAGE);

    verify(dockerConnector).removeImage((RemoveImageParams) captor.capture());

    assertEquals(captor.getValue(), removeImageParams);
  }

  @Test
  public void shouldBeAbleToRemoveImage() throws IOException {
    RemoveImageParams removeImageParams = RemoveImageParams.create(IMAGE);

    dockerConnector.removeImage(removeImageParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_DELETE);
    verify(dockerConnection).path("/images/" + removeImageParams.getImage());
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileRemovingImageIfResponseCodeIsNotSuccess()
      throws IOException {
    RemoveImageParams removeImageParams = RemoveImageParams.create(IMAGE);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.removeImage(removeImageParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToTagImage() throws IOException {
    TagParams tagParams = TagParams.create(IMAGE, REPOSITORY);

    dockerConnector.tag(tagParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/images/" + tagParams.getImage() + "/tag");
    verify(dockerConnection).query("repo", tagParams.getRepository());
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileTaggingImageIfResponseCodeIsNotSuccess()
      throws IOException {
    TagParams tagParams = TagParams.create(IMAGE, REPOSITORY);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.tag(tagParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToPushImageToRepository() throws IOException, InterruptedException {
    PushParams pushParams = PushParams.create(REPOSITORY);

    when(dockerResponse.getInputStream())
        .thenReturn(
            new ByteArrayInputStream(
                ("{\"status\":\"latest: digest: " + DIGEST + " size: 1234\"}").getBytes()));

    dockerConnector.push(pushParams, progressMonitor);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/images/" + pushParams.getRepository() + "/push");
    verify(dockerConnection).header(eq("X-Registry-Auth"), nullable(AuthConfig.class));
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test
  public void shouldBeAbleToPushImageToRegistryRepository()
      throws IOException, InterruptedException {
    PushParams pushParams = PushParams.create(REPOSITORY).withRegistry(REGISTRY);

    when(dockerResponse.getInputStream())
        .thenReturn(
            new ByteArrayInputStream(
                ("{\"status\":\"latest: digest: " + DIGEST + " size: 1234\"}").getBytes()));

    dockerConnector.push(pushParams, progressMonitor);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection)
        .path("/images/" + pushParams.getRegistry() + '/' + pushParams.getRepository() + "/push");
    verify(dockerConnection).header(eq("X-Registry-Auth"), nullable(AuthConfig.class));
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhilePushingImageIfResponseCodeIsNotSuccess()
      throws IOException, InterruptedException {
    PushParams pushParams = PushParams.create(REPOSITORY);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.push(pushParams, progressMonitor);

    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp =
        "Docker image pushing failed. Cause: Docker push response doesn't contain image digest"
  )
  public void shouldThrowDockerExceptionWhenPushImageButDigestOutputMissing()
      throws IOException, InterruptedException {
    String dockerPushOutput =
        "{\"progress\":\"[=====>              ] 25%\"}\n"
            + "{\"status\":\"Image already exists\"}\n"
            + "{\"progress\":\"[===============>    ] 75%\"}\n";

    when(dockerResponse.getInputStream())
        .thenReturn(new ByteArrayInputStream(dockerPushOutput.getBytes()));
    PushParams pushParams = PushParams.create(REPOSITORY);

    dockerConnector.push(pushParams, progressMonitor);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = "Docker image pushing failed. Cause: .*"
  )
  public void shouldThrowDockerExceptionWhenPushImageButOutputIsEmpty()
      throws IOException, InterruptedException {
    PushParams pushParams = PushParams.create(REPOSITORY);

    dockerConnector.push(pushParams, progressMonitor);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = "Docker image pushing failed. Cause: test error"
  )
  public void shouldThrowDockerExceptionWhenPushFails() throws IOException, InterruptedException {
    String dockerPushOutput =
        "{\"progress\":\"[=====>              ] 25%\"}\n"
            + "{\"progress\":\"[===============>    ] 75%\"}\n"
            + "{\"error\":\"test error\"}\n";

    when(dockerResponse.getInputStream())
        .thenReturn(new ByteArrayInputStream(dockerPushOutput.getBytes()));
    PushParams pushParams = PushParams.create(REPOSITORY);

    dockerConnector.push(pushParams, progressMonitor);
  }

  @Test
  public void shouldBeAbleToParseDigestFromDockerPushOutput()
      throws IOException, InterruptedException {
    String dockerPushOutput =
        "{\"progress\":\"[=====>              ] 25%\"}\n"
            + "{\"status\":\"Image already exists\"}\n"
            + "{\"progress\":\"[===============>    ] 75%\"}\n"
            + "{\"status\":\""
            + TAG
            + ": digest: "
            + DIGEST
            + " size: 12345\"}";

    when(dockerResponse.getInputStream())
        .thenReturn(new ByteArrayInputStream(dockerPushOutput.getBytes()));

    assertEquals(
        DIGEST,
        dockerConnector.push(
            PushParams.create(REPOSITORY).withRegistry(REGISTRY).withTag(TAG), progressMonitor));
  }

  @Test
  public void shouldBeAbleToCommitImage() throws IOException, JsonParseException {
    CommitParams commitParams = CommitParams.create(CONTAINER).withRepository(REPOSITORY);

    ContainerCommitted containerCommitted = mock(ContainerCommitted.class);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_CREATED_CODE);
    when(containerCommitted.getId()).thenReturn(IMAGE);
    doReturn(containerCommitted)
        .when(dockerConnector)
        .parseResponseStreamAndClose(inputStream, ContainerCommitted.class);

    String returnedImage = dockerConnector.commit(commitParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/commit");
    verify(dockerConnection).query("container", commitParams.getContainer());
    verify(dockerConnection).query("repo", commitParams.getRepository());
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedImage, IMAGE);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileCommittingImageIfResponseCodeIsNotSuccess()
      throws IOException {
    CommitParams commitParams = CommitParams.create(CONTAINER).withRepository(REPOSITORY);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.commit(commitParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToPullImageFromRepository() throws IOException, InterruptedException {
    PullParams pullParams = PullParams.create(IMAGE);

    when(dockerResponse.getInputStream()).thenReturn(new ByteArrayInputStream(STREAM_DATA_BYTES));

    dockerConnector.pull(pullParams, progressMonitor);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/images/create");
    verify(dockerConnection).query("fromImage", pullParams.getImage());
    verify(dockerConnection).header(eq("X-Registry-Auth"), nullable(AuthConfig.class));
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test
  public void shouldBeAbleToPullImageCreateRegistryRepository()
      throws IOException, InterruptedException {
    PullParams pullParams = PullParams.create(IMAGE).withRegistry(REGISTRY);

    when(dockerResponse.getInputStream()).thenReturn(new ByteArrayInputStream(STREAM_DATA_BYTES));

    dockerConnector.pull(pullParams, progressMonitor);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/images/create");
    verify(dockerConnection)
        .query("fromImage", pullParams.getRegistry() + '/' + pullParams.getImage());
    verify(dockerConnection).header(eq("X-Registry-Auth"), nullable(AuthConfig.class));
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhilePullingImageIfResponseCodeIsNotSuccess()
      throws IOException, InterruptedException {
    PullParams pullParams = PullParams.create(IMAGE);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.pull(pullParams, progressMonitor);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToCreateContainer() throws IOException, JsonParseException {
    CreateContainerParams createContainerParams =
        CreateContainerParams.create(new ContainerConfig());

    ContainerCreated containerCreated = mock(ContainerCreated.class);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_CREATED_CODE);
    doReturn(containerCreated)
        .when(dockerConnector)
        .parseResponseStreamAndClose(inputStream, ContainerCreated.class);

    ContainerCreated returnedContainerCreated =
        dockerConnector.createContainer(createContainerParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/containers/create");
    verify(dockerConnection).header("Content-Type", MediaType.APPLICATION_JSON);
    verify(dockerConnection).header(eq("Content-Length"), anyInt());
    verify(dockerConnection).entity(any(byte[].class));
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();

    assertEquals(returnedContainerCreated, containerCreated);
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileCreatingContainerIfResponseCodeIsNotSuccess()
      throws IOException {
    CreateContainerParams createContainerParams =
        CreateContainerParams.create(new ContainerConfig());

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.createContainer(createContainerParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToStartContainer() throws IOException {
    StartContainerParams startContainerParams = StartContainerParams.create(CONTAINER);

    dockerConnector.startContainer(startContainerParams);

    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/containers/" + startContainerParams.getContainer() + "/start");
    verify(dockerConnection).request();
    verify(dockerResponse, atLeastOnce()).getStatus();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp = EXCEPTION_ERROR_MESSAGE
  )
  public void shouldThrowDockerExceptionWhileStartingContainerIfResponseCodeIsNotSuccess()
      throws IOException {
    StartContainerParams startContainerParams = StartContainerParams.create(CONTAINER);

    when(dockerResponse.getStatus()).thenReturn(RESPONSE_ERROR_CODE);

    dockerConnector.startContainer(startContainerParams);

    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldUseFirstLetterLowercaseWhenParseResponseStreamAndClose()
      throws IOException, JsonParseException {
    String response =
        "{\n"
            + "  \"BuildTime\": \"2016-03-10T15:54:52.312835708+00:00\",\n"
            + "  \"KernelVersion\": \"3.16.0-53-generic\",\n"
            + "  \"Arch\": \"amd64\",\n"
            + "  \"Os\": \"linux\",\n"
            + "  \"GoVersion\": \"go1.5.3\",\n"
            + "  \"GitCommit\": \"20f81dd\",\n"
            + "  \"ApiVersion\": \"1.22\",\n"
            + "  \"Version\": \"1.10.3\"\n"
            + "}\n";
    Version version = new Version();
    version.setVersion("1.10.3");
    version.setApiVersion("1.22");
    version.setGoVersion("go1.5.3");
    version.setGitCommit("20f81dd");
    version.setOs("linux");
    version.setArch("amd64");

    Version parsedVersion =
        dockerConnector.parseResponseStreamAndClose(
            new ByteArrayInputStream(response.getBytes()), Version.class);

    assertEquals(parsedVersion, version);
  }

  @Test
  public void shouldBeAbleToParseResponseStreamAsListOfImagesAndClose()
      throws IOException, JsonParseException {
    String response =
        "[\n"
            + "  {\n"
            + "     \"RepoTags\": [\n"
            + "       \"ubuntu:12.04\",\n"
            + "       \"ubuntu:precise\",\n"
            + "       \"ubuntu:latest\"\n"
            + "     ],\n"
            + "     \"Id\": \"8dbd9e392a964056420e5d58ca5cc376ef18e2de93b5cc90e868a1bbc8318c1c\",\n"
            + "     \"Created\": 1365714795,\n"
            + "     \"Size\": 131506275,\n"
            + "     \"VirtualSize\": 131506275,\n"
            + "     \"Labels\": {}\n"
            + "  },\n"
            + "  {\n"
            + "     \"RepoTags\": [\n"
            + "       \"ubuntu:12.10\",\n"
            + "       \"ubuntu:quantal\"\n"
            + "     ],\n"
            + "     \"ParentId\": \"27cf784147099545\",\n"
            + "     \"Id\": \"b750fe79269d2ec9a3c593ef05b4332b1d1a02a62b4accb2c21d589ff2f5f2dc\",\n"
            + "     \"Created\": 1364102658,\n"
            + "     \"Size\": 24653,\n"
            + "     \"VirtualSize\": 180116135,\n"
            + "     \"Labels\": {\n"
            + "        \"com.example.version\": \"v1\"\n"
            + "     }\n"
            + "  }\n"
            + "]\n";

    List<Image> images =
        dockerConnector.parseResponseStreamAndClose(
            new ByteArrayInputStream(response.getBytes()), new TypeToken<List<Image>>() {});
    assertEquals(images.size(), 2);
    Image actualImage1 = images.get(0);
    Image actualImage2 = images.get(1);

    assertEquals(
        actualImage1.getRepoTags(),
        new String[] {"ubuntu:12.04", "ubuntu:precise", "ubuntu:latest"});
    assertEquals(
        actualImage1.getId(), "8dbd9e392a964056420e5d58ca5cc376ef18e2de93b5cc90e868a1bbc8318c1c");
    assertEquals(actualImage1.getCreated(), 1365714795);
    assertEquals(actualImage1.getSize(), 131506275);
    assertEquals(actualImage1.getVirtualSize(), 131506275);
    assertEquals(actualImage1.getLabels(), emptyMap());

    assertEquals(actualImage2.getRepoTags(), new String[] {"ubuntu:12.10", "ubuntu:quantal"});
    assertEquals(
        actualImage2.getId(), "b750fe79269d2ec9a3c593ef05b4332b1d1a02a62b4accb2c21d589ff2f5f2dc");
    assertEquals(actualImage2.getParentId(), "27cf784147099545");
    assertEquals(actualImage2.getCreated(), 1364102658);
    assertEquals(actualImage2.getSize(), 24653);
    assertEquals(actualImage2.getVirtualSize(), 180116135);
    assertEquals(actualImage2.getLabels(), singletonMap("com.example.version", "v1"));
  }

  @Test
  public void shouldBeAbleToParseResponseStreamAsListOfContainersAndClose()
      throws IOException, JsonParseException {
    String response =
        " [\n"
            + "         {\n"
            + "                 \"Id\": \"8dfafdbc3a40\",\n"
            + "                 \"Image\": \"ubuntu:latest\",\n"
            + "                 \"Command\": \"echo 1\",\n"
            + "                 \"Created\": 1367854155,\n"
            + "                 \"Status\": \"Exit 0\",\n"
            + "                 \"SizeRw\": 12288,\n"
            + "                 \"SizeRootFs\": 0\n"
            + "         }"
            + "]\n";

    List<ContainerListEntry> containers =
        dockerConnector.parseResponseStreamAndClose(
            new ByteArrayInputStream(response.getBytes()),
            new TypeToken<List<ContainerListEntry>>() {});
    assertEquals(containers.size(), 1);
    ContainerListEntry actualContainer1 = containers.get(0);

    assertEquals(actualContainer1.getId(), "8dfafdbc3a40");
    assertEquals(actualContainer1.getImage(), "ubuntu:latest");
    assertEquals(actualContainer1.getCommand(), "echo 1");
    assertEquals(actualContainer1.getCreated(), 1367854155);
    assertEquals(actualContainer1.getStatus(), "Exit 0");
    assertEquals(actualContainer1.getSizeRw(), 12288);
    assertEquals(actualContainer1.getSizeRootFs(), 0);
  }

  @Test
  public void shouldBeAbleToParseResponseStreamAsEmptyListOfContainersAndClose()
      throws IOException, JsonParseException {
    String response = "[]";

    List<ContainerListEntry> containers =
        dockerConnector.parseResponseStreamAndClose(
            new ByteArrayInputStream(response.getBytes()),
            new TypeToken<List<ContainerListEntry>>() {});
    assertEquals(containers.size(), 0);
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp =
        "Internal server error. Unexpected response body received from Docker."
  )
  public void shouldThrowIOExceptionWhenParseEmptyResponseStringByClass() throws IOException {
    dockerConnector.parseResponseStreamAndClose(
        new ByteArrayInputStream("".getBytes()), Version.class);
  }

  @Test(
    expectedExceptions = IOException.class,
    expectedExceptionsMessageRegExp =
        "Internal server error. Unexpected response body received from Docker."
  )
  public void shouldThrowIOExceptionWhenParseEmptyResponseStringByTypeToken() throws IOException {
    dockerConnector.parseResponseStreamAndClose(
        new ByteArrayInputStream("".getBytes()), new TypeToken<List<ContainerListEntry>>() {});
  }

  @Test
  public void shouldBeAbleToGetNetworks() throws Exception {
    // given
    doReturn(inputStream).when(dockerResponse).getInputStream();
    doReturn(singletonList(createNetwork()))
        .when(dockerConnector)
        .getNetworks(any(GetNetworksParams.class));

    // when
    dockerConnector.getNetworks();

    // then
    verify(dockerConnector).getNetworks(eq(GetNetworksParams.create()));
  }

  @Test
  public void shouldBeAbleToGetNetworksWithParams() throws Exception {
    // given
    Network network = createNetwork();
    List<Network> originNetworks = singletonList(network);
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream(GSON.toJson(originNetworks).getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();
    GetNetworksParams getNetworksParams =
        GetNetworksParams.create().withFilters(new Filters().withFilter("key", "value1", "value2"));

    // when
    List<Network> actual = dockerConnector.getNetworks(getNetworksParams);

    // then
    assertEquals(actual, originNetworks);
    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/networks");
    verify(dockerConnection).query(eq("filters"), any());
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp =
        "Error response from docker API, status: 404, message: exc_message"
  )
  public void shouldThrowExceptionOnGetNetworksIfResponseCodeIsNot20x() throws Exception {
    // given
    doReturn(404).when(dockerResponse).getStatus();
    ByteArrayInputStream inputStream = new ByteArrayInputStream("exc_message".getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();

    // when
    dockerConnector.getNetworks();
  }

  @Test
  public void shouldBeAbleToInspectNetwork() throws Exception {
    // given
    Network network = createNetwork();
    doReturn(network).when(dockerConnector).inspectNetwork(any(InspectNetworkParams.class));

    // when
    dockerConnector.inspectNetwork(network.getId());

    // then
    verify(dockerConnector).inspectNetwork(InspectNetworkParams.create(network.getId()));
  }

  @Test
  public void shouldBeAbleToInspectNetworkWithParams() throws Exception {
    // given
    Network originNetwork = createNetwork();
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream(GSON.toJson(originNetwork).getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();

    // when
    Network actual =
        dockerConnector.inspectNetwork(InspectNetworkParams.create(originNetwork.getId()));

    // then
    assertEquals(actual, originNetwork);
    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/networks/" + originNetwork.getId());
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp =
        "Error response from docker API, status: 404, message: exc_message"
  )
  public void shouldThrowExceptionOnInspectNetworkIfResponseCodeIsNot20x() throws Exception {
    // given
    doReturn(404).when(dockerResponse).getStatus();
    ByteArrayInputStream inputStream = new ByteArrayInputStream("exc_message".getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();

    // when
    dockerConnector.inspectNetwork("net_id");
  }

  @Test
  public void shouldBeAbleToCreateNetwork() throws Exception {
    // given
    CreateNetworkParams createNetworkParams = CreateNetworkParams.create(createNewNetwork());
    NetworkCreated originNetworkCreated =
        new NetworkCreated().withId("some_id").withWarning("some_warning");
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream(GSON.toJson(originNetworkCreated).getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();

    // when
    NetworkCreated networkCreated = dockerConnector.createNetwork(createNetworkParams);

    // then
    assertEquals(networkCreated, originNetworkCreated);
    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/networks/create");
    verify(dockerConnection).header("Content-Type", MediaType.APPLICATION_JSON);
    verify(dockerConnection).header(eq("Content-Length"), anyInt());
    ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(dockerConnection).entity(argumentCaptor.capture());
    assertEquals(
        argumentCaptor.getValue(), GSON.toJson(createNetworkParams.getNetwork()).getBytes());
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp =
        "Error response from docker API, status: 404, message: exc_message"
  )
  public void shouldThrowExceptionOnCreateNetworkIfResponseCodeIsNot20x() throws Exception {
    // given
    doReturn(404).when(dockerResponse).getStatus();
    ByteArrayInputStream inputStream = new ByteArrayInputStream("exc_message".getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();
    CreateNetworkParams createNetworkParams = CreateNetworkParams.create(createNewNetwork());

    // when
    dockerConnector.createNetwork(createNetworkParams);
  }

  @Test
  public void shouldBeAbleToConnectContainerToNetwork() throws Exception {
    // given
    String netId = "net_id";
    String containerId = "container_id";
    doNothing()
        .when(dockerConnector)
        .connectContainerToNetwork(any(ConnectContainerToNetworkParams.class));

    // when
    dockerConnector.connectContainerToNetwork(netId, containerId);

    // then
    verify(dockerConnector)
        .connectContainerToNetwork(
            ConnectContainerToNetworkParams.create(
                netId, new ConnectContainer().withContainer(containerId)));
  }

  @Test
  public void shouldBeAbleToConnectContainerToNetworkWithParams() throws Exception {
    // given
    String netId = "net_id";
    ConnectContainerToNetworkParams connectToNetworkParams =
        ConnectContainerToNetworkParams.create(netId, createConnectContainer());

    // when
    dockerConnector.connectContainerToNetwork(connectToNetworkParams);

    // then
    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/networks/" + netId + "/connect");
    verify(dockerConnection).header("Content-Type", MediaType.APPLICATION_JSON);
    verify(dockerConnection).header(eq("Content-Length"), anyInt());
    ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(dockerConnection).entity(argumentCaptor.capture());
    assertEquals(
        argumentCaptor.getValue(),
        GSON.toJson(connectToNetworkParams.getConnectContainer()).getBytes());
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp =
        "Error response from docker API, status: 404, message: exc_message"
  )
  public void shouldThrowExceptionOnConnectToNetworkIfResponseCodeIsNot20x() throws Exception {
    // given
    doReturn(404).when(dockerResponse).getStatus();
    ByteArrayInputStream inputStream = new ByteArrayInputStream("exc_message".getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();
    ConnectContainerToNetworkParams connectToNetworkParams =
        ConnectContainerToNetworkParams.create("net_id", createConnectContainer());

    // when
    dockerConnector.connectContainerToNetwork(connectToNetworkParams);
  }

  @Test
  public void shouldBeAbleToDisconnectContainerFromNetwork() throws Exception {
    // given
    String netId = "net_id";
    String containerId = "container_id";
    doNothing()
        .when(dockerConnector)
        .disconnectContainerFromNetwork(any(DisconnectContainerFromNetworkParams.class));

    // when
    dockerConnector.disconnectContainerFromNetwork(netId, containerId);

    // then
    verify(dockerConnector)
        .disconnectContainerFromNetwork(
            DisconnectContainerFromNetworkParams.create(
                netId, new DisconnectContainer().withContainer(containerId)));
  }

  @Test
  public void shouldBeAbleToDisconnectContainerFromNetworkWithParams() throws Exception {
    // given
    String netId = "net_id";
    DisconnectContainerFromNetworkParams disconnectFromNetworkParams =
        DisconnectContainerFromNetworkParams.create(
            netId, new DisconnectContainer().withContainer("container_id").withForce(true));

    // when
    dockerConnector.disconnectContainerFromNetwork(disconnectFromNetworkParams);

    // then
    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_POST);
    verify(dockerConnection).path("/networks/" + netId + "/disconnect");
    verify(dockerConnection).header("Content-Type", MediaType.APPLICATION_JSON);
    verify(dockerConnection).header(eq("Content-Length"), anyInt());
    ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(dockerConnection).entity(argumentCaptor.capture());
    assertEquals(
        argumentCaptor.getValue(),
        GSON.toJson(disconnectFromNetworkParams.getDisconnectContainer()).getBytes());
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp =
        "Error response from docker API, status: 404, message: exc_message"
  )
  public void shouldThrowExceptionOnDisconnectFromNetworkIfResponseCodeIsNot20x() throws Exception {
    // given
    doReturn(404).when(dockerResponse).getStatus();
    ByteArrayInputStream inputStream = new ByteArrayInputStream("exc_message".getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();
    DisconnectContainerFromNetworkParams disconnectFromNetworkParams =
        DisconnectContainerFromNetworkParams.create(
            "net_id", new DisconnectContainer().withContainer("container_id").withForce(true));

    // when
    dockerConnector.disconnectContainerFromNetwork(disconnectFromNetworkParams);
  }

  @Test
  public void shouldBeAbleToRemoveNetwork() throws Exception {
    // given
    String netId = "net_id";
    doNothing().when(dockerConnector).removeNetwork(any(RemoveNetworkParams.class));

    // when
    dockerConnector.removeNetwork(netId);

    // then
    verify(dockerConnector).removeNetwork(RemoveNetworkParams.create(netId));
  }

  @Test
  public void shouldBeAbleToRemoveNetworkWithParams() throws Exception {
    // given
    String netId = "net_id";
    RemoveNetworkParams removeNetworkParams = RemoveNetworkParams.create(netId);

    // when
    dockerConnector.removeNetwork(removeNetworkParams);

    // then
    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_DELETE);
    verify(dockerConnection).path("/networks/" + netId);
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
  }

  @Test(
    expectedExceptions = NetworkNotFoundException.class,
    expectedExceptionsMessageRegExp = "exc_message"
  )
  public void shouldThrowExceptionOnRemoveNetworkIfResponseCodeIsNot20x() throws Exception {
    // given
    doReturn(404).when(dockerResponse).getStatus();
    ByteArrayInputStream inputStream = new ByteArrayInputStream("exc_message".getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();
    RemoveNetworkParams removeNetworkParams = RemoveNetworkParams.create("net_id");

    // when
    dockerConnector.removeNetwork(removeNetworkParams);
  }

  @Test(
    expectedExceptions = VolumeNotFoundException.class,
    expectedExceptionsMessageRegExp = "exc_message"
  )
  public void shouldThrowExceptionOnRemoveVolumeIfResponseCodeIsNot20x() throws Exception {
    // given
    doReturn(404).when(dockerResponse).getStatus();
    ByteArrayInputStream inputStream = new ByteArrayInputStream("exc_message".getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();
    RemoveVolumeParams removeVolumeParams = RemoveVolumeParams.create("volumeName");

    // when
    dockerConnector.removeVolume(removeVolumeParams);
  }

  @Test
  public void shouldBeAbleToRemoveVolume() throws Exception {
    // given
    String volumeName = "net_id";
    doNothing().when(dockerConnector).removeVolume(any(RemoveVolumeParams.class));

    // when
    dockerConnector.removeVolume(volumeName);

    // then
    verify(dockerConnector).removeVolume(volumeName);
  }

  @Test
  public void shouldBeAbleToRemoveVolumeWithParams() throws Exception {
    // given
    String volumeName = "volume";
    RemoveVolumeParams removeVolumeParams = RemoveVolumeParams.create(volumeName);

    // when
    dockerConnector.removeVolume(removeVolumeParams);

    // then
    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_DELETE);
    verify(dockerConnection).path("/volumes/" + volumeName);
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
  }

  @Test
  public void shouldBeAbleToGetVolumes() throws Exception {
    // given
    doReturn(inputStream).when(dockerResponse).getInputStream();
    doReturn(createVolumes()).when(dockerConnector).getVolumes(any(GetVolumesParams.class));

    // when
    dockerConnector.getVolumes();

    // then
    verify(dockerConnector).getVolumes(eq(GetVolumesParams.create()));
  }

  @Test
  public void shouldBeAbleToGetVolumesWithParams() throws Exception {
    // given
    Volumes originVolumes = createVolumes();
    ByteArrayInputStream inputStream =
        new ByteArrayInputStream(GSON.toJson(originVolumes).getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();
    GetVolumesParams getVolumesParams =
        GetVolumesParams.create().withFilters(new Filters().withFilter("key", "value1", "value2"));

    // when
    Volumes actual = dockerConnector.getVolumes(getVolumesParams);

    // then
    assertEquals(actual, originVolumes);
    verify(dockerConnectionFactory).openConnection(nullable(URI.class));
    verify(dockerConnection).method(REQUEST_METHOD_GET);
    verify(dockerConnection).path("/volumes");
    verify(dockerConnection).query(eq("filters"), any());
    verify(dockerConnection).request();
    verify(dockerResponse).getStatus();
    verify(dockerResponse).getInputStream();
  }

  @Test(
    expectedExceptions = DockerException.class,
    expectedExceptionsMessageRegExp =
        "Error response from docker API, status: 404, message: exc_message"
  )
  public void shouldThrowExceptionOnGetVolumesIfResponseCodeIsNot20x() throws Exception {
    // given
    doReturn(404).when(dockerResponse).getStatus();
    ByteArrayInputStream inputStream = new ByteArrayInputStream("exc_message".getBytes());
    doReturn(inputStream).when(dockerResponse).getInputStream();

    // when
    dockerConnector.getVolumes();
  }

  private Network createNetwork() {
    return new Network()
        .withName("some_name")
        .withLabels(singletonMap("label1", "val1"))
        .withIPAM(
            new Ipam()
                .withConfig(
                    singletonList(
                        new IpamConfig()
                            .withGateway("some_gateway")
                            .withIPRange("some_ip_range")
                            .withSubnet("some_subnet")))
                .withDriver("some_driver2")
                .withOptions(singletonMap("opt1", "val1")))
        .withInternal(true)
        .withScope("some_scope")
        .withId("some_id")
        .withContainers(
            singletonMap(
                "some_container_key",
                new ContainerInNetwork()
                    .withEndpointID("some_endpoint_id")
                    .withIPv4Address("some_ipv4_address")
                    .withIPv6Address("some_ipv6_address")
                    .withMacAddress("some_mac_address")
                    .withName("some_container_name")))
        .withDriver("some_driver")
        .withEnableIPv6(true)
        .withOptions(singletonMap("opt2", "val1"));
  }

  private NewNetwork createNewNetwork() {
    return new NewNetwork()
        .withCheckDuplicate(true)
        .withDriver("some_driver")
        .withName("some_name")
        .withEnableIPv6(true)
        .withInternal(true)
        .withIPAM(
            new Ipam()
                .withConfig(
                    singletonList(
                        new IpamConfig()
                            .withGateway("some_gateway")
                            .withIPRange("some_ip_range")
                            .withSubnet("some_subnet")))
                .withDriver("some_driver2")
                .withOptions(singletonMap("opt1", "val1")))
        .withLabels(singletonMap("label1", "val1"))
        .withOptions(singletonMap("opt1", "val1"));
  }

  private ConnectContainer createConnectContainer() {
    return new ConnectContainer()
        .withContainer("container_id")
        .withEndpointConfig(
            new EndpointConfig()
                .withLinks(new String[] {"link_1"})
                .withAliases("alias_1")
                .withIPAMConfig(
                    new NewIpamConfig()
                        .withIPv4Address("ipv4_address")
                        .withIPv6Address("ipv6_address")));
  }

  private Volumes createVolumes() {
    return new Volumes()
        .withVolumes(
            asList(
                new Volume().withName("volume1"),
                new Volume().withName("volume2").withDriver("driver1")));
  }

  private DockerConnector newConnectorSpy(
      int buildTimeout, DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider) {
    return spy(
        new DockerConnector(
            buildTimeout,
            dockerConnectorConfiguration,
            dockerConnectionFactory,
            authManager,
            dockerApiVersionPathPrefixProvider));
  }
}
