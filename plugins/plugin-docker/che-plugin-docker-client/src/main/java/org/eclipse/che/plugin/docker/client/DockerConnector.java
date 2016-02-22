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

import org.apache.commons.codec.binary.Base64;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.core.util.ValueHolder;
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
import org.eclipse.che.plugin.docker.client.json.ContainerCommited;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerExitStatus;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerProcesses;
import org.eclipse.che.plugin.docker.client.json.ContainerResource;
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
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
     */
    public org.eclipse.che.plugin.docker.client.json.SystemInfo getSystemInfo() throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/info")) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
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
     */
    public Version getVersion() throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/version")) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
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
     */
    public Image[] listImages() throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/images/json")) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), Image[].class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
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
     * @param files
     *         files that are needed for creation docker images (e.g. file of directories used in ADD instruction in Dockerfile), one of
     *         them must be Dockerfile.
     * @return image id
     * @throws IOException
     * @throws InterruptedException
     *         if build process was interrupted
     */
    public String buildImage(String repository,
                             ProgressMonitor progressMonitor,
                             AuthConfigs authConfigs,
                             boolean doForcePull,
                             File... files) throws IOException, InterruptedException {
        final File tar = Files.createTempFile(null, ".tar").toFile();
        try {
            createTarArchive(tar, files);
            return buildImage(repository, tar, progressMonitor, authConfigs, doForcePull);
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
     */
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
        return doInspectImage(image, dockerDaemonUri);
    }

    protected ImageInfo doInspectImage(String image, URI dockerDaemonUri) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/images/" + image + "/json")) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ImageInfo.class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    public void removeImage(String image, boolean force) throws IOException {
        doRemoveImage(image, force, dockerDaemonUri);
    }

    public void tag(String image, String repository, String tag) throws IOException {
        doTag(image, repository, tag, dockerDaemonUri);
    }

    public void push(String repository,
                     String tag,
                     String registry,
                     final ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        doPush(repository, tag, registry, progressMonitor, dockerDaemonUri);
    }

    /**
     * See <a href="https://docs.docker.com/reference/api/docker_remote_api_v1.16/#create-an-image">Docker remote API # Create an
     * image</a>.
     * To pull from private registry use registry.address:port/image as image. This is not documented.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void pull(String image,
                     String tag,
                     String registry,
                     ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        doPull(image, tag, registry, progressMonitor, dockerDaemonUri);
    }

    public ContainerCreated createContainer(ContainerConfig containerConfig, String containerName) throws IOException {
        return doCreateContainer(containerConfig, containerName, dockerDaemonUri);
    }

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
     */
    public void stopContainer(String container, long timeout, TimeUnit timeunit) throws IOException {
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/stop")
                                                            .query("t", timeunit.toSeconds(timeout))
                                                            .headers(headers)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (!(NO_CONTENT.getStatusCode() == status || NOT_MODIFIED.getStatusCode() == status)) {
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
     */
    public void killContainer(String container, int signal) throws IOException {
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/kill")
                                                            .query("signal", signal)
                                                            .headers(headers)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (NO_CONTENT.getStatusCode() != status) {
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
        killContainer(container, 9);
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
     */
    public void removeContainer(String container, boolean force, boolean removeVolumes) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("DELETE")
                                                            .path("/containers/" + container)
                                                            .query("force", force ? 1 : 0)
                                                            .query("v", removeVolumes ? 1 : 0)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (NO_CONTENT.getStatusCode() != status) {
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
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/wait")
                                                            .headers(headers)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
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
        return doInspectContainer(container, dockerDaemonUri);
    }

    protected ContainerInfo doInspectContainer(String container, URI dockerDaemonUri) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/containers/" + container + "/json")) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
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
     * @throws java.io.IOException
     */
    public void attachContainer(String container, MessageProcessor<LogMessage> containerLogsProcessor, boolean stream) throws IOException {
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/attach")
                                                            .query("stream", (stream ? 1 : 0))
                                                            .query("logs", (stream ? 0 : 1))
                                                            .query("stdout", 1)
                                                            .query("stderr", 1)
                                                            .headers(headers)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            try (InputStream responseStream = response.getInputStream()) {
                new LogMessagePumper(responseStream, containerLogsProcessor).start();
            }
        }
    }

    public String commit(String container, String repository, String tag, String comment, String author) throws IOException {
        // todo: pause container
        return doCommit(container, repository, tag, comment, author, dockerDaemonUri);
    }

    /**
     * Copies file or directory {@code path} from {@code container} to the {code hostPath}.
     *
     * @param container
     *         container id
     * @param path
     *         path to file or directory inside container
     * @param hostPath
     *         path to the directory on host filesystem
     * @throws IOException
     * @deprecated since 1.20 docker api in favor of the {@link #getResource(String, String)}
     * and {@link #putResource(String, String, InputStream, boolean) putResource}
     */
    @Deprecated
    public void copy(String container, String path, File hostPath) throws IOException {
        final String entity = JsonHelper.toJson(new ContainerResource().withResource(path), FIRST_LETTER_LOWERCASE);
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path(String.format("/containers/%s/copy", container))
                                                            .headers(headers)
                                                            .entity(entity)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            // TarUtils uses apache commons compress library for working with tar archive and it fails
            // (e.g. doesn't unpack all files from archive in case of coping directory) when we try to use stream from docker remote API.
            // Docker sends tar contents as sequence of chunks and seems that causes problems for apache compress library.
            // The simplest solution is spool content to temporary file and then unpack it to destination folder.
            final Path spoolFilePath = Files.createTempFile("docker-copy-spool-", ".tar");
            try (InputStream is = response.getInputStream()) {
                Files.copy(is, spoolFilePath, StandardCopyOption.REPLACE_EXISTING);
                try (InputStream tarStream = Files.newInputStream(spoolFilePath)) {
                    TarUtils.untar(tarStream, hostPath);
                }
            } finally {
                FileCleaner.addFile(spoolFilePath.toFile());
            }
        }
    }

    public Exec createExec(String container, boolean detach, String... cmd) throws IOException {
        final ExecConfig execConfig = new ExecConfig().withCmd(cmd);
        if (!detach) {
            execConfig.withAttachStderr(true).withAttachStdout(true);
        }
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        final String entity = JsonHelper.toJson(execConfig, FIRST_LETTER_LOWERCASE);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/exec")
                                                            .headers(headers)
                                                            .entity(entity)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (status / 100 != 2) {
                throw getDockerException(response);
            }
            return new Exec(cmd, parseResponseStreamAndClose(response.getInputStream(), ExecCreated.class).getId());
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    public void startExec(String execId, MessageProcessor<LogMessage> execOutputProcessor) throws IOException {
        final ExecStart execStart = new ExecStart().withDetach(execOutputProcessor == null);
        final String entity = JsonHelper.toJson(execStart, FIRST_LETTER_LOWERCASE);
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/exec/" + execId + "/start")
                                                            .headers(headers)
                                                            .entity(entity)) {
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
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/exec/" + execId + "/json")) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ExecInfo.class);
        } catch (Exception e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    public ContainerProcesses top(String container, String... psArgs) throws IOException {
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));
        final DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                             .method("GET")
                                                             .path("/containers/" + container + "/top")
                                                             .headers(headers);
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

        try {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerProcesses.class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        } finally {
            connection.close();
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
     *         when problems occurs with docker api calls
     * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8.0 version
     */
    public InputStream getResource(String container, String sourcePath) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/containers/" + container + "/archive")
                                                            .query("path", sourcePath)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (status != OK.getStatusCode()) {
                throw getDockerException(response);
            }

            return new CloseConnectionInputStream(response.getInputStream(), connection);
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
     *         when problems occurs with docker api calls, or during file system operations
     * @apiNote this method implements 1.20 docker API and requires docker not less than 1.8 version
     */
    public void putResource(String container,
                            String targetPath,
                            InputStream sourceStream,
                            boolean noOverwriteDirNonDir) throws IOException {
        File tarFile;
        long length;
        try (InputStream sourceData = sourceStream) {
            Path tarFilePath = Files.createTempFile("compressed-resources", ".tar");
            tarFile = tarFilePath.toFile();
            length = Files.copy(sourceData, tarFilePath, StandardCopyOption.REPLACE_EXISTING);
        }

        List<Pair<String, ?>> headers = Arrays.asList(Pair.of("Content-Type", ExtMediaType.APPLICATION_X_TAR),
                                                      Pair.of("Content-Length", length));
        try (InputStream tarStream = new BufferedInputStream(new FileInputStream(tarFile));
             DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("PUT")
                                                            .path("/containers/" + container + "/archive")
                                                            .query("path", targetPath)
                                                            .query("noOverwriteDirNonDir", noOverwriteDirNonDir ? 0 : 1)
                                                            .headers(headers)
                                                            .entity(tarStream)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (status != OK.getStatusCode()) {
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
     */
    public void getEvents(long sinceSecond,
                          long untilSecond,
                          Filters filters,
                          MessageProcessor<Event> messageProcessor) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("GET")
                                                            .path("/events")) {
            if (sinceSecond != 0) {
                connection.query("since", sinceSecond);
            }
            if (untilSecond != 0) {
                connection.query("until", untilSecond);
            }
            if (filters != null) {
                connection.query("filters", urlPathSegmentEscaper().escape(JsonHelper.toJson(filters.getFilters())));
            }
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }

            try (InputStream responseStream = response.getInputStream()) {
                new MessagePumper<>(new JsonMessageReader<>(responseStream, Event.class), messageProcessor).start();
            }
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
     * @param dockerDaemonUri
     *         Uri for remote access to docker API
     * @param authConfigs
     *         Authentication configuration for private registries. Can be null.
     * @return image id
     * @throws IOException
     * @throws InterruptedException
     *         if build process was interrupted
     */
    protected String doBuildImage(String repository,
                                  File tar,
                                  final ProgressMonitor progressMonitor,
                                  URI dockerDaemonUri,
                                  AuthConfigs authConfigs,
                                  boolean doForcePull) throws IOException, InterruptedException {
        if (authConfigs == null) {
            authConfigs = initialAuthConfig.getAuthConfigs();
        }
        final List<Pair<String, ?>> headers = new ArrayList<>(3);
        headers.add(Pair.of("Content-Type", "application/x-compressed-tar"));
        headers.add(Pair.of("Content-Length", tar.length()));
        headers.add(Pair.of("X-Registry-Config", Base64.encodeBase64String(JsonHelper.toJson(authConfigs).getBytes())));

        try (InputStream tarInput = new FileInputStream(tar);
             DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/build")
                                                            .query("rm", 1)
                                                            .query("pull", doForcePull)
                                                            .headers(headers)
                                                            .entity(tarInput)) {
            if (repository != null) {
                connection.query("t", repository);
            }
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
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

    protected void doRemoveImage(String image, boolean force, URI dockerDaemonUri) throws IOException {
        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("DELETE")
                                                            .path("/images/" + image)
                                                            .query("force", force ? 1 : 0)) {
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
                throw getDockerException(response);
            }
        }
    }

    protected void doTag(String image, String repository, String tag, URI dockerDaemonUri) throws IOException {
        final List<Pair<String, ?>> headers = new ArrayList<>(3);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/" + image + "/tag")
                                                            .query("repo", repository)
                                                            .query("force", 0)
                                                            .headers(headers)) {
            if (tag != null) {
                connection.query("tag", tag);
            }
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (status / 100 != 2) {
                throw getDockerException(response);
            }
        }
    }

    protected void doPush(final String repository,
                          final String tag,
                          final String registry,
                          final ProgressMonitor progressMonitor,
                          final URI dockerDaemonUri) throws IOException, InterruptedException {
        final List<Pair<String, ?>> headers = new ArrayList<>(3);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));
        headers.add(Pair.of("X-Registry-Auth", initialAuthConfig.getAuthConfigHeader()));
        final String fullRepo = registry != null ? registry + "/" + repository : repository;

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/" + fullRepo + "/push")
                                                            .headers(headers)) {
            if (tag != null) {
                connection.query("tag", tag);
            }
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
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
                            ProgressStatus progressStatus;
                            while ((progressStatus = progressReader.next()) != null && exceptionHolder.get() == null) {
                                progressMonitor.updateProgress(progressStatus);
                                if (progressStatus.getError() != null) {
                                    exceptionHolder.set(progressStatus.getError());
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
                final IOException ioe = errorHolder.get();
                if (ioe != null) {
                    throw ioe;
                }
            }
        }
    }

    protected String doCommit(String container,
                              String repository,
                              String tag,
                              String comment,
                              String author,
                              URI dockerDaemonUri) throws IOException {

        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        final String entity = "{}";
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/commit")
                                                            .query("container", container)
                                                            .query("repo", repository)
                                                            .headers(headers)
                                                            .entity(entity)) {
            if (tag != null) {
                connection.query("tag", tag);
            }
            if (comment != null) {
                connection.query("comment", URLEncoder.encode(comment, "UTF-8"));
            }
            if (comment != null) {
                connection.query("author", URLEncoder.encode(author, "UTF-8"));
            }
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (CREATED.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerCommited.class).getId();
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    protected void doPull(String image,
                          String tag,
                          String registry,
                          final ProgressMonitor progressMonitor,
                          URI dockerDaemonUri) throws IOException, InterruptedException {
        final List<Pair<String, ?>> headers = new ArrayList<>(3);
        headers.add(Pair.of("Content-Type", MediaType.TEXT_PLAIN));
        headers.add(Pair.of("Content-Length", 0));
        headers.add(Pair.of("X-Registry-Auth", initialAuthConfig.getAuthConfigHeader()));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/images/create")
                                                            .query("fromImage", registry != null ? registry + "/" + image : image)
                                                            .headers(headers)) {
            if (tag != null) {
                connection.query("tag", tag);
            }
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (OK.getStatusCode() != status) {
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

    protected ContainerCreated doCreateContainer(ContainerConfig containerConfig,
                                                 String containerName,
                                                 URI dockerDaemonUri) throws IOException {
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        final String entity = JsonHelper.toJson(containerConfig, FIRST_LETTER_LOWERCASE);
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/create")
                                                            .headers(headers)
                                                            .entity(entity)) {
            if (containerName != null) {
                connection.query("name", containerName);
            }
            final DockerResponse response = connection.request();
            final int status = response.getStatus();
            if (CREATED.getStatusCode() != status) {
                throw getDockerException(response);
            }
            return parseResponseStreamAndClose(response.getInputStream(), ContainerCreated.class);
        } catch (JsonParseException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    protected void doStartContainer(String container,
                                    HostConfig hostConfig,
                                    URI dockerDaemonUri) throws IOException {
        final List<Pair<String, ?>> headers = new ArrayList<>(2);
        headers.add(Pair.of("Content-Type", MediaType.APPLICATION_JSON));
        final String entity = hostConfig == null ? "{}" : JsonHelper.toJson(hostConfig, FIRST_LETTER_LOWERCASE);
        headers.add(Pair.of("Content-Length", entity.getBytes().length));

        try (DockerConnection connection = connectionFactory.openConnection(dockerDaemonUri)
                                                            .method("POST")
                                                            .path("/containers/" + container + "/start")
                                                            .headers(headers)
                                                            .entity(entity)) {
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

    private <T> T parseResponseStreamAndClose(InputStream inputStream, Class<T> clazz) throws IOException, JsonParseException {
        try (InputStream responseStream = inputStream) {
            return JsonHelper.fromJson(responseStream,
                                       clazz,
                                       null,
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
}
