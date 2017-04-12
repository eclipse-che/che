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

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.exception.AgentException;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.agent.shared.model.AgentKey;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentValidator;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerServicesStartStrategy;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure} that
 * uses Docker containers as an {@code Environment} implementation.
 *
 * @author Alexander Garagatyi
 */
public class DockerRuntimeInfrastructure extends RuntimeInfrastructure {
    // TODO rework
    public static final Set<String> SUPPORTED_RECIPE_TYPES = ImmutableSet.of("dockerimage", "dockerfile", "compose");

    private final DockerEnvironmentValidator  dockerEnvironmentValidator;
    private final DockerEnvironmentParser     dockerEnvironmentParser;
    private final DockerServicesStartStrategy startStrategy;
    private final InfrastructureProvisioner   infrastructureProvisioner;
    private final DockerEnvironmentNormalizer environmentNormalizer;
    private final DockerServiceStarter        serviceStarter;
    private final DockerNetworkLifecycle      networkLifecycle;
    private final URLRewriter                 urlRewriter;
    private final DockerConnector             dockerConnector;
    private final AgentRegistry               agentRegistry;
    private final AgentSorter                 agentSorter;

    @Inject
    public DockerRuntimeInfrastructure(DockerEnvironmentParser dockerEnvironmentParser,
                                       DockerEnvironmentValidator dockerEnvironmentValidator,
                                       DockerServicesStartStrategy startStrategy,
                                       InfrastructureProvisioner infrastructureProvisioner,
                                       DockerEnvironmentNormalizer environmentNormalizer,
                                       DockerServiceStarter serviceStarter,
                                       DockerNetworkLifecycle networkLifecycle,
                                       URLRewriter urlRewriter,
                                       DockerConnector dockerConnector,
                                       AgentRegistry agentRegistry,
                                       AgentSorter agentSorter) {
        super("docker", SUPPORTED_RECIPE_TYPES);
        this.dockerEnvironmentValidator = dockerEnvironmentValidator;
        this.dockerEnvironmentParser = dockerEnvironmentParser;
        this.startStrategy = startStrategy;
        this.infrastructureProvisioner = infrastructureProvisioner;
        this.environmentNormalizer = environmentNormalizer;
        this.serviceStarter = serviceStarter;
        this.networkLifecycle = networkLifecycle;
        this.urlRewriter = urlRewriter;
        this.dockerConnector = dockerConnector;
        this.agentRegistry = agentRegistry;
        this.agentSorter = agentSorter;
    }

    @Override
    public Environment estimate(Environment environment) throws ValidationException,
                                                                InfrastructureException {
        DockerEnvironment dockerEnvironment = dockerEnvironmentParser.parse(environment);
        dockerEnvironmentValidator.validate(environment, dockerEnvironment);
        // check that order can be resolved
        startStrategy.order(dockerEnvironment);
        // TODO add an actual estimation of what is missing in the environment

        return environment;
    }

    @Override
    public RuntimeContext prepare(RuntimeIdentity identity, Environment originEnv) throws ValidationException,
                                                                                            InfrastructureException {
        // Copy to be able to change env and protect from env changes on the go
        EnvironmentImpl environment = new EnvironmentImpl(originEnv);
        DockerEnvironment dockerEnvironment = dockerEnvironmentParser.parse(environment);
        dockerEnvironmentValidator.validate(environment, dockerEnvironment);
        // check that services start order can be resolved
        List<String> orderedServices = startStrategy.order(dockerEnvironment);
        for (MachineConfigImpl machineConfig : environment.getMachines().values()) {
            try {
                List<AgentKey> agentKeys = agentSorter.sort(machineConfig.getAgents());
                machineConfig.setAgents(agentKeys.stream().map(AgentKey::getId).collect(Collectors.toList()));
            } catch (AgentException e) {
                throw new InfrastructureException(e.getLocalizedMessage(), e);
            }
        }

        // modify environment with everything needed to use docker machines on particular (cloud) infrastructure
        infrastructureProvisioner.provision(environment, dockerEnvironment);
        //
        environmentNormalizer.normalize(environment, dockerEnvironment, identity);

        return new DockerRuntimeContext(dockerEnvironment,
                                        environment,
                                        identity,
                                        this,
                                        null,
                                        orderedServices,
                                        networkLifecycle,
                                        serviceStarter,
                                        urlRewriter,
                                        dockerConnector,
                                        agentRegistry);
    }

    @Override
    public Set<RuntimeIdentity> getIdentities() throws UnsupportedOperationException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public InternalRuntime getRuntime(RuntimeIdentity id) throws UnsupportedOperationException {
        // TODO
        throw new UnsupportedOperationException();
    }
}
