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

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentValidator;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerServicesStartStrategy;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import java.util.List;
import java.util.Set;

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
    private final DockerServiceStarter serviceStarter;
    private final DockerNetworkLifecycle networkLifecycle;

    @Inject
    public DockerRuntimeInfrastructure(DockerEnvironmentParser dockerEnvironmentParser,
                                       DockerEnvironmentValidator dockerEnvironmentValidator,
                                       DockerServicesStartStrategy startStrategy,
                                       InfrastructureProvisioner infrastructureProvisioner,
                                       DockerEnvironmentNormalizer environmentNormalizer,
                                       DockerServiceStarter serviceStarter,
                                       DockerNetworkLifecycle networkLifecycle) {
        super("docker", SUPPORTED_RECIPE_TYPES);
        this.dockerEnvironmentValidator = dockerEnvironmentValidator;
        this.dockerEnvironmentParser = dockerEnvironmentParser;
        this.startStrategy = startStrategy;
        this.infrastructureProvisioner = infrastructureProvisioner;
        this.environmentNormalizer = environmentNormalizer;
        this.serviceStarter = serviceStarter;
        this.networkLifecycle = networkLifecycle;
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
    public RuntimeContext prepare(RuntimeIdentity identity, Environment environment) throws ValidationException,
                                                                                            InfrastructureException {
        DockerEnvironment dockerEnvironment = dockerEnvironmentParser.parse(environment);
        dockerEnvironmentValidator.validate(environment, dockerEnvironment);
        // check that order can be resolved
        List<String> orderedServices = startStrategy.order(dockerEnvironment);

        infrastructureProvisioner.provision(environment, dockerEnvironment);

        environmentNormalizer.normalize(environment, dockerEnvironment, identity);

        // environment holder
        return new DockerRuntimeContext(dockerEnvironment,
                                        environment,
                                        identity,
                                        this,
                                        null,
                                        orderedServices,
                                        networkLifecycle,
                                        serviceStarter);
    }


    @Override
    public Set<RuntimeIdentity> getIdentities() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InternalRuntime getRuntime(RuntimeIdentity id) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
