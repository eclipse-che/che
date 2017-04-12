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

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.workspace.infrastructure.docker.old.strategy.ServerEvaluationStrategy;
import org.eclipse.che.workspace.infrastructure.docker.old.strategy.ServerEvaluationStrategyProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Alexander Garagatyi
 */
public class DockerMachine implements Machine {
    /**
     * Name of the latest tag used in Docker image.
     */
    public static final String LATEST_TAG = "latest";
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



    private final String                           container;
    private final DockerConnector                  docker;
    private final String                           image;
//    private final String                           registry;
//    private final String                           registryNamespace;
//    private final DockerNode                       node;
//    private final DockerInstanceStopDetector       dockerInstanceStopDetector;
//    private final boolean                          snapshotUseRegistry;
    private final ContainerInfo                    info;
    private final ServerEvaluationStrategyProvider provider;
//    private final Map<String, OldServerConfImpl>   serversConf;
//    private final String                           internalHost;
//    private final ServerEvaluationStrategyProvider provider;

    public DockerMachine(DockerConnector docker,
//                         @Named("che.docker.registry") String registry,
//                         @Named("che.docker.namespace") @Nullable String registryNamespace,
//                         @Assisted Machine machine,
                         @Assisted("container") String container,
                         @Assisted("image") String image,
//                         @Assisted DockerNode node,
//                         @Assisted LineConsumer outputConsumer,
//                         DockerInstanceStopDetector dockerInstanceStopDetector,
//                         DockerInstanceProcessesCleaner processesCleaner,
//                         @Named("che.docker.registry_for_snapshots") boolean snapshotUseRegistry,
//                         @Assisted ContainerInfo containerInfo,
//                         @Assisted OldMachineConfig machineConfig,
//                         @Assisted String internalHost,
                         ServerEvaluationStrategyProvider provider
//                         @Named("machine.docker.dev_machine.machine_servers") Set<OldServerConf> devMachineSystemServers,
//                         @Named("machine.docker.machine_servers") Set<OldServerConf> allMachinesSystemServers
    ) throws InfrastructureException {
//        this.dockerMachineFactory = dockerMachineFactory;
        this.container = container;
        this.docker = docker;
        this.image = image;
//        this.outputConsumer = outputConsumer;
//        this.registry = registry;
//        this.registryNamespace = registryNamespace;
//        this.node = node;
//        this.dockerInstanceStopDetector = dockerInstanceStopDetector;
//        processesCleaner.trackProcesses(this);
//        this.snapshotUseRegistry = snapshotUseRegistry;
//        this.machineRuntime = doGetRuntime();
//        this.workspace = workspace;
//        this.envName = envName;
//        this.owner = owner;
//        this.id = id;
        try {
            this.info = docker.inspectContainer(container);
        } catch (IOException e) {
            throw new InfrastructureException(e.getLocalizedMessage(), e);
        }
//        Stream<OldServerConf> confStream = Stream.concat(machineConfig.getServers().stream(), allMachinesSystemServers.stream());
//        if (machineConfig.isDev()) {
//            confStream = Stream.concat(confStream, devMachineSystemServers.stream());
//        }
        // convert list to map for quick search and normalize port - add /tcp if missing
//        this.serversConf = confStream.collect(toMap(srvConf -> srvConf.getPort().contains("/") ?
//                                                               srvConf.getPort() :
//                                                               srvConf.getPort() + "/tcp",
//                                                    OldServerConfImpl::new));
//
//        this.internalHost = internalHost;
        this.provider = provider;
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, ? extends Server> getServers() {
        ServerEvaluationStrategy strategy = provider.get();
        return strategy.getServers(info, "localhost", Collections.emptyMap());
    }

    public void destroy() {

    }
/*
    public MachineSource saveToSnapshot() throws MachineException {
        try {
            String image = generateRepository();
            if(!snapshotUseRegistry) {
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
            final ProgressLineFormatterImpl lineFormatter = new ProgressLineFormatterImpl();
            final String digest = docker.push(pushParams,
                                              progressMonitor -> {
                                                  try {
                                                      outputConsumer.writeLine(lineFormatter.format(progressMonitor));
                                                  } catch (IOException ignored) {
                                                  }
                                              });
            docker.removeImage(RemoveImageParams.create(fullRepo).withForce(false));
            return new DockerMachineSource(image).withRegistry(registry).withDigest(digest).withTag(LATEST_TAG);
        } catch (IOException ioEx) {
            throw new MachineException(ioEx);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MachineException(e.getLocalizedMessage(), e);
        }
    }

    public InstanceProcess createProcess(Command command, String outputChannel) throws MachineException {
        final Integer pid = pidSequence.getAndIncrement();
        final InstanceProcess process = dockerMachineFactory.createProcess(command,
                                                                           container,
                                                                           outputChannel,
                                                                           String.format(PID_FILE_TEMPLATE, pid),
                                                                           pid);
        machineProcesses.put(pid, process);
        return process;
    }
*/
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
               ", info=" + info +
               ", provider=" + provider +
               '}';
    }
}
