/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases;

import static java.util.Collections.singletonMap;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Pod;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.provision.env.AgentAuthEnableEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MachineTokenEnvVarProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Extends {@link BrokerEnvironmentFactory} to be used in the kubernetes infrastructure.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class KubernetesBrokerEnvironmentFactory
    extends BrokerEnvironmentFactory<KubernetesEnvironment> {

  @Inject
  public KubernetesBrokerEnvironmentFactory(
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      @Named("che.workspace.plugin_broker.image") String pluginBrokerImage,
      @Named("che.workspace.plugin_broker.pull_policy") String brokerPullPolicy,
      AgentAuthEnableEnvVarProvider authEnableEnvVarProvider,
      MachineTokenEnvVarProvider machineTokenEnvVarProvider) {
    super(
        cheWebsocketEndpoint,
        pluginBrokerImage,
        brokerPullPolicy,
        authEnableEnvVarProvider,
        machineTokenEnvVarProvider);
  }

  @Override
  protected KubernetesEnvironment doCreate(
      String machineName,
      InternalMachineConfig machine,
      String configMapName,
      ConfigMap configMap,
      Pod pod) {
    return KubernetesEnvironment.builder()
        .setConfigMaps(singletonMap(configMapName, configMap))
        .setMachines(singletonMap(machineName, machine))
        .setPods(singletonMap(pod.getMetadata().getName(), pod))
        .build();
  }
}
