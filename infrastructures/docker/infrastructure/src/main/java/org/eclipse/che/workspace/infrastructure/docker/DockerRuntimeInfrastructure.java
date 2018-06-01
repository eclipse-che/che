/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker;

import static java.lang.String.format;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.provision.InternalEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.container.DockerContainers;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentNormalizer;
import org.eclipse.che.workspace.infrastructure.docker.environment.convert.DockerEnvironmentConverter;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Implementation of {@link RuntimeInfrastructure} that uses Docker containers as an {@code
 * Environment} implementation.
 *
 * @author Alexander Garagatyi
 */
public class DockerRuntimeInfrastructure extends RuntimeInfrastructure {

  public static String NAME = "docker";

  private final Map<String, DockerEnvironmentConverter> envConverters;
  private final DockerEnvironmentProvisioner dockerEnvProvisioner;
  private final DockerEnvironmentNormalizer dockerEnvNormalizer;
  private final DockerRuntimeContextFactory contextFactory;
  private final DockerContainers containers;

  @Inject
  public DockerRuntimeInfrastructure(
      EventService eventService,
      Map<String, DockerEnvironmentConverter> envConverters,
      DockerEnvironmentProvisioner dockerEnvProvisioner,
      DockerEnvironmentNormalizer dockerEnvNormalizer,
      DockerRuntimeContextFactory contextFactory,
      DockerContainers containers,
      Set<InternalEnvironmentProvisioner> internalEnvironmentProvisioners) {
    super(NAME, envConverters.keySet(), eventService, internalEnvironmentProvisioners);
    this.envConverters = envConverters;
    this.dockerEnvProvisioner = dockerEnvProvisioner;
    this.dockerEnvNormalizer = dockerEnvNormalizer;
    this.contextFactory = contextFactory;
    this.containers = containers;
  }

  @Override
  protected RuntimeContext internalPrepare(
      RuntimeIdentity identity, InternalEnvironment environment)
      throws ValidationException, InfrastructureException {
    DockerEnvironment dockerEnvironment = convertToDockerEnv(environment);

    // modify environment with everything needed to use docker machines on particular (cloud)
    // infrastructure
    dockerEnvProvisioner.provision(dockerEnvironment, identity);

    // normalize env to provide environment description with absolutely everything expected in
    dockerEnvNormalizer.normalize(dockerEnvironment, identity);

    return contextFactory.create(this, identity, dockerEnvironment);
  }

  @Override
  public Set<RuntimeIdentity> getIdentities() throws InfrastructureException {
    // Due to https://github.com/eclipse/che/issues/5814, recovering is not fully possible
    // so infrastructure should not claim support of it.
    // return containers.findIdentities();
    throw new UnsupportedOperationException("Runtimes tracking currently does not supported.");
  }

  private DockerEnvironment convertToDockerEnv(InternalEnvironment sourceEnv)
      throws ValidationException {
    String recipeType = sourceEnv.getRecipe().getType();
    DockerEnvironmentConverter converter = envConverters.get(recipeType);
    if (converter == null) {
      throw new ValidationException(
          format(
              "Environment type '%s' is not supported. Supported environment types: %s",
              recipeType, Joiner.on(", ").join(envConverters.keySet())));
    }
    return converter.convert(sourceEnv);
  }
}
