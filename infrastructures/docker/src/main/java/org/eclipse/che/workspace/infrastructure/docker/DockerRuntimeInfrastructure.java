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

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.NotSupportedException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.ValidationException;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentValidator;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerServicesStartStrategy;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import java.io.IOException;
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

    @Inject
    public DockerRuntimeInfrastructure(DockerEnvironmentParser dockerEnvironmentParser,
                                       DockerEnvironmentValidator dockerEnvironmentValidator,
                                       DockerServicesStartStrategy startStrategy,
                                       InfrastructureProvisioner infrastructureProvisioner,
                                       DockerEnvironmentNormalizer environmentNormalizer) {
        super("docker", SUPPORTED_RECIPE_TYPES);
        this.dockerEnvironmentValidator = dockerEnvironmentValidator;
        this.dockerEnvironmentParser = dockerEnvironmentParser;
        this.startStrategy = startStrategy;
        this.infrastructureProvisioner = infrastructureProvisioner;
        this.environmentNormalizer = environmentNormalizer;
    }

    @Override
    public Environment estimate(Environment environment) throws ValidationException,
                                                                ServerException {
        DockerEnvironment dockerEnvironment = dockerEnvironmentParser.parse(environment);
        dockerEnvironmentValidator.validate(environment, dockerEnvironment);
        // check that order can be resolved
        startStrategy.order(dockerEnvironment);
        // TODO add an actual estimation of what is missing in the environment

        return environment;
    }

    @Override
    public RuntimeContext prepare(RuntimeIdentity identity, Environment environment) throws ValidationException,
                                                                                            ApiException,
                                                                                            IOException {
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
                                        orderedServices);
    }


    @Override
    public Set<RuntimeIdentity> getIdentities() throws NotSupportedException {
        throw new NotSupportedException();
    }

    @Override
    public InternalRuntime getRuntime(RuntimeIdentity id) throws NotSupportedException {
        throw new NotSupportedException();
    }
}
