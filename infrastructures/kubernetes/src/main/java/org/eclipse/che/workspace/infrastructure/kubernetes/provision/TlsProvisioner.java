/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Enables TLS on underlying endpoints implementation. Implementation of this interface must check
 * that tls is enabled - `che.infra.kubernetes.tls_enabled=true`.
 *
 * @param <T> environment type
 */
public interface TlsProvisioner<T extends KubernetesEnvironment> {

  /**
   * If TLS enabled, updates protocol to secure one and ensures that underlying exposure objects are
   * properly configured.
   *
   * @throws InfrastructureException in case of any infrastructure failure
   */
  void provision(T k8sEnv, RuntimeIdentity identity) throws InfrastructureException;

  /**
   * Returns the secure version of the provided protocol or the same protocol if the conversion is
   * not known. Currently only understands "ws" and "http".
   */
  static String getSecureProtocol(final String protocol) {
    if ("ws".equals(protocol)) {
      return "wss";
    } else if ("http".equals(protocol)) {
      return "https";
    } else {
      return protocol;
    }
  }
}
