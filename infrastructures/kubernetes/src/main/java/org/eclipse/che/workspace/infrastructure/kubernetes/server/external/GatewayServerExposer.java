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

import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Uses Traefik gateway configured with ConfigMaps to expose servers.
 *
 * <p>TODO: implement
 *
 * @param <T> type of environment
 */
public class GatewayServerExposer<T extends KubernetesEnvironment>
    implements ExternalServerExposer<T> {

  @Override
  public void expose(
      T k8sEnv,
      String machineName,
      String serviceName,
      String serverId,
      ServicePort servicePort,
      Map<String, ServerConfig> externalServers) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }
}
