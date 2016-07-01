/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client;

import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.TarUtils;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.plugin.docker.client.connection.CloseConnectionInputStream;
import org.eclipse.che.plugin.docker.client.connection.DockerConnection;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.connection.DockerResponse;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;
import org.eclipse.che.plugin.docker.client.exception.ContainerNotFoundException;
import org.eclipse.che.plugin.docker.client.exception.DockerException;
import org.eclipse.che.plugin.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.plugin.docker.client.json.ContainerCommitted;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerExitStatus;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerListEntry;
import org.eclipse.che.plugin.docker.client.json.ContainerProcesses;
import org.eclipse.che.plugin.docker.client.json.Event;
import org.eclipse.che.plugin.docker.client.json.ExecConfig;
import org.eclipse.che.plugin.docker.client.json.ExecCreated;
import org.eclipse.che.plugin.docker.client.json.ExecInfo;
import org.eclipse.che.plugin.docker.client.json.ExecStart;
import org.eclipse.che.plugin.docker.client.json.Filters;
import org.eclipse.che.plugin.docker.client.json.Image;
import org.eclipse.che.plugin.docker.client.json.ImageInfo;
import org.eclipse.che.plugin.docker.client.json.NetworkCreated;
import org.eclipse.che.plugin.docker.client.json.ProgressStatus;
import org.eclipse.che.plugin.docker.client.json.Version;
import org.eclipse.che.plugin.docker.client.json.network.ConnectContainer;
import org.eclipse.che.plugin.docker.client.json.network.DisconnectContainer;
import org.eclipse.che.plugin.docker.client.json.network.Network;
import org.eclipse.che.plugin.docker.client.params.AttachContainerParams;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.client.params.CommitParams;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.GetContainerLogsParams;
import org.eclipse.che.plugin.docker.client.params.GetEventsParams;
import org.eclipse.che.plugin.docker.client.params.GetExecInfoParams;
import org.eclipse.che.plugin.docker.client.params.GetResourceParams;
import org.eclipse.che.plugin.docker.client.params.InspectContainerParams;
import org.eclipse.che.plugin.docker.client.params.InspectImageParams;
import org.eclipse.che.plugin.docker.client.params.KillContainerParams;
import org.eclipse.che.plugin.docker.client.params.ListContainersParams;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.params.PushParams;
import org.eclipse.che.plugin.docker.client.params.PutResourceParams;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.client.params.RemoveImageParams;
import org.eclipse.che.plugin.docker.client.params.RemoveNetworkParams;
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.plugin.docker.client.params.StopContainerParams;
import org.eclipse.che.plugin.docker.client.params.TagParams;
import org.eclipse.che.plugin.docker.client.params.TopParams;
import org.eclipse.che.plugin.docker.client.params.WaitContainerParams;
import org.eclipse.che.plugin.docker.client.params.network.ConnectContainerToNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.CreateNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.DisconnectContainerFromNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.GetNetworksParams;
import org.eclipse.che.plugin.docker.client.params.network.InspectNetworkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_MODIFIED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;

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
    private static final Logger LOG  = LoggerFactory.getLogger(DockerConnector.class);
    // Docker uses uppercase in first letter in names of json objects, e.g. {"Id":"123"} instead of {"id":"123"}
    private static final Gson   GSON = new GsonBuilder().disableHtmlEscaping()
                                                        .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                                                        .create();

    private final URI                        dockerDaemonUri;
    private final DockerRegistryAuthResolver authResolver;
    private final ExecutorService            executor;
    private final DockerConnectionFactory    connectionFactory;

    @Inject
    public DockerConnector(DockerConnectorConfiguration connectorConfiguration,
                           DockerConnectionFactory connectionFactory,
                           DockerRegistryAuthResolver authResolver) {
        this.dockerDaemonUri = connectorConfiguration.getDockerDaemonUri();
        this.connectionFactory = connectionFactory;
        this.authResolver = authResolver;
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                                                         .setNameFormat("DockerApiConnector-%d")
                                                         .setDaemon(true)
                                                         .build());
    }

    /**
     * Gets system-wide information.
     *
     * @return system-wide information
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public org.eclipse.che.plugin.docker.client.json.SystemInfo getSystemInfo() throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/info")) {
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), org.eclipse.che.plugin.docker.client.json.SystemInfo.class);
        }
    }

    /**
     * Gets docker version.
     *
     * @return information about version docker
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public Version getVersion() throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/version")) {
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), Version.class);
        }
    }

    /**
     * Lists docker images.
     *
     * @return list of docker images
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public List<Image> listImages() throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/images/json")) {
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            return parseResponseStreamAsListAndClose(response.getInputStream(), new TypeToken<List<Image>>() {}.getType());
        }
    }

    /**
     * Method returns list of docker containers, include non-running ones.
     *
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public List<ContainerListEntry> listContainers() throws IOException {
        return listContainers(ListContainersParams.create().withAll(true));
    }

    /**
     * Method returns list of docker containers which was filtered by {@link ListContainersParams}
     *
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public List<ContainerListEntry> listContainers(ListContainersParams params) throws IOException {
        final Filters filters = params.getFilters();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/containers/json")) {
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
            return parseResponseStreamAsListAndClose(response.getInputStream(), new TypeToken<List<ContainerListEntry>>() {}.getType());
        }
    }

    /**
     * Gets detailed information about docker image.
     *
     * @param image
     *         id or full repository name of docker image
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
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public ImageInfo inspectImage(InspectImageParams params) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/images/" + params.getImage() + "/json")) {
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ImageInfo.class);
        }
    }

    /**
     * Stops container.
     *
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public void stopContainer(final StopContainerParams params) throws IOException {
        final Long timeout = (params.getTimeout() == null) ? null :
                             (params.getTimeunit() == null) ? params.getTimeout() : params.getTimeunit().toSeconds(params.getTimeout());

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + params.getContainer() + "/stop")) {
            addQueryParamIfNotNull(connection, "t", timeout);
            final DockerResponse response = connection.request();
            if (response.getStatus() / 100 != 2) {
                throw getDockerException(response);
            }
        }
    }

    /**
     * Sends specified signal to running container.
     * If signal not set, then SIGKILL will be used.
     *
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public void killContainer(final KillContainerParams params) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + params.getContainer() + "/kill")) {
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
     * @param container
     *         container identifier, either id or name
     * @throws IOException
     */
    public void killContainer(String container) throws IOException {
        killContainer(KillContainerParams.create(container));
    }

    /**
     * Removes docker container.
     *
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public void removeContainer(final RemoveContainerParams params) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("DELETE")
                                                            .path("/containers/" + params.getContainer())) {
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
     * @param container
     *         container identifier, either id or name
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
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public int waitContainer(final WaitContainerParams params) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + params.getContainer() + "/wait")) {
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerExitStatus.class).getStatusCode();
        }
    }

    /**
     * Gets detailed information about docker container.
     *
     * @param container
     *         id of container
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
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public ContainerInfo inspectContainer(final InspectContainerParams params) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/containers/" + params.getContainer() + "/json")) {
            addQueryParamIfNotNull(connection, "size", params.isReturnContainerSize());
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerInfo.class);
        }
    }

    /**
     * Attaches to the container with specified id.
     * <br/>
     * Note, that if @{code stream} parameter is {@code true} then get 'live' stream from container.
     * Typically need to run this method in separate thread, if {@code stream}
     * is {@code true} since this method blocks until container is running.
     * @param containerLogsProcessor
     *         output for container logs
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public void attachContainer(final AttachContainerParams params, MessageProcessor<LogMessage> containerLogsProcessor)
            throws IOException {
        final Boolean stream = params.isStream();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + params.getContainer() + "/attach")
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
     * @param containerLogsProcessor
     *         output for container logs
     * @throws ContainerNotFoundException
     *         when container not found by docker (docker api returns 404)
     * @throws IOException
     *         when a problem occurs with docker api calls
     */
    public void getContainerLogs(final GetContainerLogsParams params, MessageProcessor<LogMessage> containerLogsProcessor)
            throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/containers/" + params.getContainer() + "/logs")
                                                            .query("stdout", 1)
                                                            .query("stderr", 1)) {
            addQueryParamIfNotNull(connection, "details", params.isDetails());
            addQueryParamIfNotNull(connection, "follow", params.isFollow());
            addQueryParamIfNotNull(connection, "since", params.getSince());
            addQueryParamIfNotNull(connection, "timestamps", params.isTimestamps());
            addQueryParamIfNotNull(connection, "tail", params.getTail());

            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (status == 404) {
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
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public Exec createExec(final CreateExecParams params) throws IOException {
        final ExecConfig execConfig = new ExecConfig().withCmd(params.getCmd())
                                                      .withAttachStderr(params.isDetach() == Boolean.FALSE)
                                                      .withAttachStdout(params.isDetach() == Boolean.FALSE);
        byte[] entityBytesArray = toJson(execConfig).getBytes();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + params.getContainer() + "/exec")
                                                            .header("Content-Type", MediaType.APPLICATION_JSON)
                                                            .header("Content-Length", entityBytesArray.length)
                                                            .entity(entityBytesArray)) {
            final DockerResponse response = connection.request();
            if (response.getStatus() / 100 != 2) {
                throw getDockerException(response);
            }
            return new Exec(params.getCmd(), parseResponseStreamAndClose(response.getInputStream(), ExecCreated.class).getId());
        }
    }

    /**
     * Starts a previously set up exec instance.
     *
     * @param execOutputProcessor
     *         processor for exec output
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public void startExec(final StartExecParams params, @Nullable MessageProcessor<LogMessage> execOutputProcessor) throws IOException {
        final ExecStart execStart = new ExecStart().withDetach(params.isDetach() == Boolean.TRUE)
                                                   .withTty(params.isTty() == Boolean.TRUE);

        byte[] entityBytesArray = toJson(execStart).getBytes();
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/exec/" + params.getExecId() + "/start")
                                                            .header("Content-Type", MediaType.APPLICATION_JSON)
                                                            .header("Content-Length", entityBytesArray.length)
                                                            .entity(entityBytesArray)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            // According to last doc (https://docs.docker.com/reference/api/docker_remote_api_v1.15/#exec-start) status must be 201 but
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
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public ExecInfo getExecInfo(final GetExecInfoParams params) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/exec/" + params.getExecId() + "/json")) {
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
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public ContainerProcesses top(final TopParams params) throws IOException {
        final String[] psArgs = params.getPsArgs();

        try (final DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                                  .method("GET")
                                                                  .path("/containers/" + params.getContainer() + "/top")) {
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
     * @throws IOException
     *          when a problem occurs with docker api calls
     * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8.0 version
     */
    public InputStream getResource(final GetResourceParams params) throws IOException {
        DockerConnection connection = null;
        try {
            connection = connectionFactory.openConnection(dockerDaemonUri)
                                          .method("GET")
                                          .path("/containers/" + params.getContainer() + "/archive")
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
     * @throws IOException
     *          when a problem occurs with docker api calls, or during file system operations
     * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8 version
     */
    public void putResource(final PutResourceParams params) throws IOException {
        File tarFile;
        long length;
        try (InputStream sourceData = params.getSourceStream()) {
            // TODO according to http spec http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.4,
            // it is possible to send request without specifying content length if chunked encoding header is set
            // Investigate is it possible to write the stream to request directly

            // we save stream to file, because we have to know its length
            Path tarFilePath = Files.createTempFile("compressed-resources", ".tar");
            tarFile = tarFilePath.toFile();
            length = Files.copy(sourceData, tarFilePath, StandardCopyOption.REPLACE_EXISTING);
        }

        try (InputStream tarStream = new BufferedInputStream(new FileInputStream(tarFile));
             DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("PUT")
                                                            .path("/containers/" + params.getContainer() + "/archive")
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
     * Get docker events.
     * Parameter {@code untilSecond} does nothing if {@code sinceSecond} is 0.<br>
     * If {@code untilSecond} and {@code sinceSecond} are 0 method gets new events only (streaming mode).<br>
     * If {@code untilSecond} and {@code sinceSecond} are not 0 (but less that current date)
     * methods get events that were generated between specified dates.<br>
     * If {@code untilSecond} is 0 but {@code sinceSecond} is not method gets old events and streams new ones.<br>
     * If {@code sinceSecond} is 0 no old events will be got.<br>
     * With some connection implementations method can fail due to connection timeout in streaming mode.
     *
     * @param messageProcessor
     *         processor of all found events that satisfy specified parameters
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public void getEvents(final GetEventsParams params, MessageProcessor<Event> messageProcessor) throws IOException {
        final Filters filters = params.getFilters();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/events")) {
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
                new MessagePumper<>(new JsonMessageReader<>(responseStream, Event.class), messageProcessor).start();
            }
        }
    }

    /**
     * Builds new image.
     *
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @return image id
     * @throws IOException
     * @throws InterruptedException
     *         if build process was interrupted
     */
    public String buildImage(final BuildImageParams params,
                             final ProgressMonitor progressMonitor) throws IOException, InterruptedException {

        if (params.getRemote() != null) {
            // build context provided by remote URL
            DockerConnection dockerConnection = connectionFactory.openConnection(dockerDaemonUri)
                                                                 .query("remote", params.getRemote());
            return buildImage(dockerConnection,
                              params,
                              progressMonitor);
        }

        // build context is set of files
        final File tar = Files.createTempFile(null, ".tar").toFile();
        try {
            File[] files = new File[params.getFiles().size()];
            files = params.getFiles().toArray(files);
            createTarArchive(tar, files);
            try (InputStream tarInput = new FileInputStream(tar)) {
                DockerConnection dockerConnection = connectionFactory.openConnection(dockerDaemonUri)
                                                                     .header("Content-Type", "application/x-compressed-tar")
                                                                     .header("Content-Length", tar.length())
                                                                     .entity(tarInput);
                return buildImage(dockerConnection,
                                  params,
                                  progressMonitor);
            }
        } finally {
            FileCleaner.addFile(tar);
        }
    }

    private String buildImage(final DockerConnection dockerConnection,
                              final BuildImageParams params,
                              final ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        final AuthConfigs authConfigs = params.getAuthConfigs();
        final String repository = params.getRepository();
        final String tag = params.getTag();

        try (DockerConnection connection = dockerConnection.method("POST")
                                                           .path("/build")
                                                           .query("rm", 1)
                                                           .query("forcerm", 1)
                                                           .header("X-Registry-Config",
                                                                   authResolver.getXRegistryConfigHeaderValue(authConfigs))) {
            if (tag == null) {
                addQueryParamIfNotNull(connection, "t", repository);
            } else {
                addQueryParamIfNotNull(connection, "t", repository == null ? null : repository + ':' + tag);
            }
            addQueryParamIfNotNull(connection, "memory", params.getMemoryLimit());
            addQueryParamIfNotNull(connection, "memswap", params.getMemorySwapLimit());
            addQueryParamIfNotNull(connection, "pull", params.isDoForcePull());
            addQueryParamIfNotNull(connection, "dockerfile", params.getDockerfile());

            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            try (InputStream responseStream = response.getInputStream()) {
                JsonMessageReader<ProgressStatus> progressReader = new JsonMessageReader<>(responseStream, ProgressStatus.class);

                final ValueHolder<IOException> errorHolder = new ValueHolder<>();
                final ValueHolder<String> imageIdHolder = new ValueHolder<>();
                // Here do some trick to be able interrupt build process. Basically for now it is not possible interrupt docker daemon while
                // it's building images but here we need just be able to close connection to the unix socket. Thread is blocking while read
                // from the socket stream so need one more thread that is able to close socket. In this way we can release thread that is
                // blocking on i/o.
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ProgressStatus progressStatus;
                            while ((progressStatus = progressReader.next()) != null) {
                                final String buildImageId = getBuildImageId(progressStatus);
                                if (buildImageId != null) {
                                    imageIdHolder.set(buildImageId);
                                }
                                progressMonitor.updateProgress(progressStatus);
                            }
                        } catch (IOException e) {
                            errorHolder.set(e);
                        }
                        synchronized (this) {
                            notify();
                        }
                    }
                };
                executor.execute(runnable);
                // noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (runnable) {
                    runnable.wait();
                }
                final IOException ioe = errorHolder.get();
                if (ioe != null) {
                    throw ioe;
                }
                if (imageIdHolder.get() == null) {
                    throw new IOException("Docker image build failed");
                }
                return imageIdHolder.get();
            }
        }
    }


    /**
     * Removes docker image.
     *
     * @param image
     *         image identifier, either id or name
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public void removeImage(String image) throws IOException {
        removeImage(RemoveImageParams.create(image));
    }

    /**
     * Removes docker image.
     *
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public void removeImage(final RemoveImageParams params) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("DELETE")
                                                            .path("/images/" + params.getImage())) {
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
     * @throws ImageNotFoundException
     *         when docker api return 404 status
     * @throws IOException
     *         when a problem occurs with docker api calls
     */
    public void tag(final TagParams params) throws ImageNotFoundException,
                                                   IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/" + params.getImage() + "/tag")
                                                            .query("repo", params.getRepository())) {
            addQueryParamIfNotNull(connection, "force", params.isForce());
            addQueryParamIfNotNull(connection, "tag", params.getTag());
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (status == 404) {
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
     * @param progressMonitor
     *         ProgressMonitor for images pushing process
     * @return digest of just pushed image
     * @throws IOException
     *          when a problem occurs with docker api calls
     * @throws InterruptedException
     *         if push process was interrupted
     */
    public String push(final PushParams params, final ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        final String fullRepo = params.getFullRepo();

        final ValueHolder<String> digestHolder = new ValueHolder<>();
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/" + fullRepo + "/push")
                                                            .header("X-Registry-Auth",
                                                                    authResolver.getXRegistryAuthHeaderValue(
                                                                            params.getRegistry(),
                                                                            params.getAuthConfigs()))) {
            addQueryParamIfNotNull(connection, "tag", params.getTag());
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            try (InputStream responseStream = response.getInputStream()) {
                JsonMessageReader<ProgressStatus> progressReader = new JsonMessageReader<>(responseStream, ProgressStatus.class);

                final ValueHolder<IOException> errorHolder = new ValueHolder<>();
                //it is necessary to track errors during the push, this is useful in the case when docker API returns status 200 OK,
                //but in fact we have an error (e.g docker registry is not accessible but we are trying to push).
                final ValueHolder<String> exceptionHolder = new ValueHolder<>();
                // Here do some trick to be able interrupt push process. Basically for now it is not possible interrupt docker daemon while
                // it's pushing images but here we need just be able to close connection to the unix socket. Thread is blocking while read
                // from the socket stream so need one more thread that is able to close socket. In this way we can release thread that is
                // blocking on i/o.
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String digestPrefix = firstNonNull(params.getTag(), "latest") + ": digest: ";
                            ProgressStatus progressStatus;
                            while ((progressStatus = progressReader.next()) != null && exceptionHolder.get() == null) {
                                progressMonitor.updateProgress(progressStatus);
                                if (progressStatus.getError() != null) {
                                    exceptionHolder.set(progressStatus.getError());
                                }
                                String status = progressStatus.getStatus();
                                // Here we find string with digest which has following format:
                                // <tag>: digest: <digest> size: <size>
                                // for example:
                                // latest: digest: sha256:9a70e6222ded459fde37c56af23887467c512628eb8e78c901f3390e49a800a0 size: 62189
                                if (status != null && status.startsWith(digestPrefix)) {
                                    String digest = status.substring(digestPrefix.length(), status.indexOf(" ", digestPrefix.length()));
                                    digestHolder.set(digest);
                                }
                            }
                        } catch (IOException e) {
                            errorHolder.set(e);
                        }
                        synchronized (this) {
                            notify();
                        }
                    }
                };
                executor.execute(runnable);
                // noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (runnable) {
                    runnable.wait();
                }
                if (exceptionHolder.get() != null) {
                    throw new DockerException(exceptionHolder.get(), 500);
                }
                if (digestHolder.get() == null) {
                    LOG.error("Docker image {}:{} was successfully pushed, but its digest wasn't obtained",
                              fullRepo,
                              firstNonNull(params.getTag(), "latest"));
                    throw new DockerException("Docker image was successfully pushed, but its digest wasn't obtained", 500);
                }
                final IOException ioe = errorHolder.get();
                if (ioe != null) {
                    throw ioe;
                }
            }
        }
        return digestHolder.get();
    }

    /**
     * Creates a new image from a container’s changes.
     *
     * @return id of a new image
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public String commit(final CommitParams params) throws IOException {
        // TODO: add option to pause container
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/commit")
                                                            .query("container", params.getContainer())) {
            addQueryParamIfNotNull(connection, "repo", params.getRepository());
            addQueryParamIfNotNull(connection, "tag", params.getTag());
            addQueryParamIfNotNull(connection, "comment", (params.getComment() == null) ?
                                                          null : URLEncoder.encode(params.getComment(), "UTF-8"));
            addQueryParamIfNotNull(connection, "author", (params.getAuthor() == null) ?
                                                         null : URLEncoder.encode(params.getAuthor(), "UTF-8"));
            final DockerResponse response = connection.request();
            if (CREATED.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerCommitted.class).getId();
        }
    }

    /**
     * Pulls docker image from registry.
     *
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @throws IOException
     *          when a problem occurs with docker api calls
     * @throws InterruptedException
     *         if push process was interrupted
     */
    public void pull(final PullParams params, final ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        pull(params, progressMonitor, dockerDaemonUri);
    }

    /**
     * See <a href="https://docs.docker.com/reference/api/docker_remote_api_v1.16/#create-an-image">Docker remote API # Create an
     * image</a>.
     * To pull from private registry use registry.address:port/image as image. This is not documented.
     *
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @param dockerDaemonUri
     *         docker service URI
     * @throws IOException
     *          when a problem occurs with docker api calls
     * @throws InterruptedException
     *         if push process was interrupted
     */
    protected void pull(final PullParams params,
                        final ProgressMonitor progressMonitor,
                        final URI dockerDaemonUri) throws IOException, InterruptedException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/create")
                                                            .query("fromImage", params.getFullRepo())
                                                            .header("X-Registry-Auth",
                                                                    authResolver.getXRegistryAuthHeaderValue(
                                                                            params.getRegistry(),
                                                                            params.getAuthConfigs()))) {
            addQueryParamIfNotNull(connection, "tag", params.getTag());
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            try (InputStream responseStream = response.getInputStream()) {
                JsonMessageReader<ProgressStatus> progressReader = new JsonMessageReader<>(responseStream, ProgressStatus.class);

                final ValueHolder<IOException> errorHolder = new ValueHolder<>();
                // Here do some trick to be able interrupt pull process. Basically for now it is not possible interrupt docker daemon while
                // it's pulling images but here we need just be able to close connection to the unix socket. Thread is blocking while read
                // from the socket stream so need one more thread that is able to close socket. In this way we can release thread that is
                // blocking on i/o.
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ProgressStatus progressStatus;
                            while ((progressStatus = progressReader.next()) != null) {
                                progressMonitor.updateProgress(progressStatus);
                            }
                        } catch (IOException e) {
                            errorHolder.set(e);
                        }
                        synchronized (this) {
                            notify();
                        }
                    }
                };
                executor.execute(runnable);
                // noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (runnable) {
                    runnable.wait();
                }
                final IOException ioe = errorHolder.get();
                if (ioe != null) {
                    throw ioe;
                }
            }
        }
    }

    /**
     * Creates docker container.
     *
     * @return information about just created container
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public ContainerCreated createContainer(final CreateContainerParams params) throws IOException {
        byte[] entityBytesArray = toJson(params.getContainerConfig()).getBytes();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/create")
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
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public void startContainer(final StartContainerParams params) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + params.getContainer() + "/start")) {
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
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public List<Network> getNetworks() throws IOException {
        return getNetworks(GetNetworksParams.create());
    }

    /**
     * Returns list of docker networks which was filtered by {@link GetNetworksParams}
     *
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public List<Network> getNetworks(GetNetworksParams params) throws IOException {
        final Filters filters = params.getFilters();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/networks")) {
            if (filters != null) {
                connection.query("filters", urlPathSegmentEscaper().escape(toJson(filters.getFilters())));
            }
            DockerResponse response = connection.request();
            if (response.getStatus() / 100 != 2) {
                throw getDockerException(response);
            }
            return parseResponseStreamAsListAndClose(response.getInputStream(), new TypeToken<List<Network>>() {}.getType());
        }
    }

    /**
     * Returns docker network matching provided id
     *
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public Network inspectNetwork(@NotNull String netId) throws IOException {
        return inspectNetwork(InspectNetworkParams.create(netId));
    }

    /**
     * Returns docker network matching provided params
     *
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public Network inspectNetwork(InspectNetworkParams params) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/networks/" + params.getNetworkId())) {
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
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public NetworkCreated createNetwork(CreateNetworkParams params) throws IOException {
        byte[] entityBytesArray = toJson(params.getNetwork()).getBytes();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/networks/create")
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
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void connectContainerToNetwork(String netId, String containerId) throws IOException {
        connectContainerToNetwork(ConnectContainerToNetworkParams.create(netId, new ConnectContainer().withContainer(containerId)));
    }

    /**
     * Connects container to docker network
     *
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void connectContainerToNetwork(ConnectContainerToNetworkParams params) throws IOException {
        byte[] entityBytesArray = toJson(params.getConnectContainer()).getBytes();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/networks/" + params.getNetworkId() + "/connect")
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
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void disconnectContainerFromNetwork(@NotNull String netId, @NotNull String containerId) throws IOException {
        disconnectContainerFromNetwork(
                DisconnectContainerFromNetworkParams.create(netId,
                                                            new DisconnectContainer().withContainer(containerId)));
    }

    /**
     * Disconnects container from docker network
     *
     * @throws IOException
     *         when problems occurs with docker api calls
     */
    public void disconnectContainerFromNetwork(DisconnectContainerFromNetworkParams params) throws IOException {
        byte[] entityBytesArray = toJson(params.getDisconnectContainer()).getBytes();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/networks/" + params.getNetworkId() + "/disconnect")
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
     * @throws IOException
     *         when a problem occurs with docker api calls
     */
    public void removeNetwork(@NotNull String netId) throws IOException {
        removeNetwork(RemoveNetworkParams.create(netId));
    }

    /**
     * Removes network matching provided params
     *
     * @throws IOException
     *         when a problem occurs with docker api calls
     */
    public void removeNetwork(RemoveNetworkParams params) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("DELETE")
                                                            .path("/networks/" + params.getNetworkId())) {
            final DockerResponse response = connection.request();
            if (response.getStatus() / 100 != 2) {
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

    protected <T> T parseResponseStreamAndClose(InputStream inputStream, Class<T> clazz) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            return GSON.fromJson(reader, clazz);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    protected <T> List<T> parseResponseStreamAsListAndClose(InputStream inputStream, Type type) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            return GSON.fromJson(reader, type);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    protected DockerException getDockerException(DockerResponse response) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(response.getInputStream())) {
            String dockerResponseContent = CharStreams.toString(isr);
            return new DockerException(
                    "Error response from docker API, status: " + response.getStatus() + ", message: " + dockerResponseContent,
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
     * @param connection
     *         connection to docker service
     * @param queryParamName
     *         name of query parameter
     * @param paramValue
     *         value of query parameter
     * @throws NullPointerException
     *         if {@code queryParamName} is null
     */
    private void addQueryParamIfNotNull(DockerConnection connection, String queryParamName, Object paramValue) {
        if (paramValue != null) {
            connection.query(queryParamName, paramValue);
        }
    }

    /**
     * The same as {@link #addQueryParamIfNotNull(DockerConnection, String, Object)}, but
     * in case of {@code paramValue} is {@code true} '1' will be added as parameter value, in case of {@code false} '0'.
     */
    private void addQueryParamIfNotNull(DockerConnection connection, String queryParamName, Boolean paramValue) {
        if (paramValue != null) {
            connection.query(queryParamName, paramValue ? 1 : 0);
        }
    }

    /**
     * Serializes object into JSON.
     * Needed to avoid usage try catch blocks with {@link JsonParseException} runtime exception catching.
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
