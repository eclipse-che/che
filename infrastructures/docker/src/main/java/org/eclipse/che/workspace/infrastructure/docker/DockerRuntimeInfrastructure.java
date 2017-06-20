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

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerConfigSourceSpecificEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.environment.EnvironmentNormalizer;
import org.eclipse.che.workspace.infrastructure.docker.environment.EnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.environment.EnvironmentValidator;
import org.eclipse.che.workspace.infrastructure.docker.environment.ServicesStartStrategy;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link RuntimeInfrastructure} that
 * uses Docker containers as an {@code Environment} implementation.
 *
 * @author Alexander Garagatyi
 */
public class DockerRuntimeInfrastructure extends RuntimeInfrastructure {
    private final EnvironmentValidator      dockerEnvironmentValidator;
    private final EnvironmentParser         dockerEnvironmentParser;
    private final ServicesStartStrategy     startStrategy;
    private final InfrastructureProvisioner infrastructureProvisioner;
    private final EnvironmentNormalizer     environmentNormalizer;
    private final DockerRuntimeFactory      runtimeFactory;
    private final AgentSorter               agentSorter;
    private final AgentRegistry             agentRegistry;

    @Inject
    public DockerRuntimeInfrastructure(EnvironmentParser dockerEnvironmentParser,
                                       EnvironmentValidator dockerEnvironmentValidator,
                                       ServicesStartStrategy startStrategy,
                                       InfrastructureProvisioner infrastructureProvisioner,
                                       EnvironmentNormalizer environmentNormalizer,
                                       Map<String, DockerConfigSourceSpecificEnvironmentParser> environmentParsers,
                                       DockerRuntimeFactory runtimeFactory,
                                       AgentSorter agentSorter,
                                       AgentRegistry agentRegistry,
                                       EventService eventService) {
        super("docker", environmentParsers.keySet(), eventService);
        this.dockerEnvironmentValidator = dockerEnvironmentValidator;
        this.dockerEnvironmentParser = dockerEnvironmentParser;
        this.startStrategy = startStrategy;
        this.infrastructureProvisioner = infrastructureProvisioner;
        this.environmentNormalizer = environmentNormalizer;
        this.runtimeFactory = runtimeFactory;
        this.agentSorter = agentSorter;
        this.agentRegistry = agentRegistry;
    }

    @Override
    public Environment estimate(Environment environment) throws ValidationException,
                                                                InfrastructureException {
        // TODO spi: get recipe from non-impl specific code
        DockerEnvironment dockerEnvironment = dockerEnvironmentParser.parse(environment);
        dockerEnvironmentValidator.validate(environment, dockerEnvironment);
        // check that order can be resolved
        startStrategy.order(dockerEnvironment);
        // TODO add an actual estimation of what is missing in the environment
        // memory
        // machines

        return environment;
    }

    @Override
    public DockerRuntimeContext prepare(RuntimeIdentity identity, Environment originEnv)
            throws ValidationException, InfrastructureException {
        // TODO spi: get recipe from non-impl specific code
        // Copy to be able to change env and protect from env changes by method caller
        EnvironmentImpl environment = new EnvironmentImpl(originEnv);
        DockerEnvironment dockerEnvironment = dockerEnvironmentParser.parse(environment);
        dockerEnvironmentValidator.validate(environment, dockerEnvironment);
        // check that services start order can be resolved
        List<String> orderedServices = startStrategy.order(dockerEnvironment);

        // modify environment with everything needed to use docker machines on particular (cloud) infrastructure
        infrastructureProvisioner.provision(environment, dockerEnvironment, identity);
        // normalize env to provide environment description with absolutely everything expected in
        environmentNormalizer.normalize(environment, dockerEnvironment, identity);

        return new DockerRuntimeContext(this,
                                        identity,
                                        environment,
                                        dockerEnvironment,
                                        orderedServices,
                                        agentSorter,
                                        agentRegistry,
                                        runtimeFactory);
    }
}
