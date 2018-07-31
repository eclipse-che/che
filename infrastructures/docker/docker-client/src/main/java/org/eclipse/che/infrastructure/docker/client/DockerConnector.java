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
package org.eclipse.che.infrastructure.docker.client;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NOT_MODIFIED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

import com.google.common.io.CharStreams;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.TarUtils;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.infrastructure.docker.client.connection.CloseConnectionInputStream;
import org.eclipse.che.infrastructure.docker.client.connection.DockerConnection;
import org.eclipse.che.infrastructure.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.infrastructure.docker.client.connection.DockerResponse;
import org.eclipse.che.infrastructure.docker.client.exception.ContainerNotFoundException;
import org.eclipse.che.infrastructure.docker.client.exception.DockerException;
import org.eclipse.che.infrastructure.docker.client.exception.ExecNotFoundException;
import org.eclipse.che.infrastructure.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.infrastructure.docker.client.exception.NetworkNotFoundException;
import org.eclipse.che.infrastructure.docker.client.exception.VolumeNotFoundException;
import org.eclipse.che.infrastructure.docker.client.json.ContainerCommitted;
import org.eclipse.che.infrastructure.docker.client.json.ContainerCreated;
import org.eclipse.che.infrastructure.docker.client.json.ContainerExitStatus;
import org.eclipse.che.infrastructure.docker.client.json.ContainerInfo;
import org.eclipse.che.infrastructure.docker.client.json.ContainerListEntry;
import org.eclipse.che.infrastructure.docker.client.json.ContainerProcesses;
import org.eclipse.che.infrastructure.docker.client.json.Event;
import org.eclipse.che.infrastructure.docker.client.json.ExecConfig;
import org.eclipse.che.infrastructure.docker.client.json.ExecCreated;
import org.eclipse.che.infrastructure.docker.client.json.ExecInfo;
import org.eclipse.che.infrastructure.docker.client.json.ExecStart;
import org.eclipse.che.infrastructure.docker.client.json.Filters;
import org.eclipse.che.infrastructure.docker.client.json.Image;
import org.eclipse.che.infrastructure.docker.client.json.ImageInfo;
import org.eclipse.che.infrastructure.docker.client.json.NetworkCreated;
import org.eclipse.che.infrastructure.docker.client.json.ProgressStatus;
import org.eclipse.che.infrastructure.docker.client.json.SystemInfo;
import org.eclipse.che.infrastructure.docker.client.json.Version;
import org.eclipse.che.infrastructure.docker.client.json.network.ConnectContainer;
import org.eclipse.che.infrastructure.docker.client.json.network.DisconnectContainer;
import org.eclipse.che.infrastructure.docker.client.json.network.Network;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for docker API.
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 * @author Anton Korneta
 * @author Mykola Morhun
 * @author Alexander Andrienko
 */
@Singleton
public class DockerConnector {
  private static final Logger LOG = LoggerFactory.getLogger(DockerConnector.class);
  // Docker uses uppercase in first letter in names of json objects, e.g. {"Id":"123"} instead of
  // {"id":"123"}
  protected static final Gson GSON =
      new GsonBuilder()
          .disableHtmlEscaping()
          .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
          .create();

  private final long imageBuildTimeoutSec;
  private final URI dockerDaemonUri;
  private final DockerRegistryAuthResolver authResolver;
  private final ExecutorService executor;
  private final DockerConnectionFactory connectionFactory;

  protected final String apiVersionPathPrefix;

  @Inject
  public DockerConnector(
      @Named("che.infra.docker.build_timeout_sec") long imageBuildTimeoutSec,
      DockerConnectorConfiguration connectorConfiguration,
      DockerConnectionFactory connectionFactory,
      DockerRegistryAuthResolver authResolver,
      DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider) {
    this.imageBuildTimeoutSec = imageBuildTimeoutSec;
    this.dockerDaemonUri = connectorConfiguration.getDockerDaemonUri();
    this.connectionFactory = connectionFactory;
    this.authResolver = authResolver;
    this.apiVersionPathPrefix = dockerApiVersionPathPrefixProvider.get();
    executor =
        Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .setNameFormat("DockerApiConnector-%d")
                .setDaemon(true)
                .build());
  }

  /**
   * Gets system-wide information.
   *
   * @return system-wide information
   * @throws IOException when a problem occurs with docker api calls
   */
  public SystemInfo getSystemInfo() throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/info")) {
      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), SystemInfo.class);
    }
  }

  /**
   * Gets docker version.
   *
   * @return information about version docker
   * @throws IOException when a problem occurs with docker api calls
   */
  public Version getVersion() throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/version")) {
      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), Version.class);
    }
  }

  /**
   * Lists all final layer docker images.
   *
   * @return list of docker images
   * @throws IOException when a problem occurs with docker api calls
   */
  public List<Image> listImages() throws IOException {
    return listImages(ListImagesParams.create());
  }

  /**
   * Lists docker images.
   *
   * @return list of docker images
   * @throws IOException when a problem occurs with docker api calls
   */
  public List<Image> listImages(ListImagesParams params) throws IOException {
    final Filters filters = params.getFilters();

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/images/json")) {
      addQueryParamIfNotNull(connection, "all", params.getAll());
      addQueryParamIfNotNull(connection, "digests", params.getAll());
      if (filters != null) {
        connection.query("filters", urlPathSegmentEscaper().escape(toJson(filters.getFilters())));
      }
      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(
          response.getInputStream(), new TypeToken<List<Image>>() {});
    }
  }

  /**
   * Method returns list of docker containers, include non-running ones.
   *
   * @throws IOException when problems occurs with docker api calls
   */
  public List<ContainerListEntry> listContainers() throws IOException {
    return listContainers(ListContainersParams.create().withAll(true));
  }

  /**
   * Method returns list of docker containers which was filtered by {@link ListContainersParams}
   *
   * @throws IOException when problems occurs with docker api calls
   */
  public List<ContainerListEntry> listContainers(ListContainersParams params) throws IOException {
    final Filters filters = params.getFilters();

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/containers/json")) {
      addQueryParamIfNotNull(connection, "all", params.isAll());
      addQueryParamIfNotNull(connection, "size", params.isSize());
      addQueryParamIfNotNull(connection, "limit", params.getLimit());
      addQueryParamIfNotNull(connection, "since", params.getSince());
      addQueryParamIfNotNull(connection, "before", params.getBefore());
      if (filters != null) {
        connection.query("filters", urlPathSegmentEscaper().escape(toJson(filters.getFilters())));
      }
      DockerResponse response = connection.request();
      final int status = response.getStatus();
      if (OK.getStatusCode() != status) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(
          response.getInputStream(), new TypeToken<List<ContainerListEntry>>() {});
    }
  }

  /**
   * Gets detailed information about docker image.
   *
   * @param image id or full repository name of docker image
   * @return detailed information about {@code image}
   * @throws IOException
   */
  public ImageInfo inspectImage(String image) throws IOException {
    return inspectImage(InspectImageParams.create(image));
  }

  /**
   * Gets detailed information about docker image.
   *
   * @return detailed information about {@code image}
   * @throws ImageNotFoundException when docker api return 404 status
   * @throws IOException when a problem occurs with docker api calls
   */
  public ImageInfo inspectImage(InspectImageParams params) throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/images/" + params.getImage() + "/json")) {
      final DockerResponse response = connection.request();
      final int status = response.getStatus();
      if (status == NOT_FOUND.getStatusCode()) {
        throw new ImageNotFoundException(readAndCloseQuietly(response.getInputStream()));
      }
      if (OK.getStatusCode() != status) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), ImageInfo.class);
    }
  }

  /**
   * Stops container.
   *
   * @throws IOException when a problem occurs with docker api calls
   */
  public void stopContainer(final StopContainerParams params) throws IOException {
    final Long timeout =
        (params.getTimeout() == null)
            ? null
            : (params.getTimeunit() == null)
                ? params.getTimeout()
                : params.getTimeunit().toSeconds(params.getTimeout());

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/containers/" + params.getContainer() + "/stop")) {
      addQueryParamIfNotNull(connection, "t", timeout);
      final DockerResponse response = connection.request();
      if (response.getStatus() / 100 != 2) {
        throw getDockerException(response);
      }
    }
  }

  /**
   * Sends specified signal to running container. If signal not set, then SIGKILL will be used.
   *
   * @throws IOException when a problem occurs with docker api calls
   */
  public void killContainer(final KillContainerParams params) throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/containers/" + params.getContainer() + "/kill")) {
      addQueryParamIfNotNull(connection, "signal", params.getSignal());
      final DockerResponse response = connection.request();
      if (NO_CONTENT.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
    }
  }

  /**
   * Kills container with SIGKILL signal.
   *
   * @param container container identifier, either id or name
   * @throws IOException
   */
  public void killContainer(String container) throws IOException {
    killContainer(KillContainerParams.create(container));
  }

  /**
   * Removes docker container.
   *
   * @throws IOException when a problem occurs with docker api calls
   */
  public void removeContainer(final RemoveContainerParams params) throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("DELETE")
            .path(apiVersionPathPrefix + "/containers/" + params.getContainer())) {
      addQueryParamIfNotNull(connection, "force", params.isForce());
      addQueryParamIfNotNull(connection, "v", params.isRemoveVolumes());
      final DockerResponse response = connection.request();
      if (NO_CONTENT.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
    }
  }

  /**
   * Blocks until {@code container} stops, then returns the exit code
   *
   * @param container container identifier, either id or name
   * @return exit code
   * @throws IOException
   */
  public int waitContainer(String container) throws IOException {
    return waitContainer(WaitContainerParams.create(container));
  }

  /**
   * Blocks until container stops, then returns the exit code
   *
   * @return exit code
   * @throws IOException when a problem occurs with docker api calls
   */
  public int waitContainer(final WaitContainerParams params) throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/containers/" + params.getContainer() + "/wait")) {
      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), ContainerExitStatus.class)
          .getStatusCode();
    }
  }

  /**
   * Gets detailed information about docker container.
   *
   * @param container id of container
   * @return detailed information about {@code container}
   * @throws IOException
   */
  public ContainerInfo inspectContainer(String container) throws IOException {
    return inspectContainer(InspectContainerParams.create(container));
  }

  /**
   * Gets detailed information about docker container.
   *
   * @return detailed information about {@code container}
   * @throws ContainerNotFoundException when container not found by docker (docker api returns 404)
   * @throws IOException when a problem occurs with docker api calls
   */
  public ContainerInfo inspectContainer(final InspectContainerParams params) throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/containers/" + params.getContainer() + "/json")) {
      addQueryParamIfNotNull(connection, "size", params.isReturnContainerSize());
      final DockerResponse response = connection.request();
      final int status = response.getStatus();
      if (status == NOT_FOUND.getStatusCode()) {
        throw new ContainerNotFoundException(readAndCloseQuietly(response.getInputStream()));
      }
      if (OK.getStatusCode() != status) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), ContainerInfo.class);
    }
  }

  /**
   * Attaches to the container with specified id. <br>
   * Note, that if @{code stream} parameter is {@code true} then get 'live' stream from container.
   * Typically need to run this method in separate thread, if {@code stream} is {@code true} since
   * this method blocks until container is running.
   *
   * @param containerLogsProcessor output for container logs
   * @throws IOException when a problem occurs with docker api calls
   */
  public void attachContainer(
      final AttachContainerParams params, MessageProcessor<LogMessage> containerLogsProcessor)
      throws IOException {
    final Boolean stream = params.isStream();

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/containers/" + params.getContainer() + "/attach")
            .query("stdout", 1)
            .query("stderr", 1)) {
      addQueryParamIfNotNull(connection, "stream", stream);
      addQueryParamIfNotNull(connection, "logs", stream);
      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
      try (InputStream responseStream = response.getInputStream()) {
        new LogMessagePumper(responseStream, containerLogsProcessor).start();
      }
    }
  }

  /**
   * Get stdout and stderr logs from container.
   *
   * @param containerLogsProcessor output for container logs
   * @throws ContainerNotFoundException when container not found by docker (docker api returns 404)
   * @throws IOException when a problem occurs with docker api calls
   */
  public void getContainerLogs(
      final GetContainerLogsParams params, MessageProcessor<LogMessage> containerLogsProcessor)
      throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/containers/" + params.getContainer() + "/logs")
            .query("stdout", 1)
            .query("stderr", 1)) {
      addQueryParamIfNotNull(connection, "details", params.isDetails());
      addQueryParamIfNotNull(connection, "follow", params.isFollow());
      addQueryParamIfNotNull(connection, "since", params.getSince());
      addQueryParamIfNotNull(connection, "timestamps", params.isTimestamps());
      addQueryParamIfNotNull(connection, "tail", params.getTail());

      final DockerResponse response = connection.request();
      final int status = response.getStatus();
      if (status == NOT_FOUND.getStatusCode()) {
        throw new ContainerNotFoundException(readAndCloseQuietly(response.getInputStream()));
      }
      if (status != OK.getStatusCode()) {
        throw getDockerException(response);
      }

      try (InputStream responseStream = response.getInputStream()) {
        new LogMessagePumper(responseStream, containerLogsProcessor).start();
      }
    }
  }

  /**
   * Sets up an exec instance in a running container.
   *
   * @return just created exec info
   * @throws IOException when a problem occurs with docker api calls
   */
  public Exec createExec(final CreateExecParams params) throws IOException {
    final ExecConfig execConfig =
        new ExecConfig()
            .withCmd(params.getCmd())
            .withUser(params.getUser())
            .withAttachStderr(params.isDetach() == Boolean.FALSE)
            .withAttachStdout(params.isDetach() == Boolean.FALSE);
    byte[] entityBytesArray = toJson(execConfig).getBytes(StandardCharsets.UTF_8);

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/containers/" + params.getContainer() + "/exec")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Content-Length", entityBytesArray.length)
            .entity(entityBytesArray)) {
      final DockerResponse response = connection.request();
      if (response.getStatus() / 100 != 2) {
        throw getDockerException(response);
      }
      return new Exec(
          params.getCmd(),
          parseResponseStreamAndClose(response.getInputStream(), ExecCreated.class).getId());
    }
  }

  /**
   * Starts a previously set up exec instance.
   *
   * @param execOutputProcessor processor for exec output
   * @throws ExecNotFoundException when exec not found by docker (docker api returns 404)
   * @throws IOException when a problem occurs with docker api calls
   */
  public void startExec(
      final StartExecParams params, @Nullable MessageProcessor<LogMessage> execOutputProcessor)
      throws IOException {
    final ExecStart execStart =
        new ExecStart()
            .withDetach(params.isDetach() == Boolean.TRUE)
            .withTty(params.isTty() == Boolean.TRUE);

    byte[] entityBytesArray = toJson(execStart).getBytes(StandardCharsets.UTF_8);
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/exec/" + params.getExecId() + "/start")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Content-Length", entityBytesArray.length)
            .entity(entityBytesArray)) {
      final DockerResponse response = connection.request();
      final int status = response.getStatus();
      if (status == NOT_FOUND.getStatusCode()) {
        throw new ExecNotFoundException(readAndCloseQuietly(response.getInputStream()));
      }
      // According to last doc
      // (https://docs.docker.com/reference/api/docker_remote_api_v1.15/#exec-start) status must be
      // 201 but
      // in fact docker API returns 200 or 204 status.
      if (status / 100 != 2) {
        throw getDockerException(response);
      }
      if (status != NO_CONTENT.getStatusCode() && execOutputProcessor != null) {
        try (InputStream responseStream = response.getInputStream()) {
          new LogMessagePumper(responseStream, execOutputProcessor).start();
        }
      }
    }
  }

  /**
   * Gets detailed information about exec
   *
   * @return detailed information about {@code execId}
   * @throws IOException
   */
  public ExecInfo getExecInfo(String execId) throws IOException {
    return getExecInfo(GetExecInfoParams.create(execId));
  }

  /**
   * Gets detailed information about exec
   *
   * @return detailed information about {@code execId}
   * @throws IOException when a problem occurs with docker api calls
   */
  public ExecInfo getExecInfo(final GetExecInfoParams params) throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/exec/" + params.getExecId() + "/json")) {
      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), ExecInfo.class);
    } catch (Exception e) {
      throw new IOException(e.getLocalizedMessage(), e);
    }
  }

  /**
   * List processes running inside the container.
   *
   * @return processes running inside the container
   * @throws IOException when a problem occurs with docker api calls
   */
  public ContainerProcesses top(final TopParams params) throws IOException {
    final String[] psArgs = params.getPsArgs();

    try (final DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/containers/" + params.getContainer() + "/top")) {
      if (psArgs != null && psArgs.length != 0) {
        StringBuilder psArgsQueryBuilder = new StringBuilder();
        for (int i = 0, l = psArgs.length; i < l; i++) {
          if (i > 0) {
            psArgsQueryBuilder.append('+');
          }
          psArgsQueryBuilder.append(URLEncoder.encode(psArgs[i], "UTF-8"));
        }
        connection.query("ps_args", psArgsQueryBuilder.toString());
      }

      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), ContainerProcesses.class);
    }
  }

  /**
   * Gets files from the specified container.
   *
   * @return stream of resources from the specified container filesystem, with retention connection
   * @throws IOException when a problem occurs with docker api calls
   * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8.0 version
   */
  public InputStream getResource(final GetResourceParams params) throws IOException {
    DockerConnection connection = null;
    try {
      connection =
          connectionFactory
              .openConnection(dockerDaemonUri)
              .method("GET")
              .path(apiVersionPathPrefix + "/containers/" + params.getContainer() + "/archive")
              .query("path", params.getSourcePath());

      final DockerResponse response = connection.request();
      if (response.getStatus() != OK.getStatusCode()) {
        throw getDockerException(response);
      }
      return new CloseConnectionInputStream(response.getInputStream(), connection);
    } catch (IOException io) {
      connection.close();
      throw io;
    }
  }

  /**
   * Puts files into specified container.
   *
   * @throws IOException when a problem occurs with docker api calls, or during file system
   *     operations
   * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8 version
   */
  public void putResource(final PutResourceParams params) throws IOException {
    File tarFile;
    long length;
    try (InputStream sourceData = params.getSourceStream()) {
      // TODO according to http spec http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.4,
      // it is possible to send request without specifying content length if chunked encoding header
      // is set
      // Investigate is it possible to write the stream to request directly

      // we save stream to file, because we have to know its length
      Path tarFilePath = Files.createTempFile("compressed-resources", ".tar");
      tarFile = tarFilePath.toFile();
      length = Files.copy(sourceData, tarFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    try (InputStream tarStream = new BufferedInputStream(new FileInputStream(tarFile));
        DockerConnection connection =
            connectionFactory
                .openConnection(dockerDaemonUri)
                .method("PUT")
                .path(apiVersionPathPrefix + "/containers/" + params.getContainer() + "/archive")
                .query("path", params.getTargetPath())
                .header("Content-Type", ExtMediaType.APPLICATION_X_TAR)
                .header("Content-Length", length)
                .entity(tarStream)) {
      addQueryParamIfNotNull(connection, "noOverwriteDirNonDir", params.isNoOverwriteDirNonDir());
      final DockerResponse response = connection.request();
      if (response.getStatus() != OK.getStatusCode()) {
        throw getDockerException(response);
      }
    } finally {
      FileCleaner.addFile(tarFile);
    }
  }

  /**
   * Get docker events. Parameter {@code untilSecond} does nothing if {@code sinceSecond} is 0.<br>
   * If {@code untilSecond} and {@code sinceSecond} are 0 method gets new events only (streaming
   * mode).<br>
   * If {@code untilSecond} and {@code sinceSecond} are not 0 (but less that current date) methods
   * get events that were generated between specified dates.<br>
   * If {@code untilSecond} is 0 but {@code sinceSecond} is not method gets old events and streams
   * new ones.<br>
   * If {@code sinceSecond} is 0 no old events will be got.<br>
   * With some connection implementations method can fail due to connection timeout in streaming
   * mode.
   *
   * @param messageProcessor processor of all found events that satisfy specified parameters
   * @throws IOException when a problem occurs with docker api calls
   */
  public void getEvents(final GetEventsParams params, MessageProcessor<Event> messageProcessor)
      throws IOException {
    final Filters filters = params.getFilters();

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/events")) {
      addQueryParamIfNotNull(connection, "since", params.getSinceSecond());
      addQueryParamIfNotNull(connection, "until", params.getUntilSecond());
      if (filters != null) {
        connection.query("filters", urlPathSegmentEscaper().escape(toJson(filters.getFilters())));
      }
      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }

      try (InputStream responseStream = response.getInputStream()) {
        new MessagePumper<>(new JsonMessageReader<>(responseStream, Event.class), messageProcessor)
            .start();
      }
    }
  }

  /**
   * Builds new image.
   *
   * @param progressMonitor ProgressMonitor for images creation process
   * @return image id
   * @throws IOException
   */
  public String buildImage(final BuildImageParams params, final ProgressMonitor progressMonitor)
      throws IOException {

    if (params.getRemote() != null) {
      // build context provided by remote URL
      DockerConnection dockerConnection =
          connectionFactory.openConnection(dockerDaemonUri).query("remote", params.getRemote());
      return buildImage(dockerConnection, params, progressMonitor);
    }

    // build context is set of files
    final File tar = Files.createTempFile(null, ".tar").toFile();
    try {
      File[] files = new File[params.getFiles().size()];
      files = params.getFiles().toArray(files);
      createTarArchive(tar, files);
      try (InputStream tarInput = new FileInputStream(tar)) {
        DockerConnection dockerConnection =
            connectionFactory
                .openConnection(dockerDaemonUri)
                .header("Content-Type", "application/x-compressed-tar")
                .header("Content-Length", tar.length())
                .entity(tarInput);
        return buildImage(dockerConnection, params, progressMonitor);
      }
    } finally {
      FileCleaner.addFile(tar);
    }
  }

  private String buildImage(
      final DockerConnection dockerConnection,
      final BuildImageParams params,
      final ProgressMonitor progressMonitor)
      throws IOException {
    final String repository = params.getRepository();

    try (DockerConnection connection =
        dockerConnection
            .method("POST")
            .path(apiVersionPathPrefix + "/build")
            .header(
                "X-Registry-Config",
                authResolver.getXRegistryConfigHeaderValue(params.getAuthConfigs()))) {
      addQueryParamIfNotNull(connection, "rm", params.isRemoveIntermediateContainer());
      addQueryParamIfNotNull(connection, "forcerm", params.isForceRemoveIntermediateContainers());
      addQueryParamIfNotNull(connection, "memory", params.getMemoryLimit());
      addQueryParamIfNotNull(connection, "memswap", params.getMemorySwapLimit());
      addQueryParamIfNotNull(connection, "pull", params.isDoForcePull());
      addQueryParamIfNotNull(connection, "dockerfile", params.getDockerfile());
      addQueryParamIfNotNull(connection, "nocache", params.isNoCache());
      addQueryParamIfNotNull(connection, "q", params.isQuiet());
      addQueryParamIfNotNull(connection, "cpusetcpus", params.getCpusetCpus());
      addQueryParamIfNotNull(connection, "cpuperiod", params.getCpuPeriod());
      addQueryParamIfNotNull(connection, "cpuquota", params.getCpuQuota());
      if (params.getTag() == null) {
        addQueryParamIfNotNull(connection, "t", repository);
      } else {
        addQueryParamIfNotNull(
            connection, "t", repository == null ? null : repository + ':' + params.getTag());
      }
      if (params.getBuildArgs() != null) {
        addQueryParamIfNotNull(
            connection,
            "buildargs",
            URLEncoder.encode(GSON.toJson(params.getBuildArgs()), "UTF-8"));
      }

      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }

      try (InputStream responseStream = response.getInputStream()) {
        JsonMessageReader<ProgressStatus> progressReader =
            new JsonMessageReader<>(responseStream, ProgressStatus.class);

        // Here do some trick to be able interrupt output streaming process.
        // Current unix socket implementation of DockerConnection doesn't react to interruption.
        // So to be able to close unix socket connection and free resources we use main thread.
        // In case of any exception main thread cancels future and close connection.
        // If Docker connection implementation supports interrupting it will stop streaming on
        // interruption,
        // if not it will be stopped by closure of unix socket
        Future<String> imageIdFuture =
            executor.submit(
                () -> {
                  ProgressStatus progressStatus;
                  while ((progressStatus = progressReader.next()) != null) {
                    if (progressStatus.getError() != null) {
                      String errorMessage = progressStatus.getError();
                      if (errorMessage.matches("Error: image .+ not found")) {
                        throw new ImageNotFoundException(errorMessage);
                      }
                    }
                    final String buildImageId = getBuildImageId(progressStatus);
                    if (buildImageId != null) {
                      return buildImageId;
                    }
                    progressMonitor.updateProgress(progressStatus);
                  }

                  throw new DockerException(
                      "Docker image build failed. Image id not found in build output.", 500);
                });

        return imageIdFuture.get(imageBuildTimeoutSec, TimeUnit.SECONDS);
      } catch (ExecutionException e) {
        // unwrap exception thrown by task with .getCause()
        if (e.getCause() instanceof ImageNotFoundException) {
          throw new ImageNotFoundException(e.getCause().getLocalizedMessage());
        } else {
          throw new DockerException(e.getCause().getLocalizedMessage(), 500);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new DockerException("Docker image build was interrupted", 500);
      } catch (TimeoutException ex) {
        throw new DockerException(
            format("Docker image build exceed timeout %s seconds.", imageBuildTimeoutSec), 500);
      }
    }
  }

  /**
   * Removes docker image.
   *
   * @param image image identifier, either id or name
   * @throws IOException when a problem occurs with docker api calls
   */
  public void removeImage(String image) throws IOException {
    removeImage(RemoveImageParams.create(image));
  }

  /**
   * Removes docker image.
   *
   * @throws IOException when a problem occurs with docker api calls
   */
  public void removeImage(final RemoveImageParams params) throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("DELETE")
            .path(apiVersionPathPrefix + "/images/" + params.getImage())) {
      addQueryParamIfNotNull(connection, "force", params.isForce());
      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
    }
  }

  /**
   * Tag the docker image into a repository.
   *
   * @throws ImageNotFoundException when docker api return 404 status
   * @throws IOException when a problem occurs with docker api calls
   */
  public void tag(final TagParams params) throws ImageNotFoundException, IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/images/" + params.getImage() + "/tag")
            .query("repo", params.getRepository())) {
      addQueryParamIfNotNull(connection, "force", params.isForce());
      addQueryParamIfNotNull(connection, "tag", params.getTag());
      final DockerResponse response = connection.request();
      final int status = response.getStatus();
      if (status == NOT_FOUND.getStatusCode()) {
        throw new ImageNotFoundException(readAndCloseQuietly(response.getInputStream()));
      }
      if (status / 100 != 2) {
        throw getDockerException(response);
      }
    }
  }

  /**
   * Push docker image to the registry.
   *
   * @param progressMonitor ProgressMonitor for images pushing process
   * @return digest of just pushed image
   * @throws IOException when a problem occurs with docker api calls
   */
  public String push(final PushParams params, final ProgressMonitor progressMonitor)
      throws IOException {
    final String fullRepo = params.getFullRepo();

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/images/" + fullRepo + "/push")
            .header(
                "X-Registry-Auth",
                authResolver.getXRegistryAuthHeaderValue(
                    params.getRegistry(), params.getAuthConfigs()))) {
      addQueryParamIfNotNull(connection, "tag", params.getTag());
      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }

      try (InputStream responseStream = response.getInputStream()) {
        JsonMessageReader<ProgressStatus> progressReader =
            new JsonMessageReader<>(responseStream, ProgressStatus.class);

        // Here do some trick to be able interrupt output streaming process.
        // Current unix socket implementation of DockerConnection doesn't react to interruption.
        // So to be able to close unix socket connection and free resources we use main thread.
        // In case of any exception main thread cancels future and close connection.
        // If Docker connection implementation supports interrupting it will stop streaming on
        // interruption,
        // if not it will be stopped by closure of unix socket
        Future<String> digestFuture =
            executor.submit(
                () -> {
                  String digestPrefix = firstNonNull(params.getTag(), "latest") + ": digest: ";
                  ProgressStatus progressStatus;
                  while ((progressStatus = progressReader.next()) != null) {
                    progressMonitor.updateProgress(progressStatus);
                    if (progressStatus.getError() != null) {
                      throw new DockerException(progressStatus.getError(), 500);
                    }
                    String status = progressStatus.getStatus();
                    // Here we find string with digest which has following format:
                    // <tag>: digest: <digest> size: <size>
                    // for example:
                    // latest: digest:
                    // sha256:9a70e6222ded459fde37c56af23887467c512628eb8e78c901f3390e49a800a0 size:
                    // 62189
                    if (status != null && status.startsWith(digestPrefix)) {
                      return status.substring(
                          digestPrefix.length(), status.indexOf(" ", digestPrefix.length()));
                    }
                  }

                  LOG.error(
                      "Docker image {}:{} was successfully pushed, but its digest wasn't obtained",
                      fullRepo,
                      firstNonNull(params.getTag(), "latest"));
                  throw new DockerException(
                      "Docker push response doesn't contain image digest", 500);
                });

        return digestFuture.get();
      } catch (ExecutionException e) {
        // unwrap exception thrown by task with .getCause()
        throw new DockerException(
            "Docker image pushing failed. Cause: " + e.getCause().getLocalizedMessage(), 500);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new DockerException("Docker image pushing was interrupted", 500);
      }
    }
  }

  /**
   * Creates a new image from a container’s changes.
   *
   * @return id of a new image
   * @throws IOException when a problem occurs with docker api calls
   */
  public String commit(final CommitParams params) throws IOException {
    // TODO: add option to pause container
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/commit")
            .query("container", params.getContainer())) {
      addQueryParamIfNotNull(connection, "repo", params.getRepository());
      addQueryParamIfNotNull(connection, "tag", params.getTag());
      addQueryParamIfNotNull(
          connection,
          "comment",
          (params.getComment() == null) ? null : URLEncoder.encode(params.getComment(), "UTF-8"));
      addQueryParamIfNotNull(
          connection,
          "author",
          (params.getAuthor() == null) ? null : URLEncoder.encode(params.getAuthor(), "UTF-8"));
      final DockerResponse response = connection.request();
      if (CREATED.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), ContainerCommitted.class)
          .getId();
    }
  }

  /**
   * Pulls docker image from registry.
   *
   * @param progressMonitor ProgressMonitor for images creation process
   * @throws IOException when a problem occurs with docker api calls
   */
  public void pull(final PullParams params, final ProgressMonitor progressMonitor)
      throws IOException {
    pull(params, progressMonitor, dockerDaemonUri);
  }

  /**
   * Pull an image from registry. To pull from private registry use registry.address:port/image as
   * image.
   *
   * @param progressMonitor ProgressMonitor for images creation process
   * @param dockerDaemonUri docker service URI
   * @throws IOException when a problem occurs with docker api calls
   */
  protected void pull(
      final PullParams params, final ProgressMonitor progressMonitor, final URI dockerDaemonUri)
      throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/images/create")
            .query("fromImage", params.getFullRepo())
            .header(
                "X-Registry-Auth",
                authResolver.getXRegistryAuthHeaderValue(
                    params.getRegistry(), params.getAuthConfigs()))) {
      addQueryParamIfNotNull(connection, "tag", params.getTag());
      final DockerResponse response = connection.request();
      if (OK.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }

      try (InputStream responseStream = response.getInputStream()) {
        JsonMessageReader<ProgressStatus> progressReader =
            new JsonMessageReader<>(responseStream, ProgressStatus.class);

        // Here do some trick to be able interrupt output streaming process.
        // Current unix socket implementation of DockerConnection doesn't react to interruption.
        // So to be able to close unix socket connection and free resources we use main thread.
        // In case of any exception main thread cancels future and close connection.
        // If Docker connection implementation supports interrupting it will stop streaming on
        // interruption,
        // if not it will be stopped by closure of unix socket
        Future<Object> pullFuture =
            executor.submit(
                () -> {
                  ProgressStatus progressStatus;
                  while ((progressStatus = progressReader.next()) != null) {
                    progressMonitor.updateProgress(progressStatus);
                  }

                  return null;
                });

        // perform get to be able to get execution exception
        pullFuture.get();
      } catch (ExecutionException e) {
        // unwrap exception thrown by task with .getCause()
        throw new DockerException(e.getCause().getLocalizedMessage(), 500);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new DockerException("Docker image pulling was interrupted", 500);
      }
    }
  }

  /**
   * Creates docker container.
   *
   * @return information about just created container
   * @throws IOException when a problem occurs with docker api calls
   */
  public ContainerCreated createContainer(final CreateContainerParams params) throws IOException {
    byte[] entityBytesArray = toJson(params.getContainerConfig()).getBytes(StandardCharsets.UTF_8);

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/containers/create")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Content-Length", entityBytesArray.length)
            .entity(entityBytesArray)) {
      addQueryParamIfNotNull(connection, "name", params.getContainerName());
      final DockerResponse response = connection.request();
      if (CREATED.getStatusCode() != response.getStatus()) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), ContainerCreated.class);
    }
  }

  /**
   * Starts docker container.
   *
   * @throws IOException when a problem occurs with docker api calls
   */
  public void startContainer(final StartContainerParams params) throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/containers/" + params.getContainer() + "/start")) {
      final DockerResponse response = connection.request();
      final int status = response.getStatus();
      if (NO_CONTENT.getStatusCode() != status && NOT_MODIFIED.getStatusCode() != status) {
        final DockerException dockerException = getDockerException(response);
        if (OK.getStatusCode() == status) {
          // docker API 1.20 returns 200 with warning message about usage of loopback docker backend
          LOG.warn(dockerException.getLocalizedMessage());
        } else {
          throw dockerException;
        }
      }
    }
  }

  /**
   * Returns list of docker networks
   *
   * @throws IOException when problems occurs with docker api calls
   */
  public List<Network> getNetworks() throws IOException {
    return getNetworks(GetNetworksParams.create());
  }

  /**
   * Returns list of docker networks which was filtered by {@link GetNetworksParams}
   *
   * @throws IOException when problems occurs with docker api calls
   */
  public List<Network> getNetworks(GetNetworksParams params) throws IOException {
    final Filters filters = params.getFilters();

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/networks")) {
      if (filters != null) {
        connection.query("filters", urlPathSegmentEscaper().escape(toJson(filters.getFilters())));
      }
      DockerResponse response = connection.request();
      if (response.getStatus() / 100 != 2) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(
          response.getInputStream(), new TypeToken<List<Network>>() {});
    }
  }

  /**
   * Returns docker network matching provided id
   *
   * @throws IOException when problems occurs with docker api calls
   */
  public Network inspectNetwork(String netId) throws IOException {
    return inspectNetwork(InspectNetworkParams.create(netId));
  }

  /**
   * Returns docker network matching provided params
   *
   * @throws IOException when problems occurs with docker api calls
   */
  public Network inspectNetwork(InspectNetworkParams params) throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/networks/" + params.getNetworkId())) {
      final DockerResponse response = connection.request();
      if (response.getStatus() / 100 != 2) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), Network.class);
    }
  }

  /**
   * Creates docker network
   *
   * @throws IOException when problems occurs with docker api calls
   */
  public NetworkCreated createNetwork(CreateNetworkParams params) throws IOException {
    byte[] entityBytesArray = toJson(params.getNetwork()).getBytes(StandardCharsets.UTF_8);

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/networks/create")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Content-Length", entityBytesArray.length)
            .entity(entityBytesArray)) {
      final DockerResponse response = connection.request();
      if (response.getStatus() / 100 != 2) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), NetworkCreated.class);
    }
  }

  /**
   * Connects container to docker network
   *
   * @throws IOException when problems occurs with docker api calls
   */
  public void connectContainerToNetwork(String netId, String containerId) throws IOException {
    connectContainerToNetwork(
        ConnectContainerToNetworkParams.create(
            netId, new ConnectContainer().withContainer(containerId)));
  }

  /**
   * Connects container to docker network
   *
   * @throws IOException when problems occurs with docker api calls
   */
  public void connectContainerToNetwork(ConnectContainerToNetworkParams params) throws IOException {
    byte[] entityBytesArray = toJson(params.getConnectContainer()).getBytes(StandardCharsets.UTF_8);

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/networks/" + params.getNetworkId() + "/connect")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Content-Length", entityBytesArray.length)
            .entity(entityBytesArray)) {
      final DockerResponse response = connection.request();
      if (response.getStatus() / 100 != 2) {
        throw getDockerException(response);
      }
    }
  }

  /**
   * Disconnects container from docker network
   *
   * @throws IOException when problems occurs with docker api calls
   */
  public void disconnectContainerFromNetwork(String netId, String containerId) throws IOException {
    disconnectContainerFromNetwork(
        DisconnectContainerFromNetworkParams.create(
            netId, new DisconnectContainer().withContainer(containerId)));
  }

  /**
   * Disconnects container from docker network
   *
   * @throws IOException when problems occurs with docker api calls
   */
  public void disconnectContainerFromNetwork(DisconnectContainerFromNetworkParams params)
      throws IOException {
    byte[] entityBytesArray =
        toJson(params.getDisconnectContainer()).getBytes(StandardCharsets.UTF_8);

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("POST")
            .path(apiVersionPathPrefix + "/networks/" + params.getNetworkId() + "/disconnect")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Content-Length", entityBytesArray.length)
            .entity(entityBytesArray)) {
      final DockerResponse response = connection.request();
      if (response.getStatus() / 100 != 2) {
        throw getDockerException(response);
      }
    }
  }

  /**
   * Removes network matching provided id
   *
   * @throws IOException when a problem occurs with docker api calls
   */
  public void removeNetwork(String netId) throws IOException {
    removeNetwork(RemoveNetworkParams.create(netId));
  }

  /**
   * Removes network matching provided params
   *
   * @throws NetworkNotFoundException if network is not found
   * @throws IOException when a problem occurs with docker api calls
   */
  public void removeNetwork(RemoveNetworkParams params) throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("DELETE")
            .path(apiVersionPathPrefix + "/networks/" + params.getNetworkId())) {
      final DockerResponse response = connection.request();
      int status = response.getStatus();
      if (status == NOT_FOUND.getStatusCode()) {
        throw new NetworkNotFoundException(readAndCloseQuietly(response.getInputStream()));
      }
      if (status / 100 != 2) {
        throw getDockerException(response);
      }
    }
  }

  /**
   * Returns all volumes.
   *
   * @return object that contains list of volumes in the system
   * @throws IOException when a problem occurs with docker api calls
   */
  public Volumes getVolumes() throws IOException {
    return getVolumes(GetVolumesParams.create());
  }

  /**
   * Returns volumes filtered by provided params.
   *
   * @return object that contains list of volumes in the system
   * @throws IOException when a problem occurs with docker api calls
   */
  public Volumes getVolumes(GetVolumesParams params) throws IOException {
    final Filters filters = params.getFilters();

    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("GET")
            .path(apiVersionPathPrefix + "/volumes")) {
      if (filters != null) {
        connection.query("filters", urlPathSegmentEscaper().escape(toJson(filters.getFilters())));
      }
      DockerResponse response = connection.request();
      if (response.getStatus() / 100 != 2) {
        throw getDockerException(response);
      }
      return parseResponseStreamAndClose(response.getInputStream(), Volumes.class);
    }
  }

  /**
   * Removes a volume that matches provided name.
   *
   * @throws VolumeNotFoundException if volume is not found
   * @throws IOException when a problem occurs with docker api calls
   */
  public void removeVolume(String volumeName) throws IOException {
    removeVolume(RemoveVolumeParams.create(volumeName));
  }

  /**
   * Removes a volume that matches provided params.
   *
   * @throws VolumeNotFoundException if volume is not found
   * @throws IOException when a problem occurs with docker api calls
   */
  public void removeVolume(RemoveVolumeParams params) throws IOException {
    try (DockerConnection connection =
        connectionFactory
            .openConnection(dockerDaemonUri)
            .method("DELETE")
            .path(apiVersionPathPrefix + "/volumes/" + params.getVolumeName())) {
      final DockerResponse response = connection.request();
      int status = response.getStatus();
      if (status == NOT_FOUND.getStatusCode()) {
        throw new VolumeNotFoundException(readAndCloseQuietly(response.getInputStream()));
      }
      if (status / 100 != 2) {
        throw getDockerException(response);
      }
    }
  }

  private String getBuildImageId(ProgressStatus progressStatus) {
    final String stream = progressStatus.getStream();
    if (stream != null && stream.startsWith("Successfully built ")) {
      int endSize = 19;
      while (endSize < stream.length() && Character.digit(stream.charAt(endSize), 16) != -1) {
        endSize++;
      }
      return stream.substring(19, endSize);
    }
    return null;
  }

  protected <T> T parseResponseStreamAndClose(InputStream inputStream, Class<T> clazz)
      throws IOException {
    try (InputStreamReader reader = new InputStreamReader(inputStream)) {
      T objectFromJson = GSON.fromJson(reader, clazz);
      if (objectFromJson == null) {
        LOG.error(
            "Docker response doesn't contain any data, though it was expected to contain some.");
        throw new IOException(
            "Internal server error. Unexpected response body received from Docker.");
      }
      return objectFromJson;
    } catch (JsonParseException e) {
      throw new IOException(e.getLocalizedMessage(), e);
    }
  }

  protected <T> T parseResponseStreamAndClose(InputStream inputStream, TypeToken<T> tt)
      throws IOException {
    try (InputStreamReader reader = new InputStreamReader(inputStream)) {
      T objectFromJson = GSON.fromJson(reader, tt.getType());
      if (objectFromJson == null) {
        LOG.error(
            "Docker response doesn't contain any data, though it was expected to contain some.");
        throw new IOException(
            "Internal server error. Unexpected response body received from Docker.");
      }
      return objectFromJson;
    } catch (JsonParseException e) {
      throw new IOException(e.getLocalizedMessage(), e);
    }
  }

  protected DockerException getDockerException(DockerResponse response) throws IOException {
    try (InputStreamReader isr = new InputStreamReader(response.getInputStream())) {
      String dockerResponseContent = CharStreams.toString(isr);
      return new DockerException(
          "Error response from docker API, status: "
              + response.getStatus()
              + ", message: "
              + dockerResponseContent,
          dockerResponseContent,
          response.getStatus());
    }
  }

  private void createTarArchive(File tar, File... files) throws IOException {
    TarUtils.tarFiles(tar, 0, files);
  }

  /**
   * Adds given parameter to query if it set (not null).
   *
   * @param connection connection to docker service
   * @param queryParamName name of query parameter
   * @param paramValue value of query parameter
   * @throws NullPointerException if {@code queryParamName} is null
   */
  private void addQueryParamIfNotNull(
      DockerConnection connection, String queryParamName, Object paramValue) {
    if (paramValue != null) {
      connection.query(queryParamName, paramValue);
    }
  }

  /**
   * The same as {@link #addQueryParamIfNotNull(DockerConnection, String, Object)}, but in case of
   * {@code paramValue} is {@code true} '1' will be added as parameter value, in case of {@code
   * false} '0'.
   */
  private void addQueryParamIfNotNull(
      DockerConnection connection, String queryParamName, Boolean paramValue) {
    if (paramValue != null) {
      connection.query(queryParamName, paramValue ? 1 : 0);
    }
  }

  /**
   * Serializes object into JSON. Needed to avoid usage try catch blocks with {@link
   * JsonParseException} runtime exception catching.
   *
   * @param object object that should be converted into JSON
   * @return json as a string
   * @throws IOException if serialization to JSON fails
   */
  private String toJson(Object object) throws IOException {
    try {
      return GSON.toJson(object);
    } catch (JsonParseException e) {
      throw new IOException(e.getLocalizedMessage(), e);
    }
  }
}
