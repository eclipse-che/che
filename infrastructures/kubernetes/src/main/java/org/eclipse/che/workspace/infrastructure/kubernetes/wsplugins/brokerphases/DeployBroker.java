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

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Pod;
import java.util.List;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.slf4j.Logger;

/**
 * Deploys Che plugin broker in a workspace, calls next {@link BrokerPhase} and removes deployment
 * after next phase completes.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class DeployBroker extends BrokerPhase {

  private static final Logger LOG = getLogger(DeployBroker.class);

  private final KubernetesNamespace namespace;
  private final KubernetesEnvironment brokerEnvironment;

  public DeployBroker(KubernetesNamespace namespace, KubernetesEnvironment brokerEnvironment) {
    this.namespace = namespace;
    this.brokerEnvironment = brokerEnvironment;
  }

  @Override
  public List<ChePlugin> execute() throws InfrastructureException {
    KubernetesDeployments deployments = namespace.deployments();
    try {
      // Creates config map that can inject Che tooling plugins meta files into a Che plugin
      // broker in a workspace.
      for (ConfigMap configMap : brokerEnvironment.getConfigMaps().values()) {
        namespace.configMaps().create(configMap);
      }
      for (Pod toCreate : brokerEnvironment.getPods().values()) {
        deployments.deploy(toCreate);
      }

      return nextPhase.execute();
    } finally {
      try {
        deployments.delete();
      } catch (InfrastructureException e) {
        LOG.error("Broker deployment removal failed. Error: " + e.getLocalizedMessage(), e);
      }
      try {
        namespace.configMaps().delete();
      } catch (InfrastructureException ex) {
        LOG.error(
            "Broker deployment config map removal failed. Error: " + ex.getLocalizedMessage(), ex);
      }
    }
  }
}
