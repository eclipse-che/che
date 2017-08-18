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
package org.eclipse.che.plugin.docker.machine;

import com.google.common.base.MoreObjects;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.core.util.JsonRpcEndpointToMachineNameHolder;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.machine.authentication.server.MachineTokenRegistry;
import org.eclipse.che.plugin.docker.client.DockerConnectorProvider;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;

/**
 * Creates/destroys docker networks and creates docker compose based {@link Instance}.
 *
 * @author Alexander Garagatyi
 */
public class AuthMachineProviderImpl extends MachineProviderImpl {

  private final MachineTokenRegistry machineTokenRegistry;

  @Inject
  public AuthMachineProviderImpl(
      DockerConnectorProvider dockerConnectorProvider,
      UserSpecificDockerRegistryCredentialsProvider dockerCredentials,
      DockerMachineFactory dockerMachineFactory,
      DockerInstanceStopDetector dockerInstanceStopDetector,
      WindowsPathEscaper windowsPathEscaper,
      RequestTransmitter requestTransmitter,
      MachineTokenRegistry machineTokenRegistry,
      JsonRpcEndpointToMachineNameHolder endpointIdsHolder,
      @Named("machine.docker.dev_machine.machine_servers") Set<ServerConf> devMachineServers,
      @Named("machine.docker.machine_servers") Set<ServerConf> allMachinesServers,
      @Named("machine.docker.dev_machine.machine_volumes") Set<String> devMachineSystemVolumes,
      @Named("machine.docker.machine_volumes") Set<String> allMachinesSystemVolumes,
      @Named("che.docker.always_pull_image") boolean doForcePullOnBuild,
      @Named("che.docker.privileged") boolean privilegedMode,
      @Named("che.docker.pids_limit") int pidsLimit,
      @Named("machine.docker.dev_machine.machine_env") Set<String> devMachineEnvVariables,
      @Named("machine.docker.machine_env") Set<String> allMachinesEnvVariables,
      @Named("che.docker.registry_for_snapshots") boolean snapshotUseRegistry,
      @Named("che.docker.swap") double memorySwapMultiplier,
      MachineTokenRegistry tokenRegistry,
      @Named("machine.docker.networks") Set<Set<String>> additionalNetworks,
      @Nullable @Named("che.docker.network_driver") String networkDriver,
      @Nullable @Named("che.docker.parent_cgroup") String parentCgroup,
      @Nullable @Named("che.docker.cpuset_cpus") String cpusetCpus,
      @Named("che.docker.cpu_period") long cpuPeriod,
      @Named("che.docker.cpu_quota") long cpuQuota,
      @Named("che.docker.extra_hosts") Set<Set<String>> additionalHosts,
      @Nullable @Named("che.docker.dns_resolvers") String[] dnsResolvers,
      @Named("che.docker.build_args") Map<String, String> buildArgs)
      throws IOException {
    super(
        dockerConnectorProvider,
        dockerCredentials,
        dockerMachineFactory,
        dockerInstanceStopDetector,
        requestTransmitter,
        endpointIdsHolder,
        devMachineServers,
        allMachinesServers,
        devMachineSystemVolumes,
        allMachinesSystemVolumes,
        doForcePullOnBuild,
        privilegedMode,
        pidsLimit,
        devMachineEnvVariables,
        allMachinesEnvVariables,
        snapshotUseRegistry,
        memorySwapMultiplier,
        additionalNetworks,
        networkDriver,
        parentCgroup,
        cpusetCpus,
        cpuPeriod,
        cpuQuota,
        windowsPathEscaper,
        additionalHosts,
        dnsResolvers,
        buildArgs);
    this.machineTokenRegistry = machineTokenRegistry;
  }

  @Override
  protected String getUserToken(String wsId) {
    String userToken = null;
    try {
      userToken =
          machineTokenRegistry.getOrCreateToken(
              EnvironmentContext.getCurrent().getSubject().getUserId(), wsId);
    } catch (NotFoundException ignore) {
    }
    return MoreObjects.firstNonNull(userToken, "");
  }
}
