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

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.shared.Utils;
import org.eclipse.che.plugin.docker.client.json.ContainerListEntry;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.api.workspace.server.OutputEndpoint.OUTPUT_WEBSOCKET_ENDPOINT_BASE;

/**
 * Docker specific implementation of {@link RuntimeContext}.
 *
 * @author Alexander Garagatyi
 * @author Yevhenii Voievodin
 */
public class DockerRuntimeContext extends RuntimeContext {

    private static final Logger LOG = LoggerFactory.getLogger(DockerRuntimeContext.class);

    private final DockerEnvironment         dockerEnvironment;
    private final List<String>              orderedContainers;
    private final String                    devMachineName;
    private final String                    websocketEndpointBase;
    private final DockerRuntimeFactory      runtimeFactory;
    private final DockerContainers          containers;
    private final RuntimeConsistencyChecker consistencyChecker;
    private final DockerSharedPool          sharedPool;

    @AssistedInject
    public DockerRuntimeContext(@Assisted DockerRuntimeInfrastructure infrastructure,
                                @Assisted RuntimeIdentity identity,
                                @Assisted Environment environment,
                                @Assisted DockerEnvironment dockerEnv,
                                @Assisted List<String> containersOrder,
                                InstallerRegistry installerRegistry,
                                DockerRuntimeFactory runtimeFactory,
                                DockerContainers containers,
                                DockerSharedPool sharedPool,
                                RuntimeConsistencyChecker consistencyChecker,
                                @Named("che.websocket.endpoint.base") String websocketEndpointBase)
            throws InfrastructureException, ValidationException {

        super(environment, identity, infrastructure, installerRegistry);
        this.devMachineName = Utils.getDevMachineName(environment);
        this.dockerEnvironment = dockerEnv;
        this.orderedContainers = ImmutableList.copyOf(containersOrder);
        this.websocketEndpointBase = websocketEndpointBase;
        this.runtimeFactory = runtimeFactory;
        this.containers = containers;
        this.sharedPool = sharedPool;
        this.consistencyChecker = consistencyChecker;
    }

    /** Returns docker environment which based on normalized context environment configuration. */
    public DockerEnvironment getDockerEnvironment() { return dockerEnvironment;}

    /** Returns the name of the dev machine. */
    public String getDevMachineName() { return devMachineName; }

    /** Returns the list of the ordered containers, machines must be started in the same order. */
    public List<String> getOrderedContainers() { return orderedContainers; }

    @Override
    public URI getOutputChannel() throws InfrastructureException {
        try {
            return UriBuilder.fromUri(websocketEndpointBase)
                             .path(OUTPUT_WEBSOCKET_ENDPOINT_BASE)
                             .build();
        } catch (UriBuilderException | IllegalArgumentException ex) {
            throw new InternalInfrastructureException("Failed to get the output channel because: " +
                                                      ex.getLocalizedMessage());
        }
    }

    @Override
    public DockerInternalRuntime getRuntime() throws InfrastructureException {
        List<ContainerListEntry> runningContainers = containers.find(identity);
        if (runningContainers.isEmpty()) {
            return runtimeFactory.create(this);
        }

        DockerInternalRuntime runtime = runtimeFactory.create(this, runningContainers);
        try {
            consistencyChecker.check(environment, runtime);
            runtime.checkServers();
        } catch (InfrastructureException | ValidationException x) {
            LOG.warn("Runtime '{}:{}' will be stopped as it is not consistent with its configuration. " +
                     "The problem: {}",
                     identity.getWorkspaceId(),
                     identity.getEnvName(),
                     x.getMessage());
            stopAsync(runtime);
            throw new InfrastructureException(x.getMessage(), x);
        }
        return runtime;
    }

    private void stopAsync(DockerInternalRuntime runtime) {
        sharedPool.execute(() -> {
            try {
                runtime.stop(Collections.emptyMap());
            } catch (Exception x) {
                LOG.error("Couldn't stop workspace runtime due to error: {}", x.getMessage());
            }
        });
    }
}
