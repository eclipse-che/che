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

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.MessageProcessor;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.params.CommitParams;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.PushParams;
import org.eclipse.che.plugin.docker.client.params.PutResourceParams;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.client.params.RemoveImageParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.workspace.infrastructure.docker.monit.DockerMachineStopDetector;
import org.eclipse.che.workspace.infrastructure.docker.snapshot.SnapshotException;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.docker.registry.DockerRegistryClient.MACHINE_SNAPSHOT_PREFIX;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Alexander Garagatyi
 */
public class DockerMachine implements Machine {

    private static final Logger LOG = getLogger(DockerMachine.class);

    /**
     * Name of the latest tag used in Docker image.
     */
    public static final String LATEST_TAG             = "latest";
    /**
     * Env variable that points to root folder of projects in dev machine
     */
    public static final String PROJECTS_ROOT_VARIABLE = "CHE_PROJECTS_ROOT";

    /**
     * Env variable for jvm settings
     */
    public static final String JAVA_OPTS_VARIABLE = "JAVA_OPTS";

    /**
     * Env variable for dev machine that contains url of Che API
     */
    public static final String API_ENDPOINT_URL_VARIABLE = "CHE_API";

    /**
     * Environment variable that will be setup in developer machine will contain ID of a workspace for which this machine has been created
     */
    public static final String CHE_WORKSPACE_ID = "CHE_WORKSPACE_ID";

    /**
     * Default HOSTNAME that will be added in all docker containers that are started. This host will container the Docker host's ip
     * reachable inside the container.
     */
    public static final String CHE_HOST = "che-host";

    /**
     * Environment variable that will be setup in developer machine and contains user token.
     */
    public static final String USER_TOKEN = "USER_TOKEN";

    private final String                    container;
    private final DockerConnector           docker;
    private final String                    image;
    private final DockerMachineStopDetector dockerMachineStopDetector;
    private final String                    registry;
    private final String                    registryNamespace;
    private final boolean                   snapshotUseRegistry;
    private final Map<String, ServerImpl>   servers;

    public DockerMachine(String containerId,
                         String image,
                         DockerConnector docker,
                         Map<String, ServerImpl> servers,
                         String registry,
                         boolean snapshotUseRegistry,
                         String registryNamespace,
                         DockerMachineStopDetector dockerMachineStopDetector) {
        this.container = containerId;
        this.docker = docker;
        this.image = image;
        this.registry = registry;
        this.registryNamespace = registryNamespace;
        this.snapshotUseRegistry = snapshotUseRegistry;
        this.dockerMachineStopDetector = dockerMachineStopDetector;
        this.servers = servers;
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, ServerImpl> getServers() {
        return servers;
    }

    void setServerStatus(String serverRef, ServerStatus status) {
        if (servers == null) {
            throw new IllegalStateException("Servers are not initialized yet");
        }
        ServerImpl server = servers.get(serverRef);
        if (server == null) {
            throw new IllegalArgumentException("Server with provided reference " + serverRef + " missing");
        }
        server.setStatus(status);
    }

    public void putResource(String targetPath, InputStream sourceStream) throws InfrastructureException {
        try {
            docker.putResource(PutResourceParams.create(container, targetPath, sourceStream));
        } catch (IOException e) {
            throw new InternalInfrastructureException(e.getMessage(), e);
        }
    }

    public void exec(String script, MessageProcessor<LogMessage> messageProcessor) throws InfrastructureException {
        try {
            Exec exec = docker.createExec(CreateExecParams.create(container,
                                                                  new String[] {"/bin/sh", "-c", script})
                                                          .withDetach(false));
            docker.startExec(StartExecParams.create(exec.getId()), messageProcessor);
        } catch (IOException e) {
            throw new InfrastructureException(e.getLocalizedMessage(), e);
        }
    }

    public void destroy() throws InfrastructureException {
        dockerMachineStopDetector.stopDetection(container);
        try {
            docker.removeContainer(RemoveContainerParams.create(container)
                                                        .withRemoveVolumes(true)
                                                        .withForce(true));
        } catch (IOException e) {
            throw new InternalInfrastructureException(e.getMessage(), e);
        }
        try {
            docker.removeImage(RemoveImageParams.create(image).withForce(false));
        } catch (IOException e) {
            // TODO make log level warning if we ignoring it or remove ignoring phrase
            LOG.error("IOException during destroy(). Ignoring.", e);
        }
    }

    /**
     * Can be used for docker specific operations with machine
     */
    public String getContainer() {
        return container;
    }

    @Override
    public String toString() {
        return "DockerMachine{" +
               "container='" + container + '\'' +
               ", docker=" + docker +
               ", image='" + image + '\'' +
               ", registry='" + registry + '\'' +
               ", registryNamespace='" + registryNamespace + '\'' +
               ", snapshotUseRegistry='" + snapshotUseRegistry +
               ", container=" + container +
               '}';
    }


    public DockerMachineSource saveToSnapshot(ProgressMonitor progressMonitor) throws SnapshotException {
        try {
            String image = generateRepository();
            if (!snapshotUseRegistry) {
                commitContainer(image, LATEST_TAG);
                return new DockerMachineSource(image).withTag(LATEST_TAG);
            }

            PushParams pushParams = PushParams.create(image)
                                              .withRegistry(registry)
                                              .withTag(LATEST_TAG);

            final String fullRepo = pushParams.getFullRepo();
            commitContainer(fullRepo, LATEST_TAG);
            //TODO fix this workaround. Docker image is not visible after commit when using swarm
            Thread.sleep(2000);
            final String digest = docker.push(pushParams, progressMonitor);
            docker.removeImage(RemoveImageParams.create(fullRepo).withForce(false));
            return new DockerMachineSource(image).withRegistry(registry).withDigest(digest).withTag(LATEST_TAG);
        } catch (IOException ioEx) {
            throw new SnapshotException(ioEx);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SnapshotException(e.getLocalizedMessage(), e);
        }
    }

    protected void commitContainer(String repository, String tag) throws IOException {
        String comment = format("Suspended at %1$ta %1$tb %1$td %1$tT %1$tZ %1$tY",
                                System.currentTimeMillis());
        // !! We SHOULD NOT pause container before commit because all execs will fail
        // to push image to private registry it should be tagged with registry in repo name
        // https://docs.docker.com/reference/api/docker_remote_api_v1.16/#push-an-image-on-the-registry
        docker.commit(CommitParams.create(container)
                                  .withRepository(repository)
                                  .withTag(tag)
                                  .withComment(comment));
    }

    private String generateRepository() {
        if (registryNamespace != null) {
            return registryNamespace + '/' + MACHINE_SNAPSHOT_PREFIX + NameGenerator.generate(null, 16);
        }
        return MACHINE_SNAPSHOT_PREFIX + NameGenerator.generate(null, 16);
    }
}
