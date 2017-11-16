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

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.inject.Named;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.infrastructure.docker.client.json.ContainerListEntry;
import org.eclipse.che.workspace.infrastructure.docker.container.DockerContainers;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.server.mapping.ExternalIpURLRewriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Docker specific implementation of {@link RuntimeContext}.
 *
 * @author Alexander Garagatyi
 * @author Yevhenii Voievodin
 */
public class DockerRuntimeContext extends RuntimeContext {

  private static final Logger LOG = LoggerFactory.getLogger(DockerRuntimeContext.class);

  private final DockerEnvironment dockerEnvironment;
  private final List<String> orderedContainers;
  private final ExternalIpURLRewriter urlRewriter;
  private final String websocketOutputEndpoint;
  private final DockerRuntimeFactory runtimeFactory;
  private final DockerContainers containers;
  private final RuntimeConsistencyChecker consistencyChecker;
  private final DockerSharedPool sharedPool;

  @AssistedInject
  public DockerRuntimeContext(
      @Assisted DockerRuntimeInfrastructure infrastructure,
      @Assisted RuntimeIdentity identity,
      @Assisted InternalEnvironment environment,
      @Assisted DockerEnvironment dockerEnv,
      @Assisted List<String> containersOrder,
      DockerRuntimeFactory runtimeFactory,
      DockerContainers containers,
      DockerSharedPool sharedPool,
      RuntimeConsistencyChecker consistencyChecker,
      ExternalIpURLRewriter urlRewriter,
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint)
      throws InfrastructureException, ValidationException {

    super(environment, identity, infrastructure);
    this.dockerEnvironment = dockerEnv;
    this.orderedContainers = ImmutableList.copyOf(containersOrder);
    this.urlRewriter = urlRewriter;
    this.websocketOutputEndpoint = cheWebsocketEndpoint;
    this.runtimeFactory = runtimeFactory;
    this.containers = containers;
    this.sharedPool = sharedPool;
    this.consistencyChecker = consistencyChecker;
  }

  /** Returns docker environment which based on normalized context environment configuration. */
  public DockerEnvironment getDockerEnvironment() {
    return dockerEnvironment;
  }

  /** Returns the list of the ordered containers, machines must be started in the same order. */
  public List<String> getOrderedContainers() {
    return orderedContainers;
  }

  @Override
  public URI getOutputChannel() throws InfrastructureException {
    try {
      return URI.create(urlRewriter.rewriteURL(getIdentity(), null, websocketOutputEndpoint));
    } catch (IllegalArgumentException ex) {
      throw new InternalInfrastructureException(
          "Failed to get the output channel because: " + ex.getLocalizedMessage());
    }
  }

  @Override
  public DockerInternalRuntime getRuntime() throws InfrastructureException {
    RuntimeIdentity identity = getIdentity();
    List<ContainerListEntry> runningContainers = containers.find(identity);
    if (runningContainers.isEmpty()) {
      return runtimeFactory.create(this);
    }

    DockerInternalRuntime runtime = runtimeFactory.create(this, runningContainers);
    try {
      consistencyChecker.check(getEnvironment(), runtime);
      runtime.checkServers();
    } catch (InfrastructureException | ValidationException x) {
      LOG.warn(
          "Runtime '{}:{}' will be stopped as it is not consistent with its configuration. "
              + "The problem: {}",
          identity.getWorkspaceId(),
          identity.getEnvName(),
          x.getMessage());
      stopAsync(runtime);
      throw new InfrastructureException(x.getMessage(), x);
    }
    return runtime;
  }

  private void stopAsync(DockerInternalRuntime runtime) {
    sharedPool.execute(
        () -> {
          try {
            runtime.stop(Collections.emptyMap());
          } catch (Exception x) {
            LOG.error("Couldn't stop workspace runtime due to error: {}", x.getMessage());
          }
        });
  }
}
