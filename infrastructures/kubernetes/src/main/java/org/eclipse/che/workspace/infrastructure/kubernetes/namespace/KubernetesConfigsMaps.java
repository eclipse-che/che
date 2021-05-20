/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static io.fabric8.kubernetes.api.model.DeletionPropagation.BACKGROUND;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.Optional;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;

/**
 * Defines an internal API for managing {@link ConfigMap} instances in {@link
 * KubernetesConfigsMaps#namespace predefined namespace}.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesConfigsMaps {
  private final String namespace;
  private final String workspaceId;
  private final KubernetesClientFactory clientFactory;

  KubernetesConfigsMaps(
      String namespace, String workspaceId, KubernetesClientFactory clientFactory) {
    this.namespace = namespace;
    this.workspaceId = workspaceId;
    this.clientFactory = clientFactory;
  }

  /**
   * Retrieves config map by name.
   *
   * @param configMapName name of config map to get
   * @return config map optional
   * @throws InfrastructureException when any exception occurs
   */
  public Optional<ConfigMap> get(String configMapName) throws InfrastructureException {
    return Optional.ofNullable(
        clientFactory
            .create(workspaceId)
            .configMaps()
            .inNamespace(namespace)
            .withName(configMapName)
            .get());
  }

  /**
   * Creates specified config map.
   *
   * @param configMap config map to create
   * @throws InfrastructureException when any exception occurs
   * @return created {@link ConfigMap}
   */
  public ConfigMap create(ConfigMap configMap) throws InfrastructureException {
    putLabel(configMap, CHE_WORKSPACE_ID_LABEL, workspaceId);
    try {
      return clientFactory
          .create(workspaceId)
          .configMaps()
          .inNamespace(namespace)
          .create(configMap);
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Deletes all existing config maps.
   *
   * @throws InfrastructureException when any exception occurs
   */
  public void delete() throws InfrastructureException {
    try {
      clientFactory
          .create(workspaceId)
          .configMaps()
          .inNamespace(namespace)
          .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
          .withPropagationPolicy(BACKGROUND)
          .delete();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }
}
