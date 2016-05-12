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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonNameConvention;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.TarUtils;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.plugin.docker.client.connection.CloseConnectionInputStream;
import org.eclipse.che.plugin.docker.client.connection.DockerConnection;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.connection.DockerResponse;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;
import org.eclipse.che.plugin.docker.client.json.ContainerCommitted;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
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
import org.eclipse.che.plugin.docker.client.json.HostConfig;
import org.eclipse.che.plugin.docker.client.json.Image;
import org.eclipse.che.plugin.docker.client.json.ImageInfo;
import org.eclipse.che.plugin.docker.client.json.ProgressStatus;
import org.eclipse.che.plugin.docker.client.json.Version;
import org.eclipse.che.plugin.docker.client.params.AttachContainerParams;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.client.params.CommitParams;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
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
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.plugin.docker.client.params.StopContainerParams;
import org.eclipse.che.plugin.docker.client.params.TagParams;
import org.eclipse.che.plugin.docker.client.params.TopParams;
import org.eclipse.che.plugin.docker.client.params.WaitContainerParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_MODIFIED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

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

    private final URI                     dockerDaemonUri;
    private final InitialAuthConfig       initialAuthConfig;
    private final ExecutorService         executor;
    private final DockerConnectionFactory connectionFactory;

    @Inject
    public DockerConnector(DockerConnectorConfiguration connectorConfiguration,
                           DockerConnectionFactory connectionFactory) {
        this.dockerDaemonUri = connectorConfiguration.getDockerDaemonUri();
        this.initialAuthConfig = connectorConfiguration.getAuthConfigs();
        this.connectionFactory = connectionFactory;
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
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
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
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
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
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
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
                connection.query("filters", urlPathSegmentEscaper().escape(JsonHelper.toJson(filters)));
            }
            DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAsListAndClose(response.getInputStream(), new TypeToken<List<ContainerListEntry>>() {}.getType());
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * The same as {@link #buildImage(String, ProgressMonitor, AuthConfigs, boolean, long, long, File...)} but without memory limits.
     *
     * @see #buildImage(String, ProgressMonitor, AuthConfigs, boolean, long, long, File...)
     * @deprecated use {@link #buildImage(BuildImageParams, ProgressMonitor)} instead
     */
    @Deprecated
    public String buildImage(String repository,
                             ProgressMonitor progressMonitor,
                             AuthConfigs authConfigs,
                             boolean doForcePull,
                             File... files) throws IOException, InterruptedException {
        return buildImage(repository, progressMonitor, authConfigs, doForcePull, 0, 0, files);
    }

    /**
     * Builds new docker image from specified dockerfile.
     *
     * @param repository
     *         full repository name to be applied to newly created image
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @param authConfigs
     *         Authentication configuration for private registries. Can be null.
     * @param memoryLimit
     *         Memory limit for build in bytes
     * @param memorySwapLimit
     *         Total memory in bytes (memory + swap), -1 to enable unlimited swap
     * @param files
     *         files that are needed for creation docker images (e.g. file of directories used in ADD instruction in Dockerfile), one of
     *         them must be Dockerfile.
     * @return image id
     * @throws IOException
     * @throws InterruptedException
     *         if build process was interrupted
     * @deprecated use {@link #buildImage(BuildImageParams, ProgressMonitor)} instead
     */
    @Deprecated
    public String buildImage(String repository,
                             ProgressMonitor progressMonitor,
                             AuthConfigs authConfigs,
                             boolean doForcePull,
                             long memoryLimit,
                             long memorySwapLimit,
                             File... files) throws IOException, InterruptedException {
        final File tar = Files.createTempFile(null, ".tar").toFile();
        try {
            createTarArchive(tar, files);
            return doBuildImage(repository, tar, progressMonitor, dockerDaemonUri, authConfigs, doForcePull, memoryLimit, memorySwapLimit);
        } finally {
            FileCleaner.addFile(tar);
        }
    }

    /**
     * Builds new docker image from specified tar archive that must contain Dockerfile.
     *
     * @param repository
     *         full repository name to be applied to newly created image
     * @param tar
     *         archived files that are needed for creation docker images (e.g. file of directories used in ADD instruction in Dockerfile).
     *         One of them must be Dockerfile.
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @param authConfigs
     *         Authentication configuration for private registries. Can be null.
     * @return image id
     * @throws IOException
     * @throws InterruptedException
     *         if build process was interrupted
     * @deprecated use {@link #buildImage(BuildImageParams, ProgressMonitor)} instead
     */
    @Deprecated
    protected String buildImage(String repository,
                                File tar,
                                final ProgressMonitor progressMonitor,
                                AuthConfigs authConfigs,
                                boolean doForcePull) throws IOException, InterruptedException {
        return doBuildImage(repository, tar, progressMonitor, dockerDaemonUri, authConfigs, doForcePull);
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
        return inspectImage(InspectImageParams.from(image));
    }

    /**
     * @deprecated use {@link #inspectImage(InspectImageParams)} instead
     */
    @Deprecated
    protected ImageInfo doInspectImage(String image, URI dockerDaemonUri) throws IOException {
        return inspectImage(InspectImageParams.from(image), dockerDaemonUri);
    }

    /**
     * Gets detailed information about docker image.
     *
     * @return detailed information about {@code image}
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public ImageInfo inspectImage(InspectImageParams params) throws IOException {
        return inspectImage(params, dockerDaemonUri);
    }

    private ImageInfo inspectImage(InspectImageParams params, URI dockerDaemonUri) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/images/" + params.getImage() + "/json")) {
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ImageInfo.class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @deprecated use {@link #removeImage(RemoveImageParams)} instead
     */
    @Deprecated
    public void removeImage(String image, boolean force) throws IOException {
        removeImage(RemoveImageParams.from(image)
                                     .withForce(force));
    }

    /**
     * @deprecated use {@link #tag(TagParams)} instead
     */
    @Deprecated
    public void tag(String image, String repository, String tag) throws IOException {
        tag(TagParams.from(image, repository).withTag(tag), dockerDaemonUri);
    }

    /**
     * Push docker image to the registry
     *
     * @param repository
     *         full repository name to be applied to newly created image
     * @param tag
     *         tag of the image
     * @param registry
     *         registry url
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @return digest of just pushed image
     * @throws IOException
     *          when a problem occurs with docker api calls
     * @throws InterruptedException
     *         if push process was interrupted
     * @deprecated use {@link #push(PushParams, ProgressMonitor)}
     */
    @Deprecated
    public String push(String repository,
                       String tag,
                       String registry,
                       final ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        return doPush(repository, tag, registry, progressMonitor, dockerDaemonUri);
    }

    /**
     * See <a href="https://docs.docker.com/reference/api/docker_remote_api_v1.16/#create-an-image">Docker remote API # Create an
     * image</a>.
     * To pull from private registry use registry.address:port/image as image. This is not documented.
     *
     * @throws IOException
     * @throws InterruptedException
     * @deprecated use {@link #pull(PullParams, ProgressMonitor)} instead
     */
    @Deprecated
    public void pull(String image,
                     String tag,
                     String registry,
                     ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        doPull(image, tag, registry, progressMonitor, dockerDaemonUri);
    }

    /**
     * @deprecated use {@link #createContainer(CreateContainerParams)} instead
     */
    @Deprecated
    public ContainerCreated createContainer(ContainerConfig containerConfig, String containerName) throws IOException {
        return createContainer(CreateContainerParams.from(containerConfig).withContainerName(containerName), dockerDaemonUri);
    }

    /**
     * @deprecated use {@link #startContainer(StartContainerParams)} instead
     */
    @Deprecated
    public void startContainer(String container, HostConfig hostConfig) throws IOException {
        doStartContainer(container, hostConfig, dockerDaemonUri);
    }

    /**
     * Stops container.
     *
     * @param container
     *         container identifier, either id or name
     * @param timeout
     *         time to wait for the container to stop before killing it
     * @param timeunit
     *         time unit of the timeout parameter
     * @throws IOException
     * @deprecated use {@link #stopContainer(StopContainerParams)} instead
     */
    @Deprecated
    public void stopContainer(String container, long timeout, TimeUnit timeunit) throws IOException {
        stopContainer(StopContainerParams.from(container)
                                         .withTimeout(timeout, timeunit));
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
     * Kills running container Kill a running container using specified signal.
     *
     * @param container
     *         container identifier, either id or name
     * @param signal
     *         code of signal, e.g. 9 in case of SIGKILL
     * @throws IOException
     * @deprecated use {@link #killContainer(KillContainerParams)} instead
     */
    @Deprecated
    public void killContainer(String container, int signal) throws IOException {
        killContainer(KillContainerParams.from(container).withSignal(signal));
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
        killContainer(KillContainerParams.from(container));
    }

    /**
     * Removes container.
     *
     * @param container
     *         container identifier, either id or name
     * @param force
     *         if {@code true} kills the running container then remove it
     * @param removeVolumes
     *         if {@code true} removes volumes associated to the container
     * @throws IOException
     *          when a problem occurs with docker api calls
     * @deprecated use {@link #removeContainer(RemoveContainerParams)} instead
     */
    @Deprecated
    public void removeContainer(String container, boolean force, boolean removeVolumes) throws IOException {
        removeContainer(RemoveContainerParams.from(container)
                                             .withForce(force)
                                             .withRemoveVolumes(removeVolumes));
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
        return waitContainer(WaitContainerParams.from(container));
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
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
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
        return inspectContainer(InspectContainerParams.from(container));
    }

    /**
     * @deprecated use {@link #createContainer(CreateContainerParams)} instead
     */
    @Deprecated
    protected ContainerInfo doInspectContainer(String container, URI dockerDaemonUri) throws IOException {
        return inspectContainer(InspectContainerParams.from(container), dockerDaemonUri);
    }

    /**
     * Gets detailed information about docker container.
     *
     * @return detailed information about {@code container}
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public ContainerInfo inspectContainer(final InspectContainerParams params) throws IOException {
        return inspectContainer(params, dockerDaemonUri);
    }

    private ContainerInfo inspectContainer(final InspectContainerParams params, URI dockerDaemonUri) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/containers/" + params.getContainer() + "/json")) {
            addQueryParamIfNotNull(connection, "size", params.isReturnContainerSize());
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerInfo.class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Attaches to the container with specified id.
     *
     * @param container
     *         id of container
     * @param containerLogsProcessor
     *         output for container logs
     * @param stream
     *         if {@code true} then get 'live' stream from container. Typically need to run this method in separate thread, if {@code
     *         stream} is {@code true} since this method blocks until container is running.
     * @throws IOException
     * @deprecated use {@link #attachContainer(AttachContainerParams, MessageProcessor)} instead
     */
    @Deprecated
    public void attachContainer(String container, MessageProcessor<LogMessage> containerLogsProcessor, boolean stream) throws IOException {
        attachContainer(AttachContainerParams.from(container)
                                             .withStream(stream),
                        containerLogsProcessor);
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
     * @deprecated use {@link #commit(CommitParams)} instead
     */
    @Deprecated
    public String commit(String container, String repository, String tag, String comment, String author) throws IOException {
        // todo: pause container
        return commit(CommitParams.from(container, repository).withTag(tag).withComment(comment).withAuthor(author), dockerDaemonUri);
    }

    /**
     * @deprecated use {@link #createExec(CreateExecParams)} instead
     */
    @Deprecated
    public Exec createExec(String container, boolean detach, String... cmd) throws IOException {
        return createExec(CreateExecParams.from(container, cmd)
                                          .withDetach(detach));
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
        byte[] entityBytesArray = JsonHelper.toJson(execConfig, FIRST_LETTER_LOWERCASE).getBytes();

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
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @deprecated use {@link #startExec(StartExecParams, MessageProcessor)} instead
     */
    @Deprecated
    public void startExec(String execId, MessageProcessor<LogMessage> execOutputProcessor) throws IOException {
        startExec(StartExecParams.from(execId), execOutputProcessor);
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
        byte[] entityBytesArray = JsonHelper.toJson(execStart, FIRST_LETTER_LOWERCASE).getBytes();
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
        return getExecInfo(GetExecInfoParams.from(execId));
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
     * @deprecated use {@link #top(TopParams)} instead
     */
    @Deprecated
    public ContainerProcesses top(String container, String... psArgs) throws IOException {
        return top(TopParams.from(container)
                            .withPsArgs(psArgs));
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
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Gets files from the specified container.
     *
     * @param container
     *         container id
     * @param sourcePath
     *         path to file or directory inside specified container
     * @return stream of resources from the specified container filesystem, with retention connection
     * @throws IOException
     *          when a problem occurs with docker api calls
     * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8.0 version
     * @deprecated use {@link #getResource(GetResourceParams)} instead
     */
    @Deprecated
    public InputStream getResource(String container, String sourcePath) throws IOException {
       return getResource(GetResourceParams.from(container, sourcePath));
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
     * @param container
     *         container id
     * @param targetPath
     *         path to file or directory inside specified container
     * @param sourceStream
     *         stream of files from source container
     * @param noOverwriteDirNonDir
     *         If "false" then it will be an error if unpacking the given content would cause
     *         an existing directory to be replaced with a non-directory or other resource and vice versa.
     * @throws IOException
     *          when a problem occurs with docker api calls, or during file system operations
     * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8 version
     * @deprecated use {@link #putResource(PutResourceParams)} instead
     */
    @Deprecated
    public void putResource(String container,
                            String targetPath,
                            InputStream sourceStream,
                            boolean noOverwriteDirNonDir) throws IOException {
       putResource(PutResourceParams.from(container, targetPath)
                                    .withSourceStream(sourceStream)
                                    .withNoOverwriteDirNonDir(noOverwriteDirNonDir));
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
     * @param sinceSecond
     *         UNIX date in seconds. allow omit events created before specified date.
     * @param untilSecond
     *         UNIX date in seconds. allow omit events created after specified date.
     * @param filters
     *         filter of needed events. Available filters: {@code event=<string>}
     *         {@code image=<string>} {@code container=<string>}
     * @param messageProcessor
     *         processor of all found events that satisfy specified parameters
     * @throws IOException
     * @deprecated use {@link #getEvents(GetEventsParams, MessageProcessor)} instead
     */
    @Deprecated
    public void getEvents(long sinceSecond,
                          long untilSecond,
                          Filters filters,
                          MessageProcessor<Event> messageProcessor) throws IOException {
        getEvents(GetEventsParams.from()
                                 .withSinceSecond(sinceSecond)
                                 .withUntilSecond(untilSecond)
                                 .withFilters(filters),
                  messageProcessor);
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
                connection.query("filters", urlPathSegmentEscaper().escape(JsonHelper.toJson(filters.getFilters())));
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
     * The same as {@link #doBuildImage(String, File, ProgressMonitor, URI, AuthConfigs, boolean, long, long)} but without memory limits.
     *
     * @see #doBuildImage(String, File, ProgressMonitor, URI, AuthConfigs, boolean, long, long)
     * @deprecated use {@link #}
     */
    @Deprecated
    protected String doBuildImage(String repository,
                                  File tar,
                                  final ProgressMonitor progressMonitor,
                                  URI dockerDaemonUri,
                                  AuthConfigs authConfigs,
                                  boolean doForcePull) throws IOException, InterruptedException {
        return doBuildImage(repository, tar, progressMonitor, dockerDaemonUri, authConfigs, doForcePull, 0, 0);
    }

    /**
     * Builds new docker image from specified tar archive that must contain Dockerfile.
     *
     * @param repository
     *         full repository name to be applied to newly created image
     * @param tar
     *         archived files that are needed for creation docker images (e.g. file of directories used in ADD instruction in Dockerfile).
     *         One of them must be Dockerfile.
     * @param progressMonitor
     *         ProgressMonitor for images creation process
     * @param dockerDaemonUri
     *         Uri for remote access to docker API
     * @param authConfigs
     *         Authentication configuration for private registries. Can be null.
     * @param memoryLimit
     *         Memory limit for build in bytes
     * @param memorySwapLimit
     *         Total memory in bytes (memory + swap), -1 to enable unlimited swap
     * @return image id
     * @throws IOException
     * @throws InterruptedException
     *         if build process was interrupted
     * @deprecated use {@link #buildImage(BuildImageParams, ProgressMonitor)} instead
     */
    @Deprecated
    protected String doBuildImage(String repository,
                                  File tar,
                                  final ProgressMonitor progressMonitor,
                                  URI dockerDaemonUri,
                                  AuthConfigs authConfigs,
                                  boolean doForcePull,
                                  long memoryLimit,
                                  long memorySwapLimit) throws IOException, InterruptedException {
        return buildImage(BuildImageParams.from(new File[]{new File(".")}) // used tar instead it
                                          .withRepository(repository)
                                          .withAuthConfigs(authConfigs)
                                          .withDoForcePull(doForcePull)
                                          .withMemoryLimit(memoryLimit)
                                          .withMemorySwapLimit(memorySwapLimit),
                          progressMonitor,
                          tar,
                          dockerDaemonUri);
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
        final File tar = Files.createTempFile(null, ".tar").toFile();
        try {
            File[] files = new File[params.getFiles().size()];
            files = params.getFiles().toArray(files);
            createTarArchive(tar, files);
            return buildImage(params, progressMonitor, tar, dockerDaemonUri);
        } finally {
            FileCleaner.addFile(tar);
        }
    }

    private String buildImage(final BuildImageParams params,
                              final ProgressMonitor progressMonitor,
                              File tar, // tar from params.files() (uses temporary until delete deprecated methods)
                              URI dockerDaemonUri) throws IOException, InterruptedException {
        AuthConfigs authConfigs = firstNonNull(params.getAuthConfigs(), initialAuthConfig.getAuthConfigs());
        try (InputStream tarInput = new FileInputStream(tar);
             DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/build")
                                                            .query("rm", 1)
                                                            .query("forcerm", 1)
                                                            .header("Content-Type", "application/x-compressed-tar")
                                                            .header("Content-Length", tar.length())
                                                            .header("X-Registry-Config",
                                                                    Base64.encodeBase64String(JsonHelper.toJson(authConfigs)
                                                                                                        .getBytes()))
                                                            .entity(tarInput)) {
            addQueryParamIfNotNull(connection, "t", params.getRepository());
            addQueryParamIfNotNull(connection, "memory", params.getMemoryLimit());
            addQueryParamIfNotNull(connection, "memswap", params.getMemorySwapLimit());
            addQueryParamIfNotNull(connection, "pull", params.isDoForcePull());
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            try (InputStream responseStream = response.getInputStream()) {
                JsonMessageReader<ProgressStatus> progressReader = new JsonMessageReader<>(responseStream, ProgressStatus.class);

                final ValueHolder<String> imageIdHolder = new ValueHolder<>();
                executeInSeparateThread(() -> {
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
                        throw new RuntimeException(e.getLocalizedMessage(), e);
                    }
                });

                if (imageIdHolder.get() == null) {
                    throw new IOException("Docker image build failed");
                }
                return imageIdHolder.get();
            }
        }
    }

    /**
     * @deprecated use {@link #removeImage(RemoveImageParams)} instead
     */
    @Deprecated
    protected void doRemoveImage(String image, boolean force, URI dockerDaemonUri) throws IOException {
        removeImage(RemoveImageParams.from(image).withForce(force), dockerDaemonUri);
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
        removeImage(RemoveImageParams.from(image));
    }

    /**
     * Removes docker image.
     *
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public void removeImage(final RemoveImageParams params) throws IOException {
        removeImage(params, dockerDaemonUri);
    }

    private void removeImage(final RemoveImageParams params,URI dockerDaemonUri) throws IOException {
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
     * @deprecated use {@link #tag(TagParams)}
     */
    @Deprecated
    protected void doTag(String image, String repository, String tag, URI dockerDaemonUri) throws IOException {
        tag(TagParams.from(image, repository).withTag(tag), dockerDaemonUri);
    }

    /**
     * Tag the docker image into a repository.
     *
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public void tag(final TagParams params) throws IOException {
        tag(params, dockerDaemonUri);
    }

    private void tag(final TagParams params, URI dockerDaemonUri) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/" + params.getImage() + "/tag")
                                                            .query("repo", params.getRepository())) {
            addQueryParamIfNotNull(connection, "force", params.isForce());
            addQueryParamIfNotNull(connection, "tag", params.getTag());
            final DockerResponse response = connection.request();
            if (response.getStatus() / 100 != 2) {
                throw getDockerException(response);
            }
        }
    }

    /**
     * @deprecated use {@link #push(PushParams, ProgressMonitor)} instead
     */
    @Deprecated
    protected String doPush(final String repository,
                            final String tag,
                            final String registry,
                            final ProgressMonitor progressMonitor,
                            final URI dockerDaemonUri) throws IOException, InterruptedException {
        return push(PushParams.from(repository)
                              .withTag(tag)
                              .withRegistry(registry),
                    progressMonitor,
                    dockerDaemonUri);
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
        return push(params, progressMonitor, dockerDaemonUri);
    }

    private String push(final PushParams params, final ProgressMonitor progressMonitor, URI dockerDaemonUri)
            throws IOException, InterruptedException {
        final String fullRepo = (params.getRegistry() != null) ?
                                params.getRegistry() + '/' + params.getRepository() : params.getRepository();
        final ValueHolder<String> digestHolder = new ValueHolder<>();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/" + fullRepo + "/push")
                                                            .header("X-Registry-Auth", initialAuthConfig.getAuthConfigHeader())) {
            addQueryParamIfNotNull(connection, "tag", params.getTag());
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            try (InputStream responseStream = response.getInputStream()) {
                JsonMessageReader<ProgressStatus> progressReader = new JsonMessageReader<>(responseStream, ProgressStatus.class);

                executeInSeparateThread(() -> {
                    try {
                        String digestPrefix = firstNonNull(params.getTag(), "latest") + ": digest: ";
                        ProgressStatus progressStatus;
                        while ((progressStatus = progressReader.next()) != null) {
                            progressMonitor.updateProgress(progressStatus);
                            if (progressStatus.getError() != null) {
                                throw new RuntimeException(progressStatus.getError());
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
                        throw new RuntimeException(e.getLocalizedMessage(), e);
                    }
                });
                if (digestHolder.get() == null) {
                    LOG.error("Docker image {}:{} was successfully pushed, but its digest wasn't obtained",
                              fullRepo,
                              firstNonNull(params.getTag(), "latest"));
                    throw new DockerException("Docker image was successfully pushed, but its digest wasn't obtained", 500);
                } else {
                    return digestHolder.get();
                }
            }
        }
    }

    /**
     * @deprecated use {@link #commit(CommitParams)} instead
     */
    @Deprecated
    protected String doCommit(String container,
                              String repository,
                              String tag,
                              String comment,
                              String author,
                              URI dockerDaemonUri) throws IOException {
        // todo: pause container
        return commit(CommitParams.from(container, repository)
                                  .withTag(tag)
                                  .withComment(comment)
                                  .withAuthor(author),
                      dockerDaemonUri);
    }

    /**
     * Creates a new image from a container’s changes.
     *
     * @return id of a new image
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public String commit(final CommitParams params) throws IOException {
        return commit(params, dockerDaemonUri);
    }

    private String commit(final CommitParams params, URI dockerDaemonUri) throws IOException {
        // TODO: add option to pause container
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/commit")
                                                            .query("container", params.getContainer())
                                                            .query("repo", params.getRepository())) {
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
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * See <a href="https://docs.docker.com/reference/api/docker_remote_api_v1.16/#create-an-image">Docker remote API # Create an
     * image</a>.
     * To pull from private registry use registry.address:port/image as image. This is not documented.
     *
     * @throws IOException
     * @throws InterruptedException
     * @deprecated use {@link #pull(PullParams, ProgressMonitor)} instead
     */
    @Deprecated
    protected void doPull(String image,
                          String tag,
                          String registry,
                          final ProgressMonitor progressMonitor,
                          URI dockerDaemonUri) throws IOException, InterruptedException {
        pull(PullParams.from(image)
                       .withTag(tag)
                       .withRegistry(registry),
             progressMonitor,
             dockerDaemonUri);
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
    private void pull(final PullParams params,
                      final ProgressMonitor progressMonitor,
                      final URI dockerDaemonUri) throws IOException, InterruptedException {
        final String image = params.getImage();
        final String registry = params.getRegistry();

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/create")
                                                            .query("fromImage", registry != null ? registry + '/' + image : image)
                                                            .header("X-Registry-Auth", initialAuthConfig.getAuthConfigHeader())) {
            addQueryParamIfNotNull(connection, "tag", params.getTag());
            final DockerResponse response = connection.request();
            if (OK.getStatusCode() != response.getStatus()) {
                throw getDockerException(response);
            }
            try (InputStream responseStream = response.getInputStream()) {
                JsonMessageReader<ProgressStatus> progressReader = new JsonMessageReader<>(responseStream, ProgressStatus.class);

                executeInSeparateThread(() -> {
                    try {
                        ProgressStatus progressStatus;
                        while ((progressStatus = progressReader.next()) != null) {
                            progressMonitor.updateProgress(progressStatus);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e.getLocalizedMessage(), e);
                    }
                });
            }
        }
    }

    /**
     * The same as {@link #createContainer(CreateContainerParams)} but with docker service uri parameter
     *
     * @param dockerDaemonUri
     *         docker service URI
     * @deprecated use {@link #createContainer(CreateContainerParams)} instead
     */
    @Deprecated
    protected ContainerCreated doCreateContainer(ContainerConfig containerConfig,
                                                 String containerName,
                                                 URI dockerDaemonUri) throws IOException {
        return createContainer(CreateContainerParams.from(containerConfig)
                                                    .withContainerName(containerName),
                               dockerDaemonUri);
    }

    /**
     * Creates docker container.
     *
     * @return information about just created container
     * @throws IOException
     *          when a problem occurs with docker api calls
     */
    public ContainerCreated createContainer(final CreateContainerParams params) throws IOException {
        return createContainer(params, dockerDaemonUri);
    }

    private ContainerCreated createContainer(final CreateContainerParams params, URI dockerDaemonUri) throws IOException {
        byte[] entityBytesArray = JsonHelper.toJson(params.getContainerConfig(), FIRST_LETTER_LOWERCASE).getBytes();

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
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @deprecated use {@link #startContainer(StartContainerParams)} instead
     */
    @Deprecated
    protected void doStartContainer(String container,
                                    HostConfig hostConfig,
                                    URI dockerDaemonUri) throws IOException {
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        final String entity = (hostConfig == null) ? "{}" : JsonHelper.toJson(hostConfig, FIRST_LETTER_LOWERCASE);
        byte[] entityBytesArray = entity.getBytes();
        headers.add(Pair.of("Content-Length", entityBytesArray.length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/start")
                                                            .headers(headers)
                                                            .entity(entityBytesArray)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (!(NO_CONTENT.getStatusCode() == status || NOT_MODIFIED.getStatusCode() == status)) {

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

    @VisibleForTesting
    <T> T parseResponseStreamAndClose(InputStream inputStream, Class<T> clazz) throws IOException, JsonParseException {
        try (InputStream responseStream = inputStream) {
            return JsonHelper.fromJson(responseStream,
                                       clazz,
                                       null,
                                       FIRST_LETTER_LOWERCASE);
        }
    }

    @VisibleForTesting
    @SuppressWarnings("unchecked")
    <T> List<T> parseResponseStreamAsListAndClose(InputStream inputStream, Type type) throws IOException, JsonParseException {
        try (InputStream responseStream = inputStream) {
            return (List<T>)JsonHelper.fromJson(responseStream,
                                                List.class,
                                                type,
                                                FIRST_LETTER_LOWERCASE);
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

    // Unfortunately we can't use generated DTO here.
    // Docker uses uppercase in first letter in names of json objects, e.g. {"Id":"123"} instead of {"id":"123"}
    protected static JsonNameConvention FIRST_LETTER_LOWERCASE = new JsonNameConvention() {
        @Override
        public String toJsonName(String javaName) {
            return Character.toUpperCase(javaName.charAt(0)) + javaName.substring(1);
        }

        @Override
        public String toJavaName(String jsonName) {
            return Character.toLowerCase(jsonName.charAt(0)) + jsonName.substring(1);
        }
    };

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

    // TODO add comments to each line :)

    // todo check when closed by interruption exception is thrown

    // Here do some trick to be able interrupt build process. Basically for now it is not possible interrupt docker daemon while
    // it's building images but here we need just be able to close connection to the unix socket. Thread is blocking while read
    // from the socket stream so need one more thread that is able to close socket. In this way we can release thread that is
    // blocking on i/o.

    /**
     * This method runs provided runnable in a separate thread in a such a way that that thread can be interrupted.
     * It is supposed that it runs long-running task and at some point it can be stopped.
     * <br/>Provided task should throw unchecked exception if exceptional situation happens.
     * In that case {@link IOException#IOException(String, Throwable)} will be thrown
     * with {@link Throwable#getLocalizedMessage()} and unchecked exception as arguments.
     */
    private void executeInSeparateThread(Runnable task) throws InterruptedException, IOException {
        final ValueHolder<Throwable> errorHolder = new ValueHolder<>();
        final ValueHolder<Boolean> parentThreadNotifiedFlagHolder = new ValueHolder<>(false);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Throwable e) {
                    errorHolder.set(e);
                } finally {
                    synchronized (this) {
                        notify();
                        parentThreadNotifiedFlagHolder.set(true);
                    }
                }
            }
        };
        // noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (runnable) {
            executor.execute(runnable);
            //noinspection ThrowableResultOfMethodCallIgnored
            while (errorHolder.get() == null && !parentThreadNotifiedFlagHolder.get()) {
                runnable.wait();
            }
        }
        final Throwable throwable = errorHolder.get();
        if (throwable != null) {
            throw new IOException(throwable.getLocalizedMessage(), throwable);
        }
    }
}
