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
package org.eclipse.che.plugin.docker.machine;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.exception.InvalidRecipeException;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SnapshotException;
import org.eclipse.che.api.machine.server.exception.UnsupportedRecipeException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.params.RemoveImageParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

/**
 * Docker implementation of {@link InstanceProvider}
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 * @author Roman Iuvshyn
 * @author Mykola Morhun
 *
 * @deprecated use {@link MachineProviderImpl} instead
 */
@Deprecated
@Singleton
public class DockerInstanceProvider implements InstanceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DockerInstanceProvider.class);

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

    private final DockerConnector                               docker;
    private final boolean                                       snapshotUseRegistry;

    @Inject
    public DockerInstanceProvider(DockerConnector docker,
                                  @Named("che.docker.registry_for_snapshots") boolean snapshotUseRegistry) throws IOException {
        this.docker = docker;
        this.snapshotUseRegistry = snapshotUseRegistry;
    }

    @Override
    public String getType() {
        return "docker";
    }

    @Override
    public Set<String> getRecipeTypes() {
        return Collections.emptySet();
    }

    /**
     * Creates instance from scratch or by reusing a previously one by using specified {@link MachineSource}
     * data in {@link MachineConfig}.
     *
     * @param machine
     *         machine description
     * @param creationLogsOutput
     *         output for instance creation logs
     * @return newly created {@link Instance}
     * @throws UnsupportedRecipeException
     *         if specified {@code recipe} is not supported
     * @throws InvalidRecipeException
     *         if {@code recipe} is invalid
     * @throws NotFoundException
     *         if instance described by {@link MachineSource} doesn't exists
     * @throws MachineException
     *         if other error occurs
     */
    @Override
    public Instance createInstance(Machine machine,
                                   LineConsumer creationLogsOutput) throws NotFoundException,
                                                                           MachineException {
        throw new UnsupportedOperationException("This machine provider is deprecated.");
    }

    /**
     * Removes snapshot of the instance in implementation specific way.
     *
     * @param machineSource
     *         contains implementation specific key of the snapshot of the instance that should be removed
     * @throws SnapshotException
     *         if exception occurs on instance snapshot removal
     */
    @Override
    public void removeInstanceSnapshot(final MachineSource machineSource) throws SnapshotException {
        // use registry API directly because docker doesn't have such API yet
        // https://github.com/docker/docker-registry/issues/45
        final DockerMachineSource dockerMachineSource;
        try {
            dockerMachineSource = new DockerMachineSource(machineSource);
        } catch (MachineException e) {
            throw new SnapshotException(e);
        }

        if (!snapshotUseRegistry) {
            try {
                docker.removeImage(RemoveImageParams.create(dockerMachineSource.getLocation(false)));
            } catch (IOException ignore) {
            }
            return;
        }

        final String registry = dockerMachineSource.getRegistry();
        final String repository = dockerMachineSource.getRepository();
        if (registry == null || repository == null) {
            LOG.error("Failed to remove instance snapshot: invalid machine source: {}", dockerMachineSource);
            throw new SnapshotException("Snapshot removing failed. Snapshot attributes are not valid");
        }

        try {
            URL url = UriBuilder.fromUri("http://" + registry) // TODO make possible to use https here
                                .path("/v2/{repository}/manifests/{digest}")
                                .build(repository, dockerMachineSource.getDigest())
                                .toURL();
            final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            try {
                conn.setConnectTimeout(30 * 1000);
                conn.setRequestMethod("DELETE");
                // TODO add auth header for secured registry
                // conn.setRequestProperty("Authorization", authHeader);
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
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

}
