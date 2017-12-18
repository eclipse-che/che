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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.Exec;
import org.eclipse.che.infrastructure.docker.client.LogMessage;
import org.eclipse.che.infrastructure.docker.client.MessageProcessor;
import org.eclipse.che.infrastructure.docker.client.params.CreateExecParams;
import org.eclipse.che.infrastructure.docker.client.params.PutResourceParams;
import org.eclipse.che.infrastructure.docker.client.params.RemoveContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.RemoveImageParams;
import org.eclipse.che.infrastructure.docker.client.params.StartExecParams;
import org.eclipse.che.workspace.infrastructure.docker.monit.DockerMachineStopDetector;
import org.slf4j.Logger;

/** @author Alexander Garagatyi */
public class DockerMachine implements Machine {

  private static final Logger LOG = getLogger(DockerMachine.class);

  /** Name of the latest tag used in Docker image. */
  public static final String LATEST_TAG = "latest";

  /**
   * Default HOSTNAME that will be added in all docker containers that are started. This host will
   * container the Docker host's ip reachable inside the container.
   */
  public static final String CHE_HOST = "che-host";

  private final String container;
  private final DockerConnector docker;
  private final String image;
  private final DockerMachineStopDetector dockerMachineStopDetector;
  private final String registry;
  private final Map<String, ServerImpl> servers;

  public DockerMachine(
      String containerId,
      String image,
      DockerConnector docker,
      Map<String, ServerImpl> servers,
      String registry,
      DockerMachineStopDetector dockerMachineStopDetector) {
    this.container = containerId;
    this.docker = docker;
    this.image = image;
    this.registry = registry;
    this.dockerMachineStopDetector = dockerMachineStopDetector;
    this.servers = servers;
  }

  @Override
  public Map<String, String> getAttributes() {
    return Collections.emptyMap();
  }

  @Override
  public Map<String, ServerImpl> getServers() {
    return servers;
  }

  void setServerStatus(String serverRef, ServerStatus status) {
    if (servers == null) {
      throw new IllegalStateException("Servers are not initialized yet");
    }
    ServerImpl server = servers.get(serverRef);
    if (server == null) {
      throw new IllegalArgumentException(
          "Server with provided reference " + serverRef + " missing");
    }
    server.setStatus(status);
  }

  public void putResource(String targetPath, InputStream sourceStream)
      throws InfrastructureException {
    try {
      docker.putResource(PutResourceParams.create(container, targetPath, sourceStream));
    } catch (IOException e) {
      throw new InternalInfrastructureException(e.getMessage(), e);
    }
  }

  public void exec(String script, MessageProcessor<LogMessage> messageProcessor)
      throws InfrastructureException {
    try {
      Exec exec =
          docker.createExec(
              CreateExecParams.create(container, new String[] {"/bin/sh", "-c", script})
                  .withDetach(false));
      docker.startExec(StartExecParams.create(exec.getId()), messageProcessor);
    } catch (IOException e) {
      throw new InfrastructureException(e.getLocalizedMessage(), e);
    }
  }

  public void destroy() throws InfrastructureException {
    dockerMachineStopDetector.stopDetection(container);
    try {
      docker.removeContainer(
          RemoveContainerParams.create(container).withRemoveVolumes(true).withForce(true));
    } catch (IOException e) {
      throw new InternalInfrastructureException(e.getMessage(), e);
    }
    try {
      docker.removeImage(RemoveImageParams.create(image).withForce(false));
    } catch (IOException e) {
      LOG.warn("IOException during destroy(). Ignoring.", e);
    }
  }

  /** Can be used for docker specific operations with machine */
  public String getContainer() {
    return container;
  }

  @Override
  public String toString() {
    return "DockerMachine{"
        + "container='"
        + container
        + '\''
        + ", docker="
        + docker
        + ", image='"
        + image
        + '\''
        + ", registry='"
        + registry
        + '\''
        + ", container="
        + container
        + '}';
  }
}
