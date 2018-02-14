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
package org.eclipse.che.workspace.infrastructure.kubernetes.server;

import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/** @author Guy Daich */
public interface ExternalServerExposerStrategy {

  void exposeExternalServers(
      KubernetesEnvironment k8sEnv,
      String machineName,
      String serviceName,
      Map<String, ServicePort> portToServicePort,
      Map<String, ServerConfig> externalServers);
}
