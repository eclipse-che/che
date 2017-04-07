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
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentValidator;

import java.io.IOException;
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

    private final DockerEnvironmentValidator dockerEnvironmentValidator;

    @Inject
    public DockerRuntimeInfrastructure(DockerEnvironmentValidator dockerEnvironmentValidator) {
        super("docker", SUPPORTED_RECIPE_TYPES);
        this.dockerEnvironmentValidator = dockerEnvironmentValidator;
    }

    public Environment estimate(Environment environment) throws ValidationException,
                                                                ServerException {
        // TODO add an actual estimation of what is missing in the environment
        environment = dockerEnvironmentValidator.validate(environment);
        return environment;
    }

    public RuntimeContext prepare(RuntimeIdentity id, Environment environment) throws ValidationException,
                                                                                      ApiException,
                                                                                      IOException {
        throw new ApiException("");
    }

    public Set<RuntimeIdentity> getIdentities() throws NotSupportedException {
        throw new NotSupportedException();
    }

    public InternalRuntime getRuntime(RuntimeIdentity id) throws NotSupportedException {
        throw new NotSupportedException();
    }
}
