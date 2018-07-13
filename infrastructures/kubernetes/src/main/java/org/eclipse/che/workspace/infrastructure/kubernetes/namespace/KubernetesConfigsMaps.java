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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClientException;
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
   * Creates specified config map.
   *
   * @param configMap config map to create
   * @throws InfrastructureException when any exception occurs
   */
  public void create(ConfigMap configMap) throws InfrastructureException {
    putLabel(configMap, CHE_WORKSPACE_ID_LABEL, workspaceId);
    try {
      clientFactory.create(workspaceId).configMaps().inNamespace(namespace).create(configMap);
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Deletes all existing secrets.
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
          .delete();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }
}
