/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.docker.container.ContainersStartStrategy;
import org.eclipse.che.workspace.infrastructure.docker.container.DockerContainers;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerConfigSourceSpecificEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.environment.EnvironmentNormalizer;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.ComposeInternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile.DockerfileInternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerimageInternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Implementation of {@link RuntimeInfrastructure} that uses Docker containers as an {@code
 * Environment} implementation.
 *
 * @author Alexander Garagatyi
 */
public class DockerRuntimeInfrastructure extends RuntimeInfrastructure {

  private final ContainersStartStrategy startStrategy;
  private final InfrastructureProvisioner infrastructureProvisioner;
  private final EnvironmentNormalizer environmentNormalizer;
  private final DockerRuntimeContextFactory contextFactory;
  private final DockerContainers containers;

  @Inject
  public DockerRuntimeInfrastructure(
      ContainersStartStrategy startStrategy,
      InfrastructureProvisioner infrastructureProvisioner,
      EnvironmentNormalizer environmentNormalizer,
      Map<String, DockerConfigSourceSpecificEnvironmentParser> environmentParsers,
      DockerRuntimeContextFactory contextFactory,
      DockerContainers containers,
      EventService eventService) {
    super("docker", environmentParsers.keySet(), eventService);
    this.startStrategy = startStrategy;
    this.infrastructureProvisioner = infrastructureProvisioner;
    this.environmentNormalizer = environmentNormalizer;
    this.contextFactory = contextFactory;
    this.containers = containers;
  }

  @Override
  public DockerRuntimeContext prepare(RuntimeIdentity identity, InternalEnvironment environment)
      throws ValidationException, InfrastructureException {

    DockerEnvironment dockerEnvironment;
    String type = environment.getRecipe().getType();
    switch (type) {
      case "dockerfile":
        dockerEnvironment = ((DockerfileInternalEnvironment) environment).getDockerEnvironment();
        break;
      case "dockerimage":
        dockerEnvironment = ((DockerimageInternalEnvironment) environment).getDockerEnvironment();
        break;
      case "compose":
        dockerEnvironment = ((ComposeInternalEnvironment) environment).getComposeEnvironment();
        break;
      default:
        throw new InfrastructureException("Recipe type is not allowed " + type);
    }

    // modify environment with everything needed to use docker machines on particular (cloud)
    // infrastructure
    infrastructureProvisioner.provision(environment, dockerEnvironment, identity);
    // check that containers start order can be resolved
    // NOTE: it should be performed before environmentNormalizer.normalize because normalization
    // changes links, volumes from which will fail order evaluation
    // It can be changed after reimplementing strategy to respect normalization
    List<String> containersOrder = startStrategy.order(dockerEnvironment);
    // normalize env to provide environment description with absolutely everything expected in
    environmentNormalizer.normalize(environment, dockerEnvironment, identity);

    return contextFactory.create(this, identity, environment, dockerEnvironment, containersOrder);
  }

  @Override
  public Set<RuntimeIdentity> getIdentities() throws InfrastructureException {
    return containers.findIdentities();
  }
}
