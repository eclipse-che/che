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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.DEVFILE_ENDPOINT;

import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

@Singleton
public class CombinedSingleHostServerExposer<T extends KubernetesEnvironment>
    implements ExternalServerExposer<T> {

  private final ExternalServerExposer<T> subdomainServerExposer;
  private final ExternalServerExposer<T> subpathServerExposer;

  public CombinedSingleHostServerExposer(
      ExternalServerExposer<T> subdomainServerExposer,
      ExternalServerExposer<T> subpathServerExposer) {
    this.subdomainServerExposer = subdomainServerExposer;
    this.subpathServerExposer = subpathServerExposer;
  }

  @Override
  public void expose(
      T k8sEnv,
      String machineName,
      String serviceName,
      String serverId,
      ServicePort servicePort,
      Map<String, ServerConfig> externalServers) {

    if (serverId == null) {
      // this is the ID for non-unique servers
      serverId = servicePort.getName();
    }

    Map<String, ServerConfig> subpathServers = new HashMap<>();
    Map<String, ServerConfig> subdomainServers = new HashMap<>();

    for (String esKey : externalServers.keySet()) {
      ServerConfig serverConfig = externalServers.get(esKey);
      if (TRUE.toString()
          .equals(serverConfig.getAttributes().getOrDefault(DEVFILE_ENDPOINT, FALSE.toString()))) {
        subdomainServers.put(esKey, serverConfig);
      } else {
        subpathServers.put(esKey, serverConfig);
      }
    }

    if (!subpathServers.isEmpty()) {
      subpathServerExposer.expose(
          k8sEnv, machineName, serviceName, serverId, servicePort, subpathServers);
    }

    if (!subdomainServers.isEmpty()) {
      subdomainServerExposer.expose(
          k8sEnv, machineName, serviceName, serverId, servicePort, subdomainServers);
    }
  }
}
