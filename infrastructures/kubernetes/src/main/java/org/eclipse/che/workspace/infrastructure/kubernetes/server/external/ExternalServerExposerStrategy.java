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

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerResolver;

/**
 * Defines a basic set of operations for exposing external servers.
 *
 * @author Guy Daich
 */
public interface ExternalServerExposerStrategy<T extends KubernetesEnvironment> {

  /**
   * Exposes service ports on given service externally (outside kubernetes cluster). Each exposed
   * service port is associated with a specific Server configuration. Server configuration should be
   * encoded in the exposing object's annotations, to be used by {@link KubernetesServerResolver}.
   *
   * @param k8sEnv Kubernetes environment
   * @param machineName machine containing servers
   * @param serviceName service associated with machine, mapping all machine server ports
   * @param servicePort specific service port to be exposed externally
   * @param externalServers server configs of servers to be exposed externally
   */
  void expose(
      T k8sEnv,
      String machineName,
      String serviceName,
      ServicePort servicePort,
      Map<String, ServerConfig> externalServers);

  /**
   * Sometimes, the exposer needs to modify the path contained in the object exposing the server
   * (ingress or route). Namely, this is needed to make the URL rewriting work for single-host
   * strategy where the path needs to contain a regular expression match group to retain some of the
   * path.
   *
   * <p>This method reverts such mangling and returns to the user a path that can be used by the
   * HTTP clients.
   *
   * @param exposingObject a Kubernetes object in charge of actual exposure of the server (i.e.
   *     ingress or route)
   * @param path the path contained within the configuration of the object that needs to be
   *     demangled
   * @return the path demangled such that it can be used in an externally reachable URL
   */
  default String demanglePath(HasMetadata exposingObject, String path) {
    return path;
  }
}
