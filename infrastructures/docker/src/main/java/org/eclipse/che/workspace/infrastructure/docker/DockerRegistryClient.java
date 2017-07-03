/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorProvider;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.params.RemoveImageParams;
import org.eclipse.che.workspace.infrastructure.docker.snapshot.SnapshotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver.DEFAULT_REGISTRY_SYNONYMS;

/**
 * @author Alexander Garagatyi
 */
public class DockerRegistryClient {
    private static final Logger LOG = LoggerFactory.getLogger(DockerRegistryClient.class);

    /**
     * dockerfile type support with recipe being a content of Dockerfile
     */
    public static final String DOCKER_FILE_TYPE = "dockerfile";

    /**
     * image type support with recipe script being the name of the repository + image name
     */
    public static final String DOCKER_IMAGE_TYPE = "image";

    /**
     * Prefix of image repository, used to identify that the image is a machine saved to snapshot.
     */
    public static final String MACHINE_SNAPSHOT_PREFIX = "machine_snapshot_";

    public static final String DOCKER_HUB_BASE_URI = "index.docker.io";

    private final DockerConnector                               docker;
    private final DockerRegistryAuthResolver                    authResolver;
    private final boolean                                       snapshotUseRegistry;

    @Inject
    public DockerRegistryClient(DockerConnectorProvider dockerProvider,
                                DockerRegistryAuthResolver authResolver,
                                @Named("che.docker.registry_for_snapshots") boolean snapshotUseRegistry) throws IOException {
        this.docker = dockerProvider.get();
        this.authResolver = authResolver;
        this.snapshotUseRegistry = snapshotUseRegistry;
    }


    /**
     * Removes snapshot of the instance in implementation specific way.
     *
     * @param machineSource
     *         contains implementation specific key of the snapshot of the instance that should be removed
     * @throws SnapshotException
     *         if exception occurs on instance snapshot removal
     */
    public void removeInstanceSnapshot(final MachineSource machineSource) throws SnapshotException {
        // use registry API directly because docker doesn't have such API yet
        // https://github.com/docker/docker-registry/issues/45
        final DockerMachineSource dockerMachineSource;
        try {
            dockerMachineSource = new DockerMachineSource(machineSource);
        } catch (Exception e) {
            throw new SnapshotException(e);
        }

        if (!snapshotUseRegistry) {
            try {
                docker.removeImage(RemoveImageParams.create(dockerMachineSource.getLocation(false)));
            } catch (IOException ignore) {
            }
            return;
        }

        final String repository = dockerMachineSource.getRepository();
        if (repository == null) {
            LOG.error("Failed to remove instance snapshot: invalid machine source: {}", dockerMachineSource);
            throw new SnapshotException("Snapshot removing failed. Snapshot attributes are not valid");
        }

        if (DEFAULT_REGISTRY_SYNONYMS.contains(dockerMachineSource.getRegistry())) {
            removeSnapshotFromDockerHub(repository);
        } else {
            removeSnapshotFromRegistry(dockerMachineSource);
        }
    }

    /**
     * Removes image from unsecured docker registry.
     * This method removes only manifests from registry, but no blobs.
     * To free disk space it is required to use garbage collection,
     * see <a href="https://docs.docker.com/registry/garbage-collection/#how-garbage-collection-works">here</a>
     *
     * @param dockerMachineSource
     *         contains information about snapshot that should be removed
     * @throws SnapshotException
     *         when an error occurs while deleting snapshot
     */
    private void removeSnapshotFromRegistry(final DockerMachineSource dockerMachineSource) throws SnapshotException {
        try {
            URL url = UriBuilder.fromUri("http://" + dockerMachineSource.getRegistry()) // TODO make possible to use https here
                                .path("/v2/{repository}/manifests/{digest}")
                                .build(dockerMachineSource.getRepository(), dockerMachineSource.getDigest())
                                .toURL();
            final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            try {
                conn.setConnectTimeout(30 * 1000);
                conn.setRequestMethod("DELETE");
                // TODO add auth header for secured registry
                final int responseCode = conn.getResponseCode();
                if ((responseCode / 100) != 2) {
                    InputStream in = conn.getErrorStream();
                    if (in == null) {
                        in = conn.getInputStream();
                    }
                    LOG.error("An error occurred while deleting snapshot with url: {}\nError stream: {}",
                              url,
                              IoUtil.readAndCloseQuietly(in));
                    throw new SnapshotException("Internal server error occurs. Can't remove snapshot");
                }
            } finally {
                conn.disconnect();
            }
        } catch (IOException e) {
            LOG.error("Failed to remove {} snapshot from {}. Cause: {}",
                      dockerMachineSource.getRepository(),
                      dockerMachineSource.getRegistry(),
                      e.getLocalizedMessage(),
                      e);
        }
    }

    /**
     * Removes snapshot repository from docker hub.
     *
     * @param repository
     *         snapshot repository name
     * @throws SnapshotException
     *         when an error occurs while deleting snapshot
     */
    private void removeSnapshotFromDockerHub(String repository) throws SnapshotException {
        try {
            URL url = UriBuilder.fromUri("https://" + DOCKER_HUB_BASE_URI)
                                // we use v1 here because docker hub doesn't provide open v2 REST API for repository deletion
                                .path("/v1/repositories/{repository}/")
                                .build(repository)
                                .toURL();
            final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            try {
                conn.setConnectTimeout(30 * 1000);
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Authorization", authResolver.getBasicAuthHeaderValue(DOCKER_HUB_BASE_URI, null));
                final int responseCode = conn.getResponseCode();
                if (responseCode != 202 && responseCode != 404) { // if snapshot is already deleted then just skip it
                    InputStream in = conn.getErrorStream();
                    if (in == null) {
                        in = conn.getInputStream();
                    }
                    LOG.error("An error occurred while deleting snapshot with url: {}\nError stream: {}",
                              url,
                              IoUtil.readAndCloseQuietly(in));
                    throw new SnapshotException("Internal server error occurs. Can't remove snapshot");
                }
            } finally {
                conn.disconnect();
            }
        } catch (IOException e) {
            LOG.error("Failed to remove {} snapshot from {}. Cause: {}",
                    repository,
                    DOCKER_HUB_BASE_URI,
                    e.getLocalizedMessage(),
                    e);
        }
    }

}
